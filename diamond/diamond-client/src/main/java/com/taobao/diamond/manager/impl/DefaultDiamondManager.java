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

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.client.DiamondSubscriber;
import com.taobao.diamond.client.impl.DefaultSubscriberListener;
import com.taobao.diamond.client.impl.DiamondClientFactory;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.ManagerListener;

/**
 * 需要注意的是：一个JVM中一个DataID只能对应一个DiamondManager
 * 
 * @author aoqiong
 * 
 */
public class DefaultDiamondManager implements DiamondManager {

	private static final Log log = LogFactory
			.getLog(DefaultDiamondManager.class);

	private static final AtomicLong INSTANCE_ID = new AtomicLong(0);

	private DiamondSubscriber diamondSubscriber = null;
	private final List<ManagerListener> managerListeners = new LinkedList<ManagerListener>();
	private final String dataId;
	private final String group;
	private final String instanceId = String.valueOf(INSTANCE_ID
			.getAndIncrement());

	public static class Builder {
		private final String dataId;
		private String group = null;
		private final List<ManagerListener> managerListenerList = new LinkedList<ManagerListener>();
		private DiamondConfigure diamondConfigure = null;

		public Builder(String dataId, ManagerListener managerListener) {
			this.dataId = dataId;
			managerListenerList.add(managerListener);
		}

		public Builder(String dataId, List<ManagerListener> managerListenerList) {
			this.dataId = dataId;
			this.managerListenerList.addAll(managerListenerList);
		}

		public Builder setGroup(String group) {
			this.group = group;
			return this;
		}

		public Builder setDiamondConfigure(DiamondConfigure diamondConfigure) {
			this.diamondConfigure = diamondConfigure;
			return this;
		}

		public DiamondManager build() {
			return new DefaultDiamondManager(this);
		}
	}

	private DefaultDiamondManager(Builder builder) {
		this.dataId = builder.dataId;
		this.group = builder.group;

		diamondSubscriber = DiamondClientFactory
				.getSingletonDiamondSubscriber(Constants.DEFAULT_DIAMOND_CLUSTER);

		if (null != builder.diamondConfigure) {
			this.diamondSubscriber
					.setDiamondConfigure(builder.diamondConfigure);
		}
		this.managerListeners.addAll(builder.managerListenerList);
		((DefaultSubscriberListener) diamondSubscriber.getSubscriberListener())
				.addManagerListeners(this.dataId, this.group, this.instanceId,
						this.managerListeners);
		diamondSubscriber.addDataId(this.dataId, this.group);
		diamondSubscriber.start();
	}

	/**
	 * 需要注意的是：一个JVM中一个DataID只能对应一个DiamondManager 使用默认的集群类型diamond
	 * 
	 * @param dataId
	 * @param managerListener
	 */
	public DefaultDiamondManager(String dataId, ManagerListener managerListener) {
		this(null, dataId, managerListener);
	}

	/**
	 * 需要注意的是：一个JVM中一个DataID只能对应一个DiamondManager 使用默认的集群类型diamond
	 * 
	 * @param group
	 * @param dataId
	 * @param managerListener
	 */
	public DefaultDiamondManager(String group, String dataId,
			ManagerListener managerListener) {
		this(group, dataId, managerListener, Constants.DEFAULT_DIAMOND_CLUSTER);

	}

	/**
	 * 使用指定的集群类型clusterType
	 * 
	 * @param group
	 * @param dataId
	 * @param managerListener
	 * @param clusterType
	 */
	public DefaultDiamondManager(String group, String dataId,
			ManagerListener managerListener, String clusterType) {
		this.dataId = dataId;
		this.group = group;

		diamondSubscriber = DiamondClientFactory
				.getSingletonDiamondSubscriber(clusterType);

		this.managerListeners.add(managerListener);
		((DefaultSubscriberListener) diamondSubscriber.getSubscriberListener())
				.addManagerListeners(this.dataId, this.group, this.instanceId,
						this.managerListeners);
		diamondSubscriber.addDataId(this.dataId, this.group);
		diamondSubscriber.start();
	}

