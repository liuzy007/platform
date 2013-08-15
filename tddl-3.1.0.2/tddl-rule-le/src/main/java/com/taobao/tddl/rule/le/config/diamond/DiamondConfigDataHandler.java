package com.taobao.tddl.rule.le.config.diamond;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.impl.DefaultDiamondManager;
import com.taobao.tddl.rule.le.config.ConfigDataHandler;
import com.taobao.tddl.rule.le.config.ConfigDataListener;

/**
 * @author shenxun
 * @author <a href="zylicfc@gmail.com">junyu</a>
 * @version 1.0
 * @since 1.6
 * @date 2011-1-11上午11:22:29
 * @desc 持久配置中心diamond实现
 */
public class DiamondConfigDataHandler implements ConfigDataHandler {
	private static final Log logger = LogFactory
			.getLog(DiamondConfigDataHandler.class);
	private DiamondManager diamondManager;
	private String dataId;

	public void init(final String dataId,
			final List<ConfigDataListener> configDataListenerList,
			final Map<String, Object> config) {
		DiamondConfig.handleConfig(config);
		DefaultDiamondManager.Builder builder = new DefaultDiamondManager.Builder(
				dataId, new ManagerListener() {
					public void receiveConfigInfo(String date) {
						for (ConfigDataListener configDataListener : configDataListenerList) {
							configDataListener.onDataRecieved(dataId, date);
						}
					}

					public Executor getExecutor() {
						return (Executor) config.get("executor");
					}
				});
		String group = (String) config.get("group");
		if (null != group) {
			builder.setGroup(group);
		}
		this.diamondManager = builder.build();
		this.dataId = dataId;
	}

	public String getData(long timeout, String strategy) {
		if (strategy != null
				&& strategy
						.equals(ConfigDataHandler.FIRST_CACHE_THEN_SERVER_STRATEGY)) {
			return diamondManager.getAvailableConfigureInfomation(timeout);
		} else if (strategy != null
				&& strategy.equals(ConfigDataHandler.FIRST_SERVER_STRATEGY)) {
			return diamondManager.getConfigureInfomation(timeout);
		} else {
			return null;
		}
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
						logger.error("one of listener failed", e);
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
