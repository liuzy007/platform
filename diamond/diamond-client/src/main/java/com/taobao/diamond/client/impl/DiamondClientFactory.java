/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.client.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.taobao.diamond.client.DiamondSubscriber;
import com.taobao.diamond.common.Constants;

/**
 * Diamond客户端工厂类，可以产生一个单例的DiamondSubscriber，供所有的DiamondManager共用 不同的集群对应不同的单例
 * 
 * @author aoqiong
 * 
 */
public class DiamondClientFactory {
	private static Map<String, DiamondSubscriber> subscribersMap = new ConcurrentHashMap<String, DiamondSubscriber>();

	/**
	 * 获取某个集群的单例订阅者
	 * 
	 * @param clusterType
	 *            集群类型
	 * @return
	 */
	public synchronized static DiamondSubscriber getSingletonDiamondSubscriber(
			String clusterType) {
		DiamondSubscriber diamondSubscriber = subscribersMap.get(clusterType);
		if (diamondSubscriber == null) {
			DefaultSubscriberListener subscriberListener = new DefaultSubscriberListener();
			DefaultDiamondSubscriber.Builder builder = new DefaultDiamondSubscriber.Builder(
					subscriberListener, clusterType);
			diamondSubscriber = builder.build();
			subscriberListener
					.setDiamondSubscriber((DefaultDiamondSubscriber) diamondSubscriber);
			subscribersMap.put(clusterType, diamondSubscriber);
		}
		return diamondSubscriber;
	}

	public synchronized static DiamondSubscriber getSingletonDiamondSubscriber() {
		DiamondSubscriber diamondSubscriber = subscribersMap
				.get(Constants.DEFAULT_DIAMOND_CLUSTER);
		if (diamondSubscriber == null) {
			DefaultSubscriberListener subscriberListener = new DefaultSubscriberListener();
			DefaultDiamondSubscriber.Builder builder = new DefaultDiamondSubscriber.Builder(
					subscriberListener, Constants.DEFAULT_DIAMOND_CLUSTER);
			diamondSubscriber = builder.build();
			subscriberListener
					.setDiamondSubscriber((DefaultDiamondSubscriber) diamondSubscriber);
			subscribersMap.put(Constants.DEFAULT_DIAMOND_CLUSTER,
					diamondSubscriber);
		}
		return diamondSubscriber;
	}

}
