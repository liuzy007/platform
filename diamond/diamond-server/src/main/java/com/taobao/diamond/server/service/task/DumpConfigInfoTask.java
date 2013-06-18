/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.service.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.util.WebUtils;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.domain.Page;
import com.taobao.diamond.server.service.TimerTaskService;
import com.taobao.diamond.server.utils.SystemConfig;
import com.taobao.diamond.utils.FileUtils;


/**
 * Dump配置信息任务
 * 
 * @author boyan
 * @date 2010-5-10
 */
public final class DumpConfigInfoTask implements Runnable {

    private static final String EXIT = "0";
    private static final String LOCAL = "1";
    static final Log log = LogFactory.getLog(DumpConfigInfoTask.class);
    PrintStream err = null;
    private List<ConfigInfo> configInfos;
    /**
     * 
     */
    private final TimerTaskService timerTaskService;


    /**
     * @param timerTaskService
     */
    public DumpConfigInfoTask(TimerTaskService timerTaskService, List<ConfigInfo> configInfos) {
        this.timerTaskService = timerTaskService;
        this.configInfos = configInfos;
        try {
            err = new PrintStream(new FileOutputStream("DumpConfigInfoTask.Error.txt"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static final int PAGE_SIZE = 1000;
    private static boolean pause = false;


    public void run() {
        if (configInfos != null) {
            configInfos.clear();
        }
        if (SystemConfig.isOfflineMode())
            return;
        if (pause)
            return;

        try {

            // System.exit(0);
            Page<ConfigInfo> page = this.timerTaskService.getPersistService().findAllConfigInfo(1, PAGE_SIZE);
            updateLastRunSuccessTs();
            if (page != null) {
                // 总页数
                int totalPages = page.getPagesAvailable();
                updateConfigInfo(page);
                if (totalPages > 1) {
                    for (int pageNo = 2; pageNo <= totalPages; pageNo++) {
                        page = this.timerTaskService.getPersistService().findAllConfigInfo(pageNo, PAGE_SIZE);
                        updateLastRunSuccessTs();
                        if (page != null) {
                            updateConfigInfo(page);
                        }
                    }
                }
            }
            else {
                dealLoadException(new Exception("page is null"));
            }
            this.timerTaskService.setFirstLoadDumpInfo(false);
        }
        catch (Throwable t) {
            dealLoadException(t);
        }
    }


    private void dealLoadException(Throwable t) {
        timerTaskService.setLastDumpErrorTs(System.currentTimeMillis());
        // query timeout
        if (t.getMessage().indexOf("Query execution was interrupted") != -1) {
            String msg = "PersistService#findAllConfigInfo#:数据库连接超时";
            log.error(msg, t);
            if (timerTaskService.isFirstLoadDumpInfo()) {
                dealFail("");
            }
            else {

            }
            throw new RuntimeException(msg);
        }
        // 加载异常，记入本地文件
        recordDumpInfoFaild(new Date());
        log.error("运行dump配置任务出错", t);
    }


    private void recordDumpInfoFaild(Date ts) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd hh:mm");
        String tss = sdf.format(ts);
        if (err != null) {
            err.println("dumpinfo err:" + tss);
            err.flush();
        }

    }


    public void dealFail(String info) {
        System.out.println(info);
        String onFirstLoadFail = System.getProperty("onFirstLoadFail", EXIT);
        if (onFirstLoadFail.indexOf(EXIT) != -1) {
            System.out.println("第一次load dump info 出错 系统退出.");
            SystemConfig.system_pause();
            System.exit(1);
        }
        if (onFirstLoadFail.indexOf(LOCAL) != -1) {
            System.out.println("当前SystemConfig.dbConnLostDealing:" + SystemConfig.LOAD_DUMP + ":" + "尝试加载本地Dump");
            tryLoadLocalDistDumpInfo();
        }
    }


    public List<ConfigInfo> tryLoadLocalDistDumpInfo() {
        List<ConfigInfo> result = new ArrayList<ConfigInfo>();
        log.warn("tryLoadLocalDumpInfo");
        if (checkDumpTs()) {
            try {
                String basePath =
                        WebUtils.getRealPath(this.timerTaskService.getDiskService().getServletContext(),
                            Constants.BASE_DIR);
                File file = new File(basePath);
                if (file.exists()) {
                    if (checkDumpTs())
                        for (File group : file.listFiles()) {
                            if (checkDumpTs())
                                for (File dataId : group.listFiles()) {
                                    String groupName = group.getName();
                                    String dataIdName = dataId.getName();
                                    dataIdName = SystemConfig.decodeFnForDataIdIfUnderWin(dataIdName);
                                    if (checkDumpTs())
                                        try {
                                            String content = FileUtils.getFileContent(dataId.getAbsolutePath());
                                            ConfigInfo configInfo = new ConfigInfo(dataIdName, groupName, content);
                                            result.add(configInfo);
                                            timerTaskService.getConfigService().updateMD5Cache(configInfo);
                                        }
                                        catch (IOException e) {
                                            log.error("加载ConfigInfo失败:dataId:" + dataIdName + ":" + groupName);
                                            log.error(e.getMessage(), e.getCause());
                                        }

                                }
                        }
                }
                timerTaskService.setFirstLoadDumpInfo(false);
            }
            catch (FileNotFoundException e) {
                log.error(e.getMessage(), e.getCause());
            }
        }
        return result;
    }


    private boolean checkDumpTs() {
        return timerTaskService.getLastDumpErrorTs() > timerTaskService.getLastDumpSuccessTs();
    }


    private void updateLastRunSuccessTs() {
        timerTaskService.setLastDumpSuccessTs(System.currentTimeMillis());
    }


    private void updateConfigInfo(Page<ConfigInfo> page) throws IOException {
        for (ConfigInfo configInfo : page.getPageItems()) {
            if (this.configInfos != null) {
                configInfos.add(configInfo);
            }
            try {
                // 写入磁盘，更新缓存
                this.timerTaskService.getConfigService().updateMD5Cache(configInfo);

                this.timerTaskService.getDiskService().saveToDisk(configInfo);
            }
            catch (Throwable t) {
                log.error("Dump配置信息出错", t);
            }

        }
    }


    public void runFlyingMode() {
        if (configInfos != null) {
            configInfos.clear();
        }
        if (SystemConfig.isOnlineMode()) {
            throw new IllegalArgumentException("flyingMode只能在离线模式使用");
        }
        configInfos.addAll(tryLoadLocalDistDumpInfo());

    }


    public List<ConfigInfo> getConfigInfos() {
        return configInfos;
    }

}