//Copyright(c) Taobao.com
package com.taobao.tddl.rule.le;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taobao.tddl.interact.bean.ComparativeMapChoicer;
import com.taobao.tddl.interact.bean.MatcherResult;
import com.taobao.tddl.interact.rule.VirtualTable;
import com.taobao.tddl.interact.rule.VirtualTableRoot;
import com.taobao.tddl.interact.rule.VirtualTableRuleMatcher;
import com.taobao.tddl.interact.rule.bean.SqlType;
import com.taobao.tddl.rule.le.exception.ResultCompareDiffException;
import com.taobao.tddl.rule.le.extend.MatchResultCompare;
import com.taobao.tddl.rule.le.inter.TddlRuleTddl;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a>
 * @version 1.0
 * @since 1.6
 * @date 2011-4-21下午01:39:37
 */
public class TddlRuleInner extends TddlRuleConfig implements TddlRuleTddl {
	private final VirtualTableRuleMatcher matcher = new VirtualTableRuleMatcher();

	/**
	 * 简单单套规则支持(TDDL使用)
	 * 
	 * @param vtab
	 * @param condition
	 * @return
	 */
	public MatcherResult route(String vtab, ComparativeMapChoicer choicer,
			List<Object> args, boolean needSourceKey) {
		VirtualTable rule = this.vtr.getVirtualTable(vtab);
		MatcherResult result = matcher
				.match(needSourceKey, choicer, args, rule);
		return result;
	}

	/**
	 * 多套规则支持(TDDL使用)
	 * 
	 * @param vtab
	 * @param condition
	 * @return
	 */
	public Map<String, MatcherResult> routeMVer(String vtab,
			ComparativeMapChoicer choicer, List<Object> args,
			boolean needSourceKey) {
		if (this.vtr != null && this.vtrs.size() == 0) {
			throw new RuntimeException(
					"routeWithMulVersion method just support multy version rule,use route method instead or config with multy version style!");
		}

		Map<String, MatcherResult> results = new HashMap<String, MatcherResult>();
		for (Map.Entry<String, VirtualTableRoot> entry : this.vtrs.entrySet()) {
			VirtualTable rule = entry.getValue().getVirtualTable(vtab);
			MatcherResult result = matcher.match(needSourceKey, choicer, args,
					rule);
			results.put(entry.getKey(), result);
		}
		return results;
	}

	/**
	 * 指定一套规则计算
	 * 
	 * @param vtab
	 * @param condition
	 * @return
	 */
	public MatcherResult route(String vtab, ComparativeMapChoicer choicer,
			List<Object> args, boolean needSourceKey,
			VirtualTableRoot specifyVtr) {
		VirtualTable rule = specifyVtr.getVirtualTable(vtab);
		MatcherResult result = matcher
				.match(needSourceKey, choicer, args, rule);
		return result;
	}

	/**
	 * 新旧规则计算并比较,不带目标库判定
	 * 
	 * @param vtab
	 * @param conditionStr
	 * @return
	 * @throws ResultCompareDiffException
	 */
	public MatcherResult routeMVerAndCompare(SqlType sqlType, String vtab,
			ComparativeMapChoicer choicer, List<Object> args,
			boolean needSourceKey) throws ResultCompareDiffException {
		return routeMVerAndCompare(sqlType, vtab, choicer, args, needSourceKey,
				null, null);
	}

