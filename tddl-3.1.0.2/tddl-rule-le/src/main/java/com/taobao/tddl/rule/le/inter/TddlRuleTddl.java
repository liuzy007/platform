//Copyright(c) Taobao.com
package com.taobao.tddl.rule.le.inter;

import java.util.List;
import java.util.Map;

import com.taobao.tddl.interact.bean.ComparativeMapChoicer;
import com.taobao.tddl.interact.bean.MatcherResult;
import com.taobao.tddl.interact.rule.VirtualTableRoot;
import com.taobao.tddl.interact.rule.bean.SqlType;
import com.taobao.tddl.rule.le.exception.ResultCompareDiffException;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a> 
 * @version 1.1
 * @since 1.6
 * @date 2011-5-4下午06:54:24
 * 
 * 增加两个接口，用于支持Join优化和broadcast.
 * @author Whisper
 */
public interface TddlRuleTddl {
	/**
	 * 如果当前表是一个广播表/广播Index。
	 * 广播表有两种类型，
	 * 	一种是小表全表复制到所有数据节点。
	 * 	一种是主键表按照另外一个维度做了复制。
	 * 
	 * 这两种情况下，处理的逻辑应该类似，
	 * 都是首先判断是否是个非一致性读。
	 * 如果是，那么按照参与join的其他表/index来决定数据的分布。
	 * 
	 * @return true 如果是个需要broadCast的逻辑表或逻辑索引名
	 */
	boolean isBroadCast(String logicName);
	
	/**
	 * 用于判断这组表是否使用了相同的切分规则
	 * 
	 * @param leftLogicName
	 * @param rightLogicName
	 * @return
	 */
	boolean isInTheSameJoinGroup(String leftLogicName,String rightLogicName);
	
	/**
	 * 用于判断这组表是否使用了相同的切分规则
	 * 
	 * @param logicNames
	 * @return
	 */
	boolean isInTheSameJoinGroup(List<String> logicNames);
	
	/**
	 * 简单单套规则支持(TDDL使用)
	 * @param vtab
	 * @param condition
	 * @return
	 */
	public MatcherResult route(String vtab,ComparativeMapChoicer choicer,List<Object> args,boolean needSourceKey);
	
	/**
	 * 多套规则支持(TDDL使用)
	 * @param vtab
	 * @param condition
	 * @return
	 */
	public Map<String, MatcherResult> routeMVer(
			String vtab,
			ComparativeMapChoicer choicer,List<Object> args,boolean needSourceKey);
	
	/**
	 * 指定一套规则计算
	 * @param vtab
	 * @param condition
	 * @return
	 */
	public MatcherResult route(String vtab,ComparativeMapChoicer choicer,List<Object> args,boolean needSourceKey,VirtualTableRoot specifyVtr);
	
	/**
	 * 新旧规则计算并比较,不带目标库判定
	 * 
	 * @param vtab
	 * @param conditionStr
	 * @return
	 */
	public MatcherResult routeMVerAndCompare(SqlType sqlType,
			String vtab, ComparativeMapChoicer choicer,List<Object> args,boolean needSourceKey)throws ResultCompareDiffException;
	
	/**
	 * 新旧规则计算并比较,带目标库判定
	 * 
	 * @param vtab
	 * @param conditionStr
	 * @return
	 */
	public MatcherResult routeMVerAndCompare(SqlType sqlType,
			String vtab,  ComparativeMapChoicer choicer,List<Object> args,boolean needSourceKey,String oriDb,String oriTable)throws ResultCompareDiffException; 
}
