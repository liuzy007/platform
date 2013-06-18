/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.server.service.task.DumpConfigInfoTask;
import com.taobao.diamond.server.utils.SystemConfig;


/**
 * 定时任务服务器
 * 
 * @author boyan
 * @date 2010-5-7
 */
// @Service
public class TimerTaskService {

    volatile long lastDumpSuccessTs = System.currentTimeMillis();
    volatile long lastDumpErrorTs = System.currentTimeMillis();

    volatile boolean isFirstLoadDumpInfo = true;
    static int DB_LOST = 0;
    static int DB_NORMAL = 0;
    volatile long dbMode = DB_NORMAL;


    public boolean isFirstLoadDumpInfo() {
        return isFirstLoadDumpInfo;
    }


    public void setFirstLoadDumpInfo(boolean isFirstLoadDumpInfo) {
        this.isFirstLoadDumpInfo = isFirstLoadDumpInfo;
    }


    public void setLastDumpSuccessTs(long lastDumpSuccessTs) {
        this.lastDumpSuccessTs = lastDumpSuccessTs;
    }


    public void setLastDumpErrorTs(long lastDumpErrorTs) {
        this.lastDumpErrorTs = lastDumpErrorTs;
    }


    public long getLastDumpSuccessTs() {
        return lastDumpSuccessTs;
    }


    public long getLastDumpErrorTs() {
        return lastDumpErrorTs;
    }


    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    // @Autowired
    // @Qualifier("persistService")
    private PersistService persistService;
    // @Autowired
    private DiskService diskService;

    // /@Autowired
    private ConfigService configService;

    // @Autowired
    private SystemConfig systemConfig;


    public void setSystemConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    private ScheduledExecutorService scheduledExecutorService;


    // @PostConstruct
    public void init() {
        List<ConfigInfo> configInfos = new ArrayList<ConfigInfo>();
        if (SystemConfig.isOfflineMode()) {
            initWithoutDB(configInfos);
        }
        else {
            initWithDB(configInfos);
        }
        try {
            Thread.sleep(5000);
        }
        catch (Exception e) {

        }
        finally {
            System.out.println("#######################################################");
            System.out.println("############## ConfigInfo 自检文件开始生成  #####################");
            productConfigInfoCheckBatchFile(configInfos);
            System.out.println("############## ConfigInfo 自检文件生成完毕  #####################");
            System.out.println("#######################################################");
        }

    }


    public int checkStatusCode(String sURL) {
        try {
            URL url = new URL(sURL);

            URLConnection conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection httpUrl = (HttpURLConnection) conn;
                return httpUrl.getResponseCode();
            }
            return -1;
        }
        catch (Exception e) {
            return -1;
        }
    }

    public static String fn = "";


    private void productConfigInfoCheckBatchFile(List<ConfigInfo> configInfos) {
        String GROUP = "$GROUP$";
        String DATAID = "$DATAID$";
        String urlPattern = "$GROUP$\t$DATAID$";
        Map<Integer, Integer> scMap = new HashMap<Integer, Integer>();
        try {
            File f = new File("slf_check_urls.txt");
            fn = f.getAbsolutePath();
            System.out.println("自检文件路径 :" + f.getAbsolutePath());
            PrintStream out = new PrintStream(new FileOutputStream(f));
            int total = configInfos.size();
            for (ConfigInfo configInfo : configInfos) {
                String url = urlPattern.replace(GROUP, configInfo.getGroup());
                url = url.replace(DATAID, configInfo.getDataId());
                out.println(url);
            }
            out.flush();
            out.close();
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException("can not create check.sh");
        }
    }


    private void initWithDB(List<ConfigInfo> configInfos) {
        this.scheduledExecutorService = Executors.newScheduledThreadPool(10);
        DumpConfigInfoTask configInfoTask = new DumpConfigInfoTask(this, configInfos);
        configInfoTask.run();
        this.scheduledExecutorService.scheduleAtFixedRate(configInfoTask, SystemConfig.getDumpConfigInterval(),
            SystemConfig.getDumpConfigInterval(), TimeUnit.SECONDS);
    }


    @SuppressWarnings("unused")
    private void checkService(Object o) {

    }


    private void initWithoutDB(List<ConfigInfo> configInfos) {
        DumpConfigInfoTask configInfoTask = new DumpConfigInfoTask(this, configInfos);
        configInfoTask.runFlyingMode();
    }


    // @PreDestroy
    public void despose() {
        this.scheduledExecutorService.shutdown();

    }


    public void setPersistService(PersistService persistService) {
        this.persistService = persistService;
    }


    public PersistService getPersistService() {
        return persistService;
    }


    public void setDiskService(DiskService diskService) {
        this.diskService = diskService;
    }


    public ConfigService getConfigService() {
        return configService;
    }


    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }


    public DiskService getDiskService() {
        return diskService;
    }

}
