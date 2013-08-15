//Copyright(c) Taobao.com
package com.taobao.tddl.rule.le;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.taobao.tddl.interact.bean.ComparativeMapChoicer;
import com.taobao.tddl.interact.monitor.TotalStatMonitor;
import com.taobao.tddl.interact.rule.VirtualTableRoot;
import com.taobao.tddl.rule.le.bean.RuleChangeListener;
import com.taobao.tddl.rule.le.config.ConfigDataHandler;
import com.taobao.tddl.rule.le.config.ConfigDataHandlerFactory;
import com.taobao.tddl.rule.le.config.ConfigDataListener;
import com.taobao.tddl.rule.le.config.impl.DefaultConfigDataHandlerFactory;
import com.taobao.tddl.rule.le.util.StringXmlApplicationContext;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a>
 * @version 1.0
 * @since 1.6
 * @date 2011-4-21下午01:15:22
 */
public class TddlRuleConfig {
	protected static Log logger = LogFactory.getLog(TddlRuleConfig.class);
	private static final String NONE_RULE_VERSION = null;
	private static final String TDDL_RULE_LE_PREFIX = "com.taobao.tddl.rule.le.";
	private static final String TDDL_RULE_LE_VERSIONS_FORMAT = "com.taobao.tddl.rule.le.{0}.versions";

	private String appName;
	private volatile ConfigDataHandlerFactory cdhf;

	// 本地规则
	private String appRuleFile;
	private String appRuleString;

	// 多套规则(动态推)
	private ConfigDataHandler versionHandler;
	private Map<String, ConfigDataHandler> ruleHandlers = new HashMap<String, ConfigDataHandler>();
	protected Map<String, VirtualTableRoot> vtrs = new HashMap<String, VirtualTableRoot>();
	protected Map<String, String> ruleStrs = new HashMap<String, String>();
	private List<RuleChangeListener> listeners = new ArrayList<RuleChangeListener>();

	/**
	 * key = 0(old),1(new),2,3,4... value= version
	 */
	protected Map<Integer, String> versionIndex = new HashMap<Integer, String>();
	private Map<String, AbstractXmlApplicationContext> oldCtxs = new HashMap<String, AbstractXmlApplicationContext>();

	// 单套规则(动态推+本地规则)
	protected VirtualTableRoot vtr = null;
	protected String ruleStr = null;
	private AbstractXmlApplicationContext oldCtx;
	
	protected volatile Map<String, Set<String>> shardColumnCache = new HashMap<String, Set<String>>();

	public static final TotalStatMonitor statMonitor = TotalStatMonitor
			.getInstance();

	private ClassLoader outerClassLoader = null;

