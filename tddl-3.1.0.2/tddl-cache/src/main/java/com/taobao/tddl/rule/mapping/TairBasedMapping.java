package com.taobao.tddl.rule.mapping;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.common.tair.DataEntry;
import com.taobao.common.tair.Result;
import com.taobao.common.tair.ResultCode;
import com.taobao.common.tair.TairManager;
import com.taobao.common.tair.impl.DefaultTairManager;
import com.taobao.tddl.common.Monitor;


/**
 * tair映射规则
 * 在database规则的基础上。
 * 因为tair的特航速要求进行了修改。
 * 
 * 因为以下特点：
 * 1.tair 要求使用简单对象，因此不打算使用java bean对象来存放数据。而只打算使用基本对象或String。
 * 2.为了减小复杂性，所以保证数据存入tair后就永远不修改。
 * 但因为在实际应用中有key->(value1,value2）,这样的映射关系，从理论上来说
 * value1和value2都有可能是null,因此必须保证在value1,value2都不为空的情况下，才写tair.
 * 3.存入tair的数据类似 value1|value2|...的String对象。但如果是只有一个value要存入tair,那么不会用String进行包装
 * 4.根据指定的index 获取value1String,然后根据预先设定好的typeHandler进行type转换处理。
 * 5.columns规则类似 col1|int,col2|long
 *  
 * @author shenxun
 *
 */
public class TairBasedMapping extends DatabaseBasedMapping{
	private final Log log = LogFactory.getLog(TairBasedMapping.class);
	private static final int DEFAULT_VALUE = -1;
	/**
	 * 内部临时使用的null holder,为了防止从数据库中查出来的value真的很不幸的是"null"这个字段。
	 * 反馈回去为null就不对了。。
	 */
	private static final String NULL_PLACEHOLDER = "TDDL_NULL_PLACE_HOLDER";
	public static final int DEFAULT_NAMESPACE = -1000;
	
	/*for tair */
	private List<String> tairConfigServers = new ArrayList<String>(2);
	private String groupName;
	private String charSet;
	private int compressionThreshold = DEFAULT_VALUE;
	private int maxWaitThread = DEFAULT_VALUE;
	private int timeout = DEFAULT_VALUE;
	private TairManager tairManager;
	
	private int namespace = DEFAULT_NAMESPACE;
	
//	 Map<String/*target key*/, TypeHandlerEntry> typeHandlerMap;

	@Override
	public void initInternal() {
			if(this.tairManager == null){
				if(namespace == DEFAULT_NAMESPACE){
					throw new IllegalArgumentException("未指定namespace");
				}
				log.warn("tddl init tair manager , tairConfigServers is "+ tairConfigServers
						+" group name is "+groupName);
				DefaultTairManager tairManager = new DefaultTairManager();
				tairManager.setConfigServerList(tairConfigServers);
				tairManager.setGroupName(groupName);
				if(charSet != null)
					tairManager.setCharset(charSet);
				if(compressionThreshold != DEFAULT_VALUE)
					tairManager.setCompressionThreshold(compressionThreshold);
				if(maxWaitThread != DEFAULT_VALUE)
					tairManager.setMaxWaitThread(maxWaitThread);
				if(timeout != DEFAULT_VALUE)
					tairManager.setTimeout(timeout);
				
				tairManager.init();
				log.warn("inited");
				this.tairManager = tairManager;
			}

		super.initInternal();
	}
	
	
	/**
	 * 构造为tair准备的一段字符串，如果取到的数据有null，则hasNullValue为true;
	 * 如 map : key1 -> val1 ,key2 -> null;
	 * 那么hasNullValue = true,tokenForTair = val1
	 * @author shenxun
	 *
	 */
	static class TokenForTairResult{
		/**
		 * 是否有空值
		 */
		boolean hasNullVale;
		/**
		 * 可能是基本类型或String,如果基本类型或String拼装的字段中没有null值
		 * 则会写入到tair中，否则只是用于后续映射处理。
		 */
		Serializable tokenForTair;
	}
	
	protected Object get(String targetKey, String sourceKey, Object sourceValue) {
		
		Object cacheres = getTargetValueFromTair(sourceValue);
		
		if(cacheres == null){
			Monitor.add(Monitor.KEY1, sourceKey, Monitor.KEY3_TAIR_HIT_RATING,0,1);
			log.debug("tair doesn't have spec value,get from database;");
			//如果没取到数据，那么从数据库中取数据
			Map<String, Object>  map = getResultMap(sourceKey, sourceValue,targetKey);
			log.debug("value from database is :"+map);
			TokenForTairResult tokenForTair = getValueForTairCache(map);
			putIntoTairCache(sourceValue, tokenForTair);
			cacheres = tokenForTair.tokenForTair;
		}else{
			Monitor.add(Monitor.KEY1, sourceKey, Monitor.KEY3_TAIR_HIT_RATING,1,1);
		}
		return translate(targetKey, cacheres);
	}