	/**
	 * 新旧规则计算并比较,带目标库判定
	 * 
	 * @param vtab
	 * @param conditionStr
	 * @return
	 * @throws ResultCompareDiffException
	 */
	public MatcherResult routeMVerAndCompare(SqlType sqlType, String vtab,
			ComparativeMapChoicer choicer, List<Object> args,
			boolean needSourceKey, String oriDb, String oriTable)
			throws ResultCompareDiffException {
		if (this.vtr != null && this.vtrs.size() == 0) {
			throw new RuntimeException(
					"routeWithMulVersion method just support multy version rule,use route method instead or config with multy version style!");
		}

		// 如果只有单套规则,直接返回这套规则的路由结果
		if (this.vtrs.size() == 1) {
			return route(vtab, choicer, args, needSourceKey,
					this.vtrs.get(versionIndex.get(0)));
		}

		// 如果不止一套规则,那么计算两套规则,默认都返回新规则
		if (this.vtrs.size() != 2 || this.versionIndex.size() != 2) {
			throw new RuntimeException(
					"not support more than 2 copy rule compare");
		}

		// 第一个排位的为旧规则
		MatcherResult oldResult = route(vtab, choicer, args, needSourceKey,
				this.vtrs.get(versionIndex.get(0)));

		if (sqlType.equals(SqlType.SELECT)
				|| sqlType.equals(SqlType.SELECT_FOR_UPDATE)) {
			return oldResult;
		} else {
			// 第二个排位的为新规则
			MatcherResult newResult = route(vtab, choicer, args, needSourceKey,
					this.vtrs.get(versionIndex.get(1)));

			boolean compareResult = MatchResultCompare.matchResultCompare(
					newResult, oldResult, oriDb, oriTable);

			if (compareResult) {
				return oldResult;
			} else {
				throw new ResultCompareDiffException(
						"sql type is not-select,rule calculate result diff");
			}
		}
	}

	@Override
	public boolean isBroadCast(String logicName) {
		// 如果只有单套规则,直接返回这套规则的路由结果
		if (this.vtrs.size() == 1) {
			return isBroadCast(logicName,
					this.vtrs.get(versionIndex.get(0)));
		}

		// 如果不止一套规则,那么计算两套规则,默认都返回新规则
		if (this.vtrs.size() != 2 || this.versionIndex.size() != 2) {
			throw new RuntimeException(
					"not support more than 2 copy rule compare");
		}

		// 第一个排位的为旧规则
		return isBroadCast(logicName,
				this.vtrs.get(versionIndex.get(0)));

	}

	public boolean isBroadCast(String logicName, VirtualTableRoot specifyVtr) {
		VirtualTable rule = specifyVtr.getVirtualTable(logicName);
		return rule.isBroadcast();
	}
	@Override
	public boolean isInTheSameJoinGroup(List<String> logicNames) {
		// 如果只有单套规则,直接返回这套规则的路由结果
		if (this.vtrs.size() == 1) {
			return isInTheSameJoinGroup(logicNames,
					this.vtrs.get(versionIndex.get(0)));
		}

		// 如果不止一套规则,那么计算两套规则,默认都返回新规则
		if (this.vtrs.size() != 2 || this.versionIndex.size() != 2) {
			throw new RuntimeException(
					"not support more than 2 copy rule compare");
		}

		// 第一个排位的为旧规则
		return isInTheSameJoinGroup(logicNames,
				this.vtrs.get(versionIndex.get(0)));
	}
	
	private boolean isInTheSameJoinGroup(List<String> logicNames, VirtualTableRoot specifyVtr) {
		boolean isInTheSameJoinGroup = true;
		String tempJoinGroup = null;
		boolean firstLoop = true;
		for(String logicName : logicNames)
		{
			VirtualTable rule = specifyVtr.getVirtualTable(logicName);
			if(rule == null)
			{
				logger.info("can't find table or index : " + logicName + ". tables  : "+ specifyVtr.getVirtualTableMap());
				//可能是默认的defaultDBIndex 或者没找到。直接返回false可能是最好的选择。
				return false;
			}
			if(firstLoop )
			{
				firstLoop = false;
				//第一次循环
				tempJoinGroup = rule.getJoinGroup();
				continue;
			}
			
			//第二次进入循环
			if(tempJoinGroup == null)
			{
				//null和任意其他join group都能组成joinGroup
				isInTheSameJoinGroup = false;
				break;
			}
			else
			{
				if(!tempJoinGroup.equalsIgnoreCase(rule.getJoinGroup()))
				{
					//两个join group 不相同，也不是相同的join group
					isInTheSameJoinGroup = false;
					break;
				}
			}
		}
		return isInTheSameJoinGroup;
	}
	
	@Override
	public boolean isInTheSameJoinGroup(String leftLogicName,
			String rightLogicName) {
		List<String> logicNames = new ArrayList<String>(2);
		logicNames.add(leftLogicName);
		logicNames.add(rightLogicName);
		return isInTheSameJoinGroup(logicNames);
	}
	
}
