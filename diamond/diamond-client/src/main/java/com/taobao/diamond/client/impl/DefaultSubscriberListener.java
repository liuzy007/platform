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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.client.SubscriberListener;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.configinfo.ConfigureInfomation;
import com.taobao.diamond.manager.ManagerListener;

public class DefaultSubscriberListener implements SubscriberListener {
	private static final Log log = LogFactory
			.getLog(DefaultDiamondSubscriber.class);

	private static final long ACK_TIME_OUT = 10000;

	private final ConcurrentMap<String/* dataId + group */, ConcurrentMap<String/* instanceId */, CopyOnWriteArrayList<ManagerListener>>> allListeners = new ConcurrentHashMap<String, ConcurrentMap<String, CopyOnWriteArrayList<ManagerListener>>>();

	private DefaultDiamondSubscriber diamondSubscriber;

	private RotateType rotateType = RotateType.SERVER;

	public Executor getExecutor() {
		return null;
	}

	public void setDiamondSubscriber(DefaultDiamondSubscriber diamondSubscriber) {
		this.diamondSubscriber = diamondSubscriber;
	}

	void setRotateType(RotateType rotateType) {
		this.rotateType = rotateType;
	}

	public void receiveConfigInfo(final ConfigureInfomation configureInfomation) {
		String dataId = configureInfomation.getDataId();
		String group = configureInfomation.getGroup();
		if (null == dataId) {
			log.error("服务器端返回了空的DataID");
		} else {
			String key = makeKey(dataId, group);
			Map<String, CopyOnWriteArrayList<ManagerListener>> map = allListeners
					.get(key);
			if (null == map) {
				log.warn("客户没有设置MessageListener");
			} else if (map.size() == 0) {
				log.warn("客户没有设置MessageListener");
				allListeners.remove(key);
			} else {
				for (List<ManagerListener> listeners : map.values()) {
					for (ManagerListener listener : listeners) {
						callListener(configureInfomation, listener);
					}
				}
			}
		}
	}

	private void callListener(final ConfigureInfomation configureInfomation,
			final ManagerListener listener) {
		// 如果listener为null，记录日志，不影响客户端的正常使用，同步获取数据依然能够成功
		if (listener == null) {
			log.warn("监听器为空警告：客户端在创建DiamondManager时没有指定监听器");
			return;
		}

		if (null != listener.getExecutor()) {
			listener.getExecutor().execute(new Runnable() {
				public void run() {
					String listenerName = listener.getClass().getName();
					try {
						listener.receiveConfigInfo(configureInfomation
								.getConfigureInfomation());
						// 客户端正确处理了接收到的数据，记录日志 by leiwen
						log.info("客户端正确处理了接收到的数据: " + "listener="
								+ listener.getClass().getName() + " & dataId="
								+ configureInfomation.getDataId() + " & group="
								+ configureInfomation.getGroup()
								+ " & configInfo="
								+ configureInfomation.getConfigureInfomation()
								+ " & type=" + rotateType);
						// 向server发送处理结果
						diamondSubscriber.sendAckToServer(
								configureInfomation.getDataId(),
								configureInfomation.getGroup(),
								Constants.STAT_CLIENT_SUCCESS, listenerName,
								rotateType, ACK_TIME_OUT);
					} catch (Throwable t) {
						// 客户端处理接收到的数据时发生异常，记录日志 by leiwen
						log.error(
								"客户端MessageListener中抛异常，请客户自查："
										+ "listener="
										+ listener.getClass().getName()
										+ " & dataId="
										+ configureInfomation.getDataId()
										+ " & group="
										+ configureInfomation.getGroup()
										+ " & configInfo="
										+ configureInfomation
												.getConfigureInfomation()
										+ " & type=" + rotateType, t);
						// 向server发送处理结果
						diamondSubscriber.sendAckToServer(
								configureInfomation.getDataId(),
								configureInfomation.getGroup(),
								Constants.STAT_CLIENT_FAILURE, listenerName,
								rotateType, ACK_TIME_OUT);
					}
				}
			});
		} else {
			String listenerName = listener.getClass().getName();
			try {
				listener.receiveConfigInfo(configureInfomation
						.getConfigureInfomation());
				// 客户端正确处理了接收到的数据，记录日志 by leiwen
				log.info("客户端正确处理了接收到的数据: " + "listener="
						+ listener.getClass().getName() + " & dataId="
						+ configureInfomation.getDataId() + " & group="
						+ configureInfomation.getGroup() + " & configInfo="
						+ configureInfomation.getConfigureInfomation()
						+ " & type=" + rotateType);
				// 向server发送处理结果
				diamondSubscriber.sendAckToServer(
						configureInfomation.getDataId(),
						configureInfomation.getGroup(),
						Constants.STAT_CLIENT_SUCCESS, listenerName,
						rotateType, ACK_TIME_OUT);
			} catch (Throwable t) {
				// 客户端处理接收到的数据时发生异常，记录日志 by leiwen
				log.error("客户端MessageListener中抛异常，请客户自查：" + "listener="
						+ listener.getClass().getName() + " & dataId="
						+ configureInfomation.getDataId() + " & group="
						+ configureInfomation.getGroup() + " & configInfo="
						+ configureInfomation.getConfigureInfomation()
						+ " & type=" + rotateType, t);
				// 向server发送处理结果
				diamondSubscriber.sendAckToServer(
						configureInfomation.getDataId(),
						configureInfomation.getGroup(),
						Constants.STAT_CLIENT_FAILURE, listenerName,
						rotateType, ACK_TIME_OUT);
			}
		}
	}

