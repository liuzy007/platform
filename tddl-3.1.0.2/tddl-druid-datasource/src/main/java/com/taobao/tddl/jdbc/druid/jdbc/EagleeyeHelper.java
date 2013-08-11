package com.taobao.tddl.jdbc.druid.jdbc;

import java.lang.reflect.Field;

import com.taobao.eagleeye.EagleEye;
import com.taobao.tddl.jdbc.druid.config.object.DruidDsConfDO;

/**
 * @author jiechen.qzm
 * Eagleeye帮助类，协助记录查询时间
 */
public class EagleeyeHelper {

	/**
	 * 获取本次execute的数据源信息
	 * @param datasourceWrapper
	 * @param sqlType
	 * @throws Exception
	 */
	public static void startRpc(DruidDsConfDO runTimeConf, String sqlType){
		String ip = runTimeConf.getIp();
		String port = runTimeConf.getPort();
		String dbName = runTimeConf.getDbName();
		String serviceName = "TDDL-" + dbName + "-" + ip + "-" + port;
		String methodName = sqlType.toString();
		EagleEye.startRpc(serviceName, methodName);
	}

	/**
	 * execute之前写日志
	 */
	public static void annotateRpcBeforeExecute(){
		EagleEye.annotateRpc(EagleEye.TAG_CLIENT_SEND);
	}

	/**
	 * execute之后写日志
	 */
	public static void annotateRpcAfterExecute(){
		EagleEye.annotateRpc(EagleEye.TAG_CLIENT_RECV);
	}
}