	/**
	 * 从tair中取到的数据，用targetKey获取对应的index的数据，然后转意
	 * 真正的数据
	 * @param targetKey
	 * @param cacheres
	 * @return
	 */
	Object translate(String targetKey, Object cacheres) {
		if(cacheres == null){
			//处理单个列，直接存基本类型时的情况，如果遇到null则直接返回null
			return null;
		}
		if(!(cacheres instanceof String)){
			//处理基本类型
			return cacheres;
		}
		//处理不为null，多个列的情况，包含拼装字段后的123|TDDL_NULL_PLACE_HOLDER
		String str = String.valueOf(cacheres);
		String[] targetValues = str.split("\\|");
		if(targetValues.length != typeHandlerMap.size()){
			log.error("source values do not equal type Handler map size ." +
					",target value in tair is " + str+" map size is" + typeHandlerMap.size());
			return null;
		}
		TypeHandlerEntry typeHandlerEntry = typeHandlerMap.get(targetKey);
		if(typeHandlerEntry == null){
			log.error("cant find type handler by targetKey :" + targetKey+" .type handler map is"+typeHandlerMap);
			return null;
		}
		String targetValue = targetValues[typeHandlerEntry.index];
		if(NULL_PLACEHOLDER.equals(targetValue)){
			return null;
		}else{
			return typeHandlerEntry.typeHandler.process(targetValue);
		}
	}
	
	private void putIntoTairCache(Object sourceValue, TokenForTairResult tokenForTair) {
		ResultCode rs=null;
		try {
			if(!tokenForTair.hasNullVale){
				rs=this.tairManager.put(namespace, sourceValue, tokenForTair.tokenForTair);
				if(!rs.isSuccess()){
					log.error("put key "+sourceValue.toString()+" "+rs.getMessage());
				}
			}else{
				log.info("null value was detected, give up to write to tair cache" +
						", It will not be a  problem until sourceValue repeat to many times ,current sourceValue is"+sourceValue+" token for tair is "+tokenForTair.tokenForTair);
			}
		} catch (Exception e) {
			log.error(sourceValue.toString(), e);
		}
	}
	
	/**
	 * 获取数据库中的数据在tair里存放的样式
	 * 
	 * val1|val2|val3
	 * 
	 * @param map
	 * @return
	 * @throws InterruptedException
	 */
	TokenForTairResult getValueForTairCache(Map<String, Object> map){
		TokenForTairResult tokenForTairResult = new TokenForTairResult();
	
	
		if(columns.length > 1){
			StringBuilder sb = new StringBuilder();
			boolean firstElement = true;
			for(String column: super.columns){
				if(firstElement){
					firstElement = false;
				}else{
					sb.append("|");
				}
				
				if(appendColumnStr(map, sb, column)){
					tokenForTairResult.hasNullVale = true;
				}
			}
			
			tokenForTairResult.tokenForTair  = sb.toString();
		}else if(columns.length == 1){
			/*
			 * 处理columns == 1 .<1的不可能出现，columns.length不可能小于0，等于0则initRule会异常
			 * columns =1时，不强转为String处理，直接使用原有的Serializable来处理。
			 */
			String column  = columns[0];
			Object value = map.get(column);
			if(value == null){
				tokenForTairResult.hasNullVale = true;
			}else{
				try {
					tokenForTairResult.tokenForTair  = (Serializable) value;
				} catch (ClassCastException e) {
					log.error("target column "+column + " , value is "+value);
				}
			}
			
		}else{
			//处理可能的没想到的情况
			throw new IllegalStateException("should not be here");
		}
	
		return tokenForTairResult;
	}
	
	boolean appendColumnStr(Map<String, Object> map, StringBuilder sb,
			String column) {
		String tempColumn = null;
		boolean hasNullValue = false;
		Object tempValue = map.get(column);
		if(tempValue == null){
			hasNullValue = true;
		}
		if(tempValue == null){
			tempColumn = NULL_PLACEHOLDER;
		}else{
			tempColumn = tempValue.toString();
		}
		sb.append(tempColumn);
		
		return hasNullValue;
	}
	 Object getTargetValueFromTair(Object sourceValue) {
		Object cacheres = null;
		try {

			Result<DataEntry> result = this.tairManager.get(namespace,sourceValue);

			if (result!=null&&result.isSuccess()) {

				
				DataEntry dataEntry = result.getValue();
				//当result!=null时，并不能保证dataEntry不为null,还是需要判断
				if(dataEntry!=null){
				 cacheres = dataEntry.getValue();						
				}
				log.debug("get from tair success ; cacheres is "+ cacheres);
			}else{
				log.error("get key" + sourceValue + (result == null ? "" : result.getRc().getMessage()));
			}

		} catch (Exception e) {
			log.error(sourceValue, e);
		}
		return cacheres;
	}
	public int getNamespace() {
		return namespace;
	}
	public void setNamespace(int namespace) {
		this.namespace = namespace;
	}

	public TairManager getTairManager() {
		return tairManager;
	}

	public void setTairManager(TairManager tairManager) {
		this.tairManager = tairManager;
	}

	public List<String> getTairConfigServers() {
		return tairConfigServers;
	}

	public void setTairConfigServers(List<String> tairConfigServers) {
		this.tairConfigServers = tairConfigServers;
	}

	public void setCommaTairConfigServers(String commaTairConfigServers) {
		this.tairConfigServers = Arrays.asList(commaTairConfigServers.split(","));
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getCharSet() {
		return charSet;
	}

	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}

	public int getCompressionThreshold() {
		return compressionThreshold;
	}

	public void setCompressionThreshold(int compressionThreshold) {
		this.compressionThreshold = compressionThreshold;
	}

	public int getMaxWaitThread() {
		return maxWaitThread;
	}

	public void setMaxWaitThread(int maxWaitThread) {
		this.maxWaitThread = maxWaitThread;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public Map<String, TypeHandlerEntry> getTypeHandlerMap() {
		return typeHandlerMap;
	}

	public void setTypeHandlerMap(Map<String, TypeHandlerEntry> typeHandlerMap) {
		this.typeHandlerMap = typeHandlerMap;
	}
	
	
}
