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
import java.util.Map;
import java.util.Set;


public interface DiamondClientMBean {
    /**
     * 获取客户端注册监听的所有dataId
     * 
     * @return
     */
    Map<String, Set<String>> getDataIds();


    /**
     * 获取当前使用的服务器列表
     * 
     * @return
     */
    Map<String, List<String>> getServerList();


    /**
     * 获得发布的dataId列表
     * 
     * @return
     */
    Map<String, Set<String>> getPubDataIds();

    /**
     * 获得dataId的推送次数
     */
    Map<String, Map<String, Integer>> getPopCount();
}
