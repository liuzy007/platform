package com.alifi.mizar.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alifi.mizar.common.vo.GatewayInParam;

/**
 * 
 * @author mark.lijj
 *
 */
public class GatewayUtil {
    
    private GatewayUtil() {}

	/**
	 * 验证输入参数有效性
	 * 
	 * @param listParams
	 * @return
	 */
	public static boolean validateInParams(List<GatewayInParam> listParams,
			Map<String, String> paramsMap) {
		for (GatewayInParam param : listParams) {
			// 参数不允许为空
			if (!param.isNullable() && paramsMap.get(param.getParamName()) == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 转换map
	 * 
	 * @return
	 */
	public static Map<String, String> convertMap(Map<String, String[]> maps) {
		Map<String, String> resultMap = new HashMap<String, String>();
		String[] values;

		for (Iterator<Entry<String, String[]>> it = maps.entrySet().iterator(); it
				.hasNext();) {
			Entry<String, String[]> entry = (Entry<String, String[]>) it.next();
			values = (String[]) entry.getValue();
			if (values != null && values.length == 1) {
				resultMap.put((String) entry.getKey(), values[0]);
			}
		}
		return resultMap;
	}

	/**
	 * 转换paramType 不支持byte
	 * 
	 * @param param
	 * @param paramType
	 * @return
	 */
	public static Object convertParamType(String param, String paramType) {
		if (param == null) {
			return null;
		}
		if (Constants.BOOLEAN_OBJECT.equals(paramType)) {
			return Boolean.valueOf(param);
		} else if (Constants.BOOLEAN.equals(paramType)) {
			return Boolean.parseBoolean(param);
		} else if (Constants.DOUBLE_OBJECT.equals(paramType)) {
			return Double.valueOf(param);
		} else if (Constants.DOUBLE.equals(paramType)) {
			return Double.parseDouble(param);
		} else if (Constants.FLOAT_OBJECT.equals(paramType)) {
			return Float.valueOf(param);
		} else if (Constants.FLOAT.equals(paramType)) {
			return Float.parseFloat(param);
		} else if (Constants.INTEGER.equals(paramType)) {
			return Integer.valueOf(param);
		} else if (Constants.INT.equals(paramType)) {
			return Integer.parseInt(param);
		} else if (Constants.LONG_OBJECT.equals(paramType)) {
			return Long.valueOf(param);
		} else if (Constants.LONG.equals(paramType)) {
			return Long.parseLong(param);
		} else if (Constants.SHORT_OBJECT.equals(param)) {
			return Short.valueOf(param);
		} else if (Constants.SHORT.equals(paramType)) {
			return Short.parseShort(param);
		} else if (Constants.DATE.equals(paramType)) {
		    try {
                return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(param);
            } catch (ParseException e) {
                return null;
            }
		}
		return param;
	}

}
