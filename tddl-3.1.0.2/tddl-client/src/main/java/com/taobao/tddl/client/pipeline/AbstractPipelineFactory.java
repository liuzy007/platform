//Copyright(c) Taobao.com
package com.taobao.tddl.client.pipeline;

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.tddl.client.ThreadLocalString;
import com.taobao.tddl.client.dispatcher.SqlDispatcher;
import com.taobao.tddl.client.util.ThreadLocalMap;
import com.taobao.tddl.common.SQLPreParser;
import com.taobao.tddl.interact.rule.bean.DBType;
import com.taobao.tddl.interact.rule.bean.SqlType;
import com.taobao.tddl.parser.ParserCache;
import com.taobao.tddl.rule.bean.LogicTable;
import com.taobao.tddl.rule.ruleengine.util.StringUtils;
import com.taobao.tddl.util.IDAndDateCondition.routeCondImp.DirectlyRouteCondition;

/**
 * @description 抽象类,提供sqlDispatcher选择,sql类型选择,sql预解析和表级别
 *              数据库类型选择 等功能,继承PipelineFactory接口
 *              DefaultPipelineFactory和NewRulePipelineFactory继承此类,
 *              需要实现getPipeline()方法.
 *              
 *              自定义的PipelineFactory实现这个抽象类,以便提供自定义handler的
 *              pipeline
 * 
 * @author <a href="junyu@taobao.com">junyu</a>
 * @version 2.4.3
 * @since 1.6
 * @date 2010-08-15下午03:24:42
 */
public abstract class AbstractPipelineFactory implements PipelineFactory {
	public static final Log logger=LogFactory.getLog(AbstractPipelineFactory.class);
	protected SqlDispatcher defaultDispatcher;
	protected Map<String, SqlDispatcher> dispatcherMap;
	
	private static final Pattern SELECT_FOR_UPDATE_PATTERN = Pattern.compile(
			"^select\\s+.*\\s+for\\s+update.*$", Pattern.CASE_INSENSITIVE);
	
	private static final ParserCache globalCache = ParserCache.instance();
	
	public abstract Pipeline getPipeline();

	public SqlDispatcher selectSqlDispatcher(String selectKey)
			throws SQLException {
		if (selectKey == null) {
			return defaultDispatcher;
		}
		SqlDispatcher sqlDispatcher = dispatcherMap.get(selectKey);
		if (sqlDispatcher == null) {
			throw new IllegalArgumentException("can't find selector by key :"
					+ selectKey);
		} else {
			return sqlDispatcher;
		}
	}

	public void setDefaultDispatcher(SqlDispatcher defaultDispatcher) {
		this.defaultDispatcher = defaultDispatcher;
	}

	public void setDispatcherMap(Map<String, SqlDispatcher> dispatcherMap) {
		this.dispatcherMap = dispatcherMap;
	}
	
	/**
	 * 获得SQL语句种类
	 * 
	 * @param sql
	 *            SQL语句
	 * @throws SQLException
	 *             当SQL语句不是SELECT、INSERT、UPDATE、DELETE语句时，抛出异常。
	 */
	public static SqlType getSqlType(String sql) throws SQLException {
		SqlType sqlType = globalCache.getSqlType(sql);
		if (sqlType == null) {
			//#bug 2011-12-8,modify by junyu ,this code use huge cpu resource,and most 
            //sql have no comment,so first simple look for there whether have the comment
            String noCommentsSql=sql;
            if(sql.contains("/*")){
                  noCommentsSql = StringUtils.stripComments(sql, "'\"", "'\"", true, false, true, true).trim();
            }

			if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "select")) {
				//#bug 2011-12-9,this select-for-update regex has low performance,so
				//first judge this sql whether have ' for ' string.
				if (noCommentsSql.toLowerCase().contains(" for ")&&SELECT_FOR_UPDATE_PATTERN.matcher(noCommentsSql).matches()) {
					sqlType = SqlType.SELECT_FOR_UPDATE;
				} else {
					sqlType = SqlType.SELECT;
				}
			} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql,
					"insert")) {
				sqlType = SqlType.INSERT;
			} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql,
					"update")) {
				sqlType = SqlType.UPDATE;
			} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql,
					"delete")) {
				sqlType = SqlType.DELETE;
			} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql,
					"replace")) {
				sqlType = SqlType.REPLACE;
			} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, 
					"truncate")){ 
			    sqlType = SqlType.TRUNCATE;
			} else {
				throw new SQLException(
						"only select, insert, update, delete, replace,truncate sql is supported");
			}
			sqlType = globalCache.setSqlTypeIfAbsent(sql, sqlType);
		}
		return sqlType;
	}
	
	public DirectlyRouteCondition sqlPreParse(String sql) throws SQLException {
		//如果用户指定了ROUTE_CONDITION或者DB_SELECTOR，那么跳过预解析，防止干扰
		if (null != ThreadLocalMap.get(ThreadLocalString.ROUTE_CONDITION)
				|| null != ThreadLocalMap.get(ThreadLocalString.DB_SELECTOR)
				|| null != ThreadLocalMap.get(ThreadLocalString.RULE_SELECTOR)) {
			return null;
		}

		String firstTable = SQLPreParser.findTableName(sql);
		if (null != firstTable) {
			Map<String, LogicTable> logicTableMap = this.defaultDispatcher
					.getRoot().getLogicTableMap();
			if(null!=logicTableMap.get(firstTable)){
				return null;
			}	
		}

		logger.debug("no logic table in defaultDispather's logicTableMap,try to produce DirectlyRouteCondition");
		
		if(null==this.defaultDispatcher.getRoot()
				.getDefaultDBSelectorID()){
		    throw new SQLException("the defaultDispatcher have no defaultDbIndex");	
		}
		
		//表名不存在logicTable map中，那么生成Condition，指定执行dbIndex返回
		DirectlyRouteCondition condition = new DirectlyRouteCondition();
		condition.setDBId(this.defaultDispatcher.getRoot()
				.getDefaultDBSelectorID());
		
		return condition;
	}
	
	public DBType decideDBType(String sql,SqlDispatcher sqlDispatcher)throws SQLException{
		String firstTable = SQLPreParser.findTableName(sql);
		if (null != firstTable) {
			Map<String, LogicTable> logicTableMap = sqlDispatcher.getRoot().getLogicTableMap();
			DBType findInLogicTab=null;
			if(null!=logicTableMap.get(firstTable)){
				findInLogicTab=logicTableMap.get(firstTable).getDbType();
			}
			
			return findInLogicTab;
		}
		
		return (DBType) sqlDispatcher.getRoot().getDBType();
	}
}