	/**
	 * 可以选择是否启用实时通知功能的构造方法，默认实时通知功能开启 使用默认的集群类型diamond
	 * 
	 * @param group
	 * @param dataId
	 * @param managerListener
	 * @param useRealTimeNotification
	 *            是否启用实时通知功能
	 */
	public DefaultDiamondManager(String group, String dataId,
			ManagerListener managerListener, boolean useRealTimeNotification) {
		this(group, dataId, managerListener, useRealTimeNotification,
				Constants.DEFAULT_DIAMOND_CLUSTER);
	}

	/**
	 * 可以选择是否启用实时通知功能的构造方法，默认实时通知功能开启 使用指定的集群类型clusterType
	 * 
	 * @param group
	 * @param dataId
	 * @param managerListener
	 * @param useRealTimeNotification
	 * @param clusterType
	 */
	public DefaultDiamondManager(String group, String dataId,
			ManagerListener managerListener, boolean useRealTimeNotification,
			String clusterType) {
		this.dataId = dataId;
		this.group = group;

		diamondSubscriber = DiamondClientFactory
				.getSingletonDiamondSubscriber(clusterType);

		this.managerListeners.add(managerListener);
		((DefaultSubscriberListener) diamondSubscriber.getSubscriberListener())
				.addManagerListeners(this.dataId, this.group, this.instanceId,
						this.managerListeners);
		diamondSubscriber.addDataId(this.dataId, this.group);
		diamondSubscriber.start();

	}

	/**
	 * 需要注意的是：一个JVM中一个DataID只能对应一个DiamondManager 使用默认的集群类型diamond
	 * 
	 * @param dataId
	 * @param managerListener
	 */
	public DefaultDiamondManager(String dataId,
			List<ManagerListener> managerListenerList) {
		this(null, dataId, managerListenerList);
	}

	/**
	 * 需要注意的是：一个JVM中一个DataID只能对应一个DiamondManager 使用默认的集群类型diamond
	 * 
	 * @param group
	 * @param dataId
	 * @param managerListener
	 */
	public DefaultDiamondManager(String group, String dataId,
			List<ManagerListener> managerListenerList) {
		this(group, dataId, managerListenerList,
				Constants.DEFAULT_DIAMOND_CLUSTER);
	}

	/**
	 * 使用指定的集群类型clusterType
	 * 
	 * @param group
	 * @param dataId
	 * @param managerListenerList
	 * @param clusterType
	 */
	public DefaultDiamondManager(String group, String dataId,
			List<ManagerListener> managerListenerList, String clusterType) {
		this.dataId = dataId;
		this.group = group;

		diamondSubscriber = DiamondClientFactory
				.getSingletonDiamondSubscriber(clusterType);

		this.managerListeners.addAll(managerListenerList);
		((DefaultSubscriberListener) diamondSubscriber.getSubscriberListener())
				.addManagerListeners(this.dataId, this.group, this.instanceId,
						this.managerListeners);
		diamondSubscriber.addDataId(this.dataId, this.group);
		diamondSubscriber.start();

	}

	/**
	 * 可以选择是否启用实时通知功能的构造方法，默认实时通知功能开启 使用默认的集群类型diamond
	 * 
	 * @param group
	 * @param dataId
	 * @param managerListenerList
	 * @param useRealTimeNotification
	 */
	public DefaultDiamondManager(String group, String dataId,
			List<ManagerListener> managerListenerList,
			boolean useRealTimeNotification) {
		this(group, dataId, managerListenerList, useRealTimeNotification,
				Constants.DEFAULT_DIAMOND_CLUSTER);
	}

	/**
	 * 可以选择是否启用实时通知功能的构造方法，默认实时通知功能开启 使用指定的集群类型clusterType
	 * 
	 * @param group
	 * @param dataId
	 * @param managerListenerList
	 * @param useRealTimeNotification
	 * @param clusterType
	 */
	public DefaultDiamondManager(String group, String dataId,
			List<ManagerListener> managerListenerList,
			boolean useRealTimeNotification, String clusterType) {
		this.dataId = dataId;
		this.group = group;

		diamondSubscriber = DiamondClientFactory
				.getSingletonDiamondSubscriber(clusterType);

		this.managerListeners.addAll(managerListenerList);
		((DefaultSubscriberListener) diamondSubscriber.getSubscriberListener())
				.addManagerListeners(this.dataId, this.group, this.instanceId,
						this.managerListeners);
		diamondSubscriber.addDataId(this.dataId, this.group);
		diamondSubscriber.start();

	}

