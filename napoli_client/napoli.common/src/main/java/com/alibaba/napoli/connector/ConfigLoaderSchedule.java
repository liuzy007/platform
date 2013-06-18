package com.alibaba.napoli.connector;

import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.util.ConfigServiceHttpImpl;
import com.alibaba.napoli.domain.client.ClientDestination;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 9/2/11 Time: 5:02 下午
 */
public class ConfigLoaderSchedule implements Runnable {
    private static final Log      log = LogFactory.getLog(ConfigLoaderSchedule.class);
    //private float configServiceVersion = 0.09f;

    private ConfigServiceHttpImpl configService;
    private ConsoleConnector      connector;

    public ConfigLoaderSchedule(ConfigServiceHttpImpl configService, ConsoleConnector connector) {
        this.configService = configService;
        this.connector = connector;
    }

    public void run() {
        try {
            if (connector.getDestinationSize() == 0) {
                return;
            }

            List<String> names = connector.getDestinationNames();

            if (connector.getDestinationSize() < NapoliConstant.DEFAULT_CONFIG_BATCH_SIZE) {
                updateDestinations(names);
                return;
            }

            //分批处理
            List<String> list = new ArrayList<String>();
            int count = 0;
            for (String key : names) {
                list.add(key);
                count++;
                //限制批量大小，避免超大请求
                if (count >= NapoliConstant.DEFAULT_CONFIG_BATCH_SIZE) {
                    updateDestinations(list);
                    //重置，处理下一批
                    list.clear();
                    count = 0;
                }
            }
            //多余的请求
            if (!list.isEmpty()) {
                updateDestinations(list);
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    /**
     * remove and update destinations in Connector
     * 
     * @param names destinations list
     */
    private void updateDestinations(List<String> names) {
        if (log.isInfoEnabled()) {
            log.info("configLoad start for:" + names);
        }
        Map<String, ClientDestination> result = fetchDestination(names);
        if (result == null) {
            return;
        }
        for (Map.Entry<String, ClientDestination> entry : result.entrySet()) {
            //String name = entry.getKey();
            ClientDestination destination = entry.getValue();
            //for safe donot delete destination
            /*
             * if (destination == null) { connector.removeDestination(name);
             * continue; }
             */
            if (destination == null) {
                continue;
            }
            updateDestination(destination);

            /*
             * List<DestinationContext> destinationContextList =
             * connector.getDestinationList(name); //应该不会出现 if
             * (destinationContextList == null) { continue; } for
             * (DestinationContext destinationContext : destinationContextList)
             * { if (destinationContext != null &&
             * destinationContext.getDestination().getModified() !=
             * destination.getModified()) {
             * destinationContext.setDestination(destination);
             * destinationContext.refresh(); } }
             */
        }
    }

    public synchronized void updateDestination(ClientDestination destination) {
        List<DestinationContext> destinationContextList = connector.getDestinationList(destination.getName());
        //应该不会出现
        if (destinationContextList == null) {
            return;
        }
        for (DestinationContext destinationContext : destinationContextList) {
            if (destinationContext != null
                    && destinationContext.getDestination().getModified() < destination.getModified()) {
                destinationContext.setDestination(destination);
                destinationContext.refresh();
            }
        }
    }

    private Map<String, ClientDestination> fetchDestination(List<String> names) {
        //after client 1.5.0, must fit the console 1.4.10+
        /*
         * if (configServiceVersion == 0.09f) { configServiceVersion =
         * getConfigServiceVersion(configService); }
         */
        /*
         * if (configServiceVersion >= NapoliConstant.DEFAULT_CONFIG_VERSION)
         * {//1.3之后才有list接口 return configService.fetchDestination(names); } else
         * { Map<String, Destination> ret = new HashMap<String, Destination>();
         * for (String name : names) {//如果单个有异常，这次不处理，等待下次心跳 ret.put(name,
         * configService.fetchDestination(name)); } return ret; }
         */
        return configService.fetchDestination(names);
    }

    /*
     * private float getConfigServiceVersion(ConfigService configService) {
     * //重试5次，避免napoli server的偶尔错误导致启动失败 //note:null 有特殊含义，无论如何在异常情况下不能返回null
     * int count = 5; while (count > 0) { try { return
     * configService.getVersion(); } catch (Throwable e) {
     *//*
        * if (e instanceof RemoteAccessException) { Throwable nest =
        * ((RemoteAccessException) e).getRootCause(); if (nest instanceof
        * NoSuchMethodException) { //没有这个方法，是老版本接口，使用老版本方法获得心跳 return 1.0f; } }
        *//*
           * //网络故障,目标不存在？ignore it
           * log.error("failed to fetch config and retry " + count + " times.",
           * e); } count--; } return 1.00f; }
           */
}
