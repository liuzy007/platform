/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.manager.impl;

import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.taobao.diamond.manager.ManagerListenerAdapter;


public abstract class DelayLoadListener extends ManagerListenerAdapter {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static Pattern pattern = Pattern.compile("diamond-config-effective-time\\s*=\\s*\"(.*?)\"");

    private ScheduledExecutorService scheduled;
    private SimpleDateFormat format;
    private volatile ScheduledFuture<?> previousTask;


    public DelayLoadListener() {
        scheduled = Executors.newSingleThreadScheduledExecutor();
        format = new SimpleDateFormat(DATE_FORMAT);
    }


    public synchronized void receiveConfigInfo(String configInfo) {
        if (configInfo == null || configInfo.length() == 0) {
            return;
        }

        long delay = getDelay(configInfo);
        delayReceive(configInfo, delay);

    }


    private void delayReceive(final String configInfo, long delay) {
        // 首先取消前一次的定时任务
        if (previousTask != null) {
            previousTask.cancel(false);
        }

        if (delay > 0) {
            previousTask = scheduled.schedule(new Runnable() {

                public void run() {
                    innerReceive(configInfo);

                }
            }, delay, TimeUnit.MILLISECONDS);
        }
        else {
            innerReceive(configInfo);
        }

    }


    public abstract void innerReceive(String configInfo);


    /**
     * <pre>
     * 在配置中如果包含了形如 diamond-config-effective-time="2010-07-10 10:29:01"的字符串
     * 取出后面的时间作为配置的生效时间，减去当前时间就是延迟时间
     * </pre>
     * 
     * @param configInfo
     * @return delay time
     */
    private long getDelay(String configInfo) {

        long delay = 0;
        try {
            delay = getEffectiveTime(configInfo) - System.currentTimeMillis();
        }
        catch (Exception e) {

        }

        if (delay < 0) {
            delay = 0;
        }
        return delay;
    }


    private long getEffectiveTime(String configInfo) {
        long time = 0;
        Matcher matcher = pattern.matcher(configInfo);
        String data = null;
        if (matcher.find()) {
            data = matcher.group(1);
        }
        if (data != null) {
            try {
                time = format.parse(data.trim()).getTime();
            }
            catch (Exception e) {
            }
        }
        return time;
    }

}
