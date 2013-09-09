package com.alifi.mizar.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.common.lang.StringUtil;

/**
 * 
 * @author wenjing.huangwj
 *
 */
public class OperationStringUtil {
    
    private OperationStringUtil() {};
    
    private static final Log	logger = LogFactory.getLog(OperationStringUtil.class);
    private static final String INPUT_CHARSET = "_input_charset";
    private static final String DEFAULT_CHARSET = "UTF-8";

	/**
	 * 解析queryString
	 * @param str
	 * @return
	 */
	private static Map<String, String> parseQueryString(String str){
	    Map<String, String> map = new HashMap<String, String>();
		if(StringUtil.isBlank(str)) {
		    return map;
		}
		String[] paramStrs = StringUtil.split(str, "&");
		for(String paramStr:paramStrs){
			String[] keyValue = StringUtil.split(paramStr, "=");
			if(keyValue.length<2) {
			    continue;
			}
			map.put(keyValue[0], keyValue[1]);
		}
		return map;
	}
	
	/**
	 * 解析queryString并decode参数
	 * @param queryString
	 * @return
	 */
	public static Map<String, String> parseAndDecodeQueryString(String queryString) {
        Map<String, String> params = parseQueryString(queryString);
        String charset = params.containsKey(INPUT_CHARSET) ? params.get(INPUT_CHARSET) : DEFAULT_CHARSET;
        URLCodec codec = new URLCodec(charset);
        for(Entry<String, String> entry : params.entrySet()) {
            String value = null;
            try {
                value = codec.decode(entry.getValue());
            } catch (DecoderException e) {
                logger.warn("got exception when decode [" + entry.getValue() + "]");
            }
            params.put(entry.getKey(), value);
        }
        return params;
    }

	/**
	 * 以map中内容生成 queryString
	 * @param properties
	 * @return
	 */
    public static String getSortParameters(Map<String, ?> params) {
        StringBuffer content = new StringBuffer();
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        boolean firstKey = true;
        for (String key : keys) {
            if (params.get(key) == null) {
                continue;
            }
            String value = params.get(key).toString();
            content.append((firstKey ? "" : "&") + key + "=" + value);
            firstKey = false;
        }
        return content.toString();
    }
    
    /**
     * 提取map中每个String数组的第一个参数
     * @param map
     * @return
     */
    public static Map<String, String> convertMapOfArrayToString(Map<String, String[]> map) {
        Map<String, String> result = new HashMap<String, String>();
        for (Entry<String, String[]> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue()[0]);
        }
        return result;
    }
}