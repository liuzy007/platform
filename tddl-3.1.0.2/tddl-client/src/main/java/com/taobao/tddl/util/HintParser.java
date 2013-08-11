//Copyright(c) Taobao.com
package com.taobao.tddl.util;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.tddl.client.RouteCondition;
import com.taobao.tddl.client.databus.StartInfo;
import com.taobao.tddl.common.jdbc.ParameterContext;
import com.taobao.tddl.common.util.TStringUtil;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a>
 * @version 1.0
 * @since 1.6
 * @date 2010-12-24上午10:32:16
 */
public class HintParser {
	public static Log log = LogFactory.getLog(HintParser.class);

	public static RouteCondition convertHint2RouteCondition(StartInfo startInfo) {
		String tddlHint = extractTDDLHintString(startInfo.getSql(),startInfo.getSqlParam());
		// decode 成RouteCondition
		if (null != tddlHint && !tddlHint.equals("")) {
			try {
				JSONObject jsonObject = new JSONObject(tddlHint);
				RouteMethod type = RouteMethod.valueOf(jsonObject
						.getString("type"));
				if (type.equals(RouteMethod.executeByDB)
						|| type.equals(RouteMethod.executeByDBAndTab)
						|| type.equals(RouteMethod.executeByDBAndMutiReplace)) {
					return DBProxyThreadLocalHepler
							.decodeNoComparativeRouteCondition4Outer(jsonObject, type);
				} else if (type.equals(RouteMethod.executeByCondition)
						/*|| type.equals(RouteMethod.executeByAdvancedCondition)*/) {
					return DBProxyThreadLocalHepler
							.decodeComparativeRouteCondition4Outer(jsonObject);
				} else {
					log.error("not supported type! the type is:" + type.value());
				}
			} catch (JSONException e) {
				log.error(
						"convert tddl hint to RouteContion faild,check the hint string!",
						e);
			}
			return null;
		} else {
			return null;
		}
	}

	protected enum RouteMethod {
		executeByDBAndTab("executeByDBAndTab"), executeByDBAndMutiReplace(
				"executeByDBAndMutiReplace"), executeByDB("executeByDB"), executeByRule(
				"executeByRule"), executeByCondition("executeByCondition"), executeByAdvancedCondition(
				"executeByAdvancedCondition");

		private String type;

		private RouteMethod(String type) {
			this.type = type;
		}

		public String value() {
			return this.type;
		}
	}

	private static String extractTDDLHintString(String sql,
			Map<Integer, ParameterContext> parameterSettings) {
		String tddlHint = TStringUtil.getBetween(sql, "/*+TDDL(", ")*/");
		if(null==tddlHint||"".endsWith(tddlHint)){
			return null;
		}
		StringBuffer sb = new StringBuffer();
		int size = tddlHint.length();
		int parameters = 1;
		for (int i = 0; i < size; i++) {
			if (tddlHint.charAt(i) == '?') {
				// TDDLHINT只能设置简单值
				sb.append(parameterSettings.get(parameters).getArgs()[1]);
				parameters++;
			}else{
				sb.append(tddlHint.charAt(i));
			}
		}
		return sb.toString();
	}

	public static void removeTddlHintAndParameter(StartInfo startInfo) {
		String sql = startInfo.getSql();
		String tddlHint= TStringUtil.getBetween(sql, "/*+TDDL(", ")*/");
		if(null==tddlHint||"".endsWith(tddlHint)){
			return;
		}
		int size = tddlHint.length();
		int parameters = 0;
		for (int i = 0; i < size; i++) {
			if (tddlHint.charAt(i) == '?') {
				parameters++;
			}
		}

	    sql=TStringUtil.removeBetweenWithSplitor(sql, "/*+TDDL(", ")*/");
		startInfo.setSql(sql);

		// 如果parameters为0，说明TDDLhint中没有参数，所以直接返回sql即可
		if (parameters == 0) {
			return;
		}

		Map<Integer, ParameterContext> parametersettings = startInfo.getSqlParam();
		// TDDL的hint必需写在SQL语句的最前面，如果和ORACLE hint一起用，
		// 也必需写在hint字符串的最前面，否则参数非常难以处理，也就会出错
		SortedMap<Integer, ParameterContext> tempMap = new TreeMap<Integer, ParameterContext>();
		for (int i = 1; i <= parameters; i++) {
			parametersettings.remove(i);
		}

		tempMap.putAll(parametersettings);
		parametersettings.clear();
		// 这段需要性能优化
		int tempMapSize = tempMap.size();
		for (int i = 1; i <= tempMapSize; i++) {
			Integer ind = tempMap.firstKey();
			ParameterContext pc = tempMap.get(ind);
			pc.getArgs()[0] = i;
			parametersettings.put(i, pc);
			tempMap.remove(ind);
		}
	}

	public static void main(String[] args) {
		Map<Integer, ParameterContext> re = new HashMap<Integer, ParameterContext>();
		ParameterContext pc1 = new ParameterContext();
		pc1.setArgs(new Object[] { 1, 1 });
		ParameterContext pc2 = new ParameterContext();
		pc2.setArgs(new Object[] { 2, 2 });
		ParameterContext pc3 = new ParameterContext();
		pc3.setArgs(new Object[] { 3, 3 });
		re.put(1, pc1);
		re.put(2, pc2);
		re.put(3, pc3);
		String sql = "/*+TDDL({key:?,key2:?})*//*+FULL(tab)*/ select * from tab where b=?";
		System.out.println(HintParser.extractTDDLHintString(sql, re));
		StartInfo startInfo = new StartInfo();
		startInfo.setSql(sql);
		startInfo.setSqlParam(re);
		HintParser.removeTddlHintAndParameter(startInfo);
		System.out.println(startInfo.getSql());
		System.out.println(re);
	}
}