	public void setManagerListener(ManagerListener managerListener) {
		this.managerListeners.clear();
		this.managerListeners.add(managerListener);

		((DefaultSubscriberListener) diamondSubscriber.getSubscriberListener())
				.removeManagerListeners(this.dataId, this.group,
						this.instanceId);
		((DefaultSubscriberListener) diamondSubscriber.getSubscriberListener())
				.addManagerListeners(this.dataId, this.group, this.instanceId,
						this.managerListeners);
	}

	public List<ManagerListener> getManagerListeners() {
		return ((DefaultSubscriberListener) this.diamondSubscriber
				.getSubscriberListener()).getManagerListenerList(dataId, group,
				instanceId);
	}

	public void close() {
		/**
		 * 因为同一个DataID只能对应一个MnanagerListener，所以，关闭时一次性关闭所有ManagerListener即可
		 */
		((DefaultSubscriberListener) diamondSubscriber.getSubscriberListener())
				.removeManagerListeners(this.dataId, this.group,
						this.instanceId);

		diamondSubscriber.removeDataId(dataId, group);
		if (diamondSubscriber.getDataIds().size() == 0) {
			diamondSubscriber.close();
		}

	}

	public String getConfigureInfomation(long timeout) {
		return diamondSubscriber.getConfigureInfomation(this.dataId,
				this.group, timeout);
	}

	public String getAvailableConfigureInfomation(long timeout) {
		return diamondSubscriber.getAvailableConfigureInfomation(dataId, group,
				timeout);
	}

	public String getAvailableConfigureInfomationFromSnapshot(long timeout) {
		return diamondSubscriber.getAvailableConfigureInfomationFromSnapshot(
				dataId, group, timeout);
	}

	public Properties getAvailablePropertiesConfigureInfomation(long timeout) {
		String configInfo = this.getAvailableConfigureInfomation(timeout);
		Properties properties = new Properties();
		try {
			properties.load(new StringReader(configInfo));
			return properties;
		} catch (IOException e) {
			log.warn("装载properties失败：" + configInfo, e);
			throw new RuntimeException("装载properties失败：" + configInfo, e);
		}
	}

	public Properties getAvailablePropertiesConfigureInfomationFromSnapshot(
			long timeout) {
		String configInfo = this
				.getAvailableConfigureInfomationFromSnapshot(timeout);
		Properties properties = new Properties();
		try {
			properties.load(new StringReader(configInfo));
			return properties;
		} catch (IOException e) {
			log.warn("装载properties失败：" + configInfo, e);
			throw new RuntimeException("装载properties失败：" + configInfo, e);
		}
	}

	public void setManagerListeners(List<ManagerListener> managerListenerList) {
		this.managerListeners.clear();
		this.managerListeners.addAll(managerListenerList);

		((DefaultSubscriberListener) diamondSubscriber.getSubscriberListener())
				.removeManagerListeners(this.dataId, this.group,
						this.instanceId);
		((DefaultSubscriberListener) diamondSubscriber.getSubscriberListener())
				.addManagerListeners(this.dataId, this.group, this.instanceId,
						this.managerListeners);
	}

	public DiamondConfigure getDiamondConfigure() {
		return diamondSubscriber.getDiamondConfigure();
	}

	public void setDiamondConfigure(DiamondConfigure diamondConfigure) {
		diamondSubscriber.setDiamondConfigure(diamondConfigure);
	}

	public Properties getPropertiesConfigureInfomation(long timeout) {
		String configInfo = this.getConfigureInfomation(timeout);
		Properties properties = new Properties();
		try {
			properties.load(new StringReader(configInfo));
			return properties;
		} catch (IOException e) {
			log.warn("装载properties失败：" + configInfo, e);
			throw new RuntimeException("装载properties失败：" + configInfo, e);
		}
	}

	public Set<String> getAllDataId() {
		return diamondSubscriber.getDataIds();
	}

	public List<String> getServerAddress() {
		return diamondSubscriber.getDiamondConfigure().getDomainNameList();
	}

	public boolean exists(String dataId, String group) {
		if (StringUtils.isBlank(dataId) || StringUtils.isBlank(group)) {
			throw new IllegalArgumentException("dataId, group不能为null或者空字符串");
		}
		return this.diamondSubscriber.exists(dataId, group);
	}

}