	public void init() {
		// 启动日志
		statMonitor.setAppName(appName);
		statMonitor.start();

		if (appRuleFile != null) {
			String[] rulePaths = appRuleFile.split(";");
			if(rulePaths.length == 1 && !rulePaths[0].matches("^V[0-9]*#.+$")) {
				ApplicationContext ctx = new ClassPathXmlApplicationContext(
						appRuleFile) {
					@Override
					public ClassLoader getClassLoader() {
						if (outerClassLoader == null) {
							return super.getClassLoader();
						} else {
							return outerClassLoader;
						}
					}
				};
				vtr = (VirtualTableRoot) ctx.getBean("vtabroot");
			}
			else {
				for(int i = 0 ; i < rulePaths.length ; i ++) {
					if(rulePaths[i].matches("^V[0-9]*#.+$")){
						continue;
					}
					else {
						throw new RuntimeException("rule file path \"" + rulePaths[i] + " \" does not fit the pattern!");
					}
				}

				Map<Integer, String> tempIndexMap = new HashMap<Integer, String>();
				for(int i = 0 ; i < rulePaths.length ; i ++) {
					String rulePath = rulePaths[i];
					String[] temp = rulePath.split("#");

					ApplicationContext ctx = new ClassPathXmlApplicationContext(
							temp[1]) {
						@Override
						public ClassLoader getClassLoader() {
							if (outerClassLoader == null) {
								return super.getClassLoader();
							} else {
								return outerClassLoader;
							}
						}
					};
					tempIndexMap.put(i, temp[0]);
					vtrs.put(temp[0], (VirtualTableRoot) ctx.getBean("vtabroot"));
				}
				this.versionIndex = tempIndexMap;
			}
		} else if (appRuleString != null) {
			StringXmlApplicationContext ctx = new StringXmlApplicationContext(
					appRuleString, outerClassLoader);
			vtr = (VirtualTableRoot) ctx.getBean("vtabroot");
			ruleStr=appRuleString;
		} else if (appName != null) {
			String versionsDataId = new MessageFormat(
					TDDL_RULE_LE_VERSIONS_FORMAT)
					.format(new Object[] { appName });
			cdhf = new DefaultConfigDataHandlerFactory();
			versionHandler = cdhf.getConfigDataHandler(versionsDataId,
					new VersionsConfigListener());
			String versionData = versionHandler.getData(10 * 1000,
					ConfigDataHandler.FIRST_CACHE_THEN_SERVER_STRATEGY);

			if (versionData == null) {
				String dataId = TDDL_RULE_LE_PREFIX + appName;
				if(!dataSub(dataId, NONE_RULE_VERSION,new SingleRuleConfigListener())){
					throw new RuntimeException("subscribe the rule data or init rule error!check the error log!");
				}
			} else {
				String[] versions = versionData.split(",");
				int index = 0;
				Map<Integer, String> tempIndexMap = new HashMap<Integer, String>();
				for (String version : versions) {
					String dataId = TDDL_RULE_LE_PREFIX + appName + "."
							+ version;
					if(!dataSub(dataId, version, new SingleRuleConfigListener())){
						throw new RuntimeException("subscribe the rule data or init rule error!check the error log! the rule version is:"+version);
					}
					tempIndexMap.put(index, version);
					index++;
				}
				this.versionIndex = tempIndexMap;
				// 记下日志,方便分析
				TotalStatMonitor.recieveRuleLog(versionData);
			}
		}
	}

	public String getOldRuleStr() {
		if (this.ruleStrs != null && this.ruleStrs.size() > 0) {
			String ruleStr = this.ruleStrs.get(versionIndex.get(0));
			return ruleStr;
		} else if (this.ruleStr != null) {
			return this.ruleStr;
		} else {
			throw new RuntimeException("规则对象为空!请检查diamond上是否存在动态规则!");
		}
	}

	private boolean dataSub(String dataId, String version,
			ConfigDataListener listener) {
		ConfigDataHandler ruleHandler = cdhf.getConfigDataHandler(dataId,
				listener);
		if (version != null) {
			this.ruleHandlers.put(version, ruleHandler);
		}

		String data = null;
		try {
			data = ruleHandler.getData(10 * 1000,
					ConfigDataHandler.FIRST_CACHE_THEN_SERVER_STRATEGY);
		} catch (Exception e) {
			logger.error(e);
			return false;
		}

		if (data == null) {
			logger.error("use diamond rule config,but recieve no config at all!");
            return false;
		}

		return configInit(data, version);
	}

	private synchronized boolean configInit(String data, String version) {
		StringXmlApplicationContext ctx=null;
		try {
			//this rule may be wrong rule,don't throw it but log it,
			//and will not change the vtr!
			ctx = new StringXmlApplicationContext(data,
					outerClassLoader);
		} catch (Exception e) {
			logger.error(e);
			return false;
		}

		VirtualTableRoot tempvtr = (VirtualTableRoot) ctx.getBean("vtabroot");
		if (version != null&&tempvtr!=null) {
			// 直接覆盖
			this.vtrs.put(version, tempvtr);
			this.ruleStrs.put(version, data);
			oldCtx = this.oldCtxs.get(version);
			// 销毁旧有容器
			if (oldCtx != null) {
				oldCtx.close();
			}
			this.oldCtxs.remove(version);
			this.oldCtxs.put(version, ctx);
		} else if(tempvtr!=null) {
			this.vtr = tempvtr;
			this.ruleStr = data;
			if (oldCtx != null) {
				oldCtx.close();
			}
			oldCtx = (AbstractXmlApplicationContext) ctx;
		} else{
			//common not be here!
			logger.error("rule no vtabroot!!");
			return false;
		}
		return true;
	}

