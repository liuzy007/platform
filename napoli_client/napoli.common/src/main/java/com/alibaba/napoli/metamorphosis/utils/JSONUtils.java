package com.alibaba.napoli.metamorphosis.utils;

import com.alibaba.fastjson.JSON;

public class JSONUtils {
	
	public static String serializeObject(final Object o) throws Exception {
		return JSON.toJSONString(o);
	}

	public static Object deserializeObject(final String s, final Class<?> clazz) throws Exception {
		return JSON.parseObject(s, clazz);
	}
	
	
	
}
