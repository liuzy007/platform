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

import java.util.List;


public class DiamondClientUtil {

    private static class Holder {
        private static final DiamondClient instance = new DiamondClient();
    }


    public static void close() {
    }


    public static void addDataId(String clusterType, String dataId) {
        Holder.instance.addDataId(clusterType, dataId);
    }


    public static void setServerAddrs(String clusterType, List<String> addrs) {
        Holder.instance.setServerList(clusterType, addrs);
    }
    
    public static void addServerAddr(String clusterType, String addr) {
        Holder.instance.addServerList(clusterType, addr);
    }


    public static void addPubDataId(String clusterType, String dataId) {
        Holder.instance.addPubDataId(clusterType, dataId);
    }
    
    public static void addPopCount(String clusterType, String dataId, String groupId) {
        Holder.instance.addPopCount(clusterType, dataId, groupId);
    }
}
