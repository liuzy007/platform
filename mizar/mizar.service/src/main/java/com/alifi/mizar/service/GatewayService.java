package com.alifi.mizar.service;

import java.util.Map;

import com.alifi.mizar.common.util.CommonResult;

public interface GatewayService {
	
	public CommonResult<?> invokeService(Map<String, String[]> paramsMap);
	
	public CommonResult<?> invokeDubboServiceByClass(Map<String, String> paramsMap);
}