	/**
	 * 添加一个DataID对应的ManagerListener
	 * 
	 * @param dataId
	 * @param listener
	 */
	public void addManagerListener(String dataId, String group,
			String instanceId, ManagerListener listener) {
		List<ManagerListener> list = new ArrayList<ManagerListener>();
		list.add(listener);
		addManagerListeners(dataId, group, instanceId, list);
	}

	public List<ManagerListener> getManagerListenerList(String dataId,
			String group, String instanceId) {
		if (null == dataId || null == instanceId) {
			return null;
		}

		List<ManagerListener> ret = null;

		String key = makeKey(dataId, group);
		ConcurrentMap<String, CopyOnWriteArrayList<ManagerListener>> map = allListeners
				.get(key);
		if (map != null) {
			ret = map.get(instanceId);
			if (ret != null) {
				ret = new ArrayList<ManagerListener>(ret);
			}
		}
		return ret;
	}

	/**
	 * 删除一个DataID对应的所有的ManagerListeners
	 * 
	 * @param dataId
	 */
	public void removeManagerListeners(String dataId, String group,
			String instanceId) {
		if (null == dataId || null == instanceId) {
			return;
		}
		String key = makeKey(dataId, group);
		ConcurrentMap<String, CopyOnWriteArrayList<ManagerListener>> map = allListeners
				.get(key);
		if (map != null) {
			map.remove(instanceId);
		}
	}

	/**
	 * 添加一个DataID对应的一些ManagerListener
	 * 
	 * @param dataId
	 * @param addListeners
	 */
	public void addManagerListeners(String dataId, String group,
			String instanceId, List<ManagerListener> addListeners) {
		if (null == dataId || null == addListeners || null == instanceId) {
			return;
		}
		if (addListeners.size() == 0) {
			return;
		}

		String key = makeKey(dataId, group);
		ConcurrentMap<String, CopyOnWriteArrayList<ManagerListener>> map = allListeners
				.get(key);
		if (map == null) {
			map = new ConcurrentHashMap<String, CopyOnWriteArrayList<ManagerListener>>();
			ConcurrentMap<String, CopyOnWriteArrayList<ManagerListener>> oldMap = allListeners
					.putIfAbsent(key, map);
			if (oldMap != null) {
				map = oldMap;
			}
		}

		CopyOnWriteArrayList<ManagerListener> listenerList = map
				.get(instanceId);
		if (listenerList == null) {
			listenerList = new CopyOnWriteArrayList<ManagerListener>();
			CopyOnWriteArrayList<ManagerListener> oldList = map.putIfAbsent(
					instanceId, listenerList);
			if (oldList != null) {
				listenerList = oldList;
			}
		}
		listenerList.addAll(addListeners);
	}

	private String makeKey(String dataId, String group) {
		if (StringUtils.isEmpty(group)) {
			group = Constants.DEFAULT_GROUP;
		}
		return dataId + "_" + group;
	}

}