	protected ComparativeMapChoicer generateComparativeMapChoicer(
			String conditionStr) {
		SimpleComparativeMapChoicer mc = new SimpleComparativeMapChoicer();
		mc.addComparatives(conditionStr);
		return mc;
	}

	public void setAppRuleFile(String appRuleFile) {
		this.appRuleFile = appRuleFile;
	}

	public void setAppRuleString(String appRuleString) {
		this.appRuleString = appRuleString;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setOuterClassLoader(ClassLoader outerClassLoader) {
		this.outerClassLoader = outerClassLoader;
	}

	public void addRuleChangeListener(RuleChangeListener listener) {
		this.listeners.add(listener);
	}

	private class SingleRuleConfigListener implements ConfigDataListener {
		public void onDataRecieved(String dataId, String data) {
			if (data != null && !data.equals("")) {
				StringBuilder sb = new StringBuilder("recieve data!dataId:");
				sb.append(dataId);
				sb.append(" data:");
				sb.append(data);
				logger.info(sb.toString());

				String prefix = TDDL_RULE_LE_PREFIX + appName + ".";
				int i = dataId.indexOf(prefix);
				if (i < 0) {
					//non-versioned rule
					if(configInit(data, null)){
						for (RuleChangeListener listener : listeners) {
							try {
								//may be wrong,so try catch it ,not to affect other!
								listener.onRuleRecieve(data);
							} catch (Exception e) {
								logger.error("one listener error!",e);
							}
						}
					}
				} else {
					String version = dataId.substring(i + prefix.length());
					if(configInit(data, version)){
						for (RuleChangeListener listener : listeners) {
							try {
								//may be wrong,so try catch it ,not to affect other!
								listener.onRuleRecieve(getOldRuleStr());
							} catch (Exception e) {
								logger.error("one listener error!",e);
							}
						}
					}
				}
			}
		}
	}

	private class VersionsConfigListener implements ConfigDataListener {
		public void onDataRecieved(String dataId, String data) {
			if (data != null && !data.equals("")) {
				StringBuilder sb = new StringBuilder(
						"recieve versions data!dataId:");
				sb.append(dataId);
				sb.append(" data:");
				sb.append(data);
				logger.info(sb.toString());

				String[] versions = data.split(",");
				Map<String, String> checkMap = new HashMap<String, String>();
				// 添加新增的规则订阅
				int index = 0;
				Map<Integer, String> tempIndexMap = new HashMap<Integer, String>();
				for (String version : versions) {
					//FIXME:change the rule,may be wrong,this is a problem
					if (ruleHandlers.get(version) == null) {
						String ruleDataId = TDDL_RULE_LE_PREFIX + appName + "."
								+ version;
						dataSub(ruleDataId, version,
								new SingleRuleConfigListener());
					}
					checkMap.put(version, version);
					tempIndexMap.put(index, version);
					index++;
				}

				// 删除没有在version中存在的订阅
				List<String> needRemove = new ArrayList<String>();
				for (Map.Entry<String, ConfigDataHandler> handler : ruleHandlers
						.entrySet()) {
					if (checkMap.get(handler.getKey()) == null) {
						needRemove.add(handler.getKey());
					}
				}

				// 清理
				for (String version : needRemove) {
					ConfigDataHandler handler = ruleHandlers.get(version);
					handler.closeUnderManager();
					ruleHandlers.remove(version);
					vtrs.remove(version);
					ruleStrs.remove(version);
					oldCtxs.get(version).close();
					oldCtxs.remove(version);
				}
				versionIndex = tempIndexMap;

				// 在versions data收到为null,或者为空,不调用,保护AppServer
				// 调用listener,但只返回位列第一个的VirtualTableRoot
				for (RuleChangeListener listener : listeners) {
					try {
						//may be wrong,so try catch it ,not to affect other!
						listener.onRuleRecieve(getOldRuleStr());
					} catch (Exception e) {
						logger.error("one listener error!",e);
					}
				}
				
				shardColumnCache.clear();
			}

			// 记下日志,方便分析
			TotalStatMonitor.recieveRuleLog(data);
		}
	}
}
