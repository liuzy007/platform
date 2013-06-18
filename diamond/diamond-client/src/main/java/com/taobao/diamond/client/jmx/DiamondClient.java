/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.client.jmx;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;


public class DiamondClient implements DiamondClientMBean {

    private static final Logger log = Logger.getLogger(DiamondClient.class);

    private Map<String, Set<String>> dataIds = new HashMap<String, Set<String>>();
    private Map<String, List<String>> addrs = new HashMap<String, List<String>>();
    private Map<String, Set<String>> pubDataIds = new HashMap<String, Set<String>>();
    private Map<String/* cluster type */, Map<String, Integer>> popCount = new HashMap<String, Map<String, Integer>>();


    public DiamondClient() {

        try {
        	// jira DIAMOND-82
        	MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName =
                    new ObjectName(DiamondClient.class.getPackage().getName() + ":type="
                            + DiamondClient.class.getSimpleName() + "-" + this.hashCode());

            mbs.registerMBean(this, objectName);
        }
        catch (Throwable t) {
            log.error("注册MBean出错", t);
        }

    }


    public Map<String, Set<String>> getDataIds() {
        return dataIds;
    }


    public synchronized void addDataId(String clusterType, String dataId) {
        Set<String> list = dataIds.get(clusterType);
        if (list == null) {
            list = new HashSet<String>();
            dataIds.put(clusterType, list);
        }
        list.add(dataId);
    }


    public Map<String, List<String>> getServerList() {
        return addrs;
    }


    public synchronized void setServerList(String clusterType, List<String> addrs) {
        this.addrs.put(clusterType, addrs);
    }


    public synchronized void addServerList(String clusterType, String addr) {
        List<String> list = addrs.get(clusterType);
        if (list == null) {
            list = new ArrayList<String>();
            addrs.put(clusterType, list);
        }
        if (!list.contains(addr)) {
            list.add(addr);
        }
    }


    public Map<String, Set<String>> getPubDataIds() {
        return pubDataIds;
    }


    public synchronized void addPubDataId(String clusterType, String dataId) {
        Set<String> list = pubDataIds.get(clusterType);
        if (list == null) {
            list = new HashSet<String>();
            pubDataIds.put(clusterType, list);
        }
        list.add(dataId);
    }


    public Map<String, Map<String, Integer>> getPopCount() {
        return popCount;
    }


    public synchronized void addPopCount(String clusterType, String dataId, String groupId) {
        String key = dataId + "-" + groupId;
        Map<String, Integer> map = popCount.get(clusterType);
        if (map == null) {
            map = new HashMap<String, Integer>();
            popCount.put(clusterType, map);
        }
        Integer count = map.get(key);
        if (count == null) {
            log.info("探测到DataId:" + dataId + ",group:" + groupId + ";第1次变化。");
            map.put(key, 1);
        }
        else {
            log.info("探测到DataId:" + dataId + ",group:" + groupId + ";第" + count+1 + "次变化。");
            map.put(key, count + 1);
        }
    }

}
