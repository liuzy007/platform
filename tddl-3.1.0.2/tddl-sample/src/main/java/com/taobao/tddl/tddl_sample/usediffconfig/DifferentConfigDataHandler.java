//Copyright(c) Taobao.com
package com.taobao.tddl.tddl_sample.usediffconfig;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.impl.DefaultDiamondManager;
import com.taobao.tddl.common.config.ConfigDataHandler;
import com.taobao.tddl.common.config.ConfigDataListener;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a> 
 * @version 1.0
 * @since 1.6
 * @date 2011-1-13涓嬪崍03:25:45
 */
public class DifferentConfigDataHandler implements ConfigDataHandler {
	private static final Log logger=LogFactory.getLog(DifferentConfigDataHandler.class);
	private DiamondManager diamondManager;
	private String dataId;

	public void init(final String dataId,final List<ConfigDataListener> listenerList,
			 final Map<String,Object> config) {
		DefaultDiamondManager.Builder builder = new DefaultDiamondManager.Builder(
				dataId, new ManagerListener() {
					public void receiveConfigInfo(String date) {
						for(ConfigDataListener configDataListener:listenerList){
						configDataListener.onDataRecieved(dataId, date);
						}
					}

					public Executor getExecutor() {
						return (Executor) config.get("executor");
					}
				});
		String group=(String) config.get("group");
		if(null!=group){
			builder.setGroup(group);
		}
		this.diamondManager=builder.build();
		this.dataId=dataId;
	}

	public String getData(long timeout,String strategy) {
		return diamondManager.getConfigureInfomation(timeout);
	}

	public void addListener(final ConfigDataListener configDataListener,
			final Executor executor) {
		diamondManager.setManagerListener(new ManagerListener() {
			public void receiveConfigInfo(String date) {
				configDataListener.onDataRecieved(dataId, date);
			}

			public Executor getExecutor() {
				return executor;
			}
		});
	}

	public void addListeners(
			final List<ConfigDataListener> configDataListenerList,
			final Executor executor) {
		diamondManager.setManagerListener(new ManagerListener() {
			public void receiveConfigInfo(String date) {
				for (ConfigDataListener configDataListener : configDataListenerList) {
					try {
						configDataListener.onDataRecieved(dataId, date);
					} catch (Exception e) {
						logger.error("one of listener failed",e);
						continue;
					}
				}
			}

			public Executor getExecutor() {
				return executor;
			}
		});
	}

	public void closeUnderManager() {
		diamondManager.close();
	}
}
