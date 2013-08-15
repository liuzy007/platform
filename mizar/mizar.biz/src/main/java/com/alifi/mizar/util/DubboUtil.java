package com.alifi.mizar.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alifi.mizar.common.vo.GatewayServiceInfo;

public class DubboUtil {
	
	private static String appName;
	private static String registryAddress;
	private static Integer timeout;
	
	private static Map<Integer, GenericService> services = 
			new ConcurrentHashMap<Integer, GenericService>();
	
	public static GenericService getService(GatewayServiceInfo gatewayServiceInfo) {
		GenericService service = services.get(gatewayServiceInfo.getId());
		if (service != null) {
			return service;
		}
		
		ApplicationConfig application = new ApplicationConfig();
		application.setName(appName);
		
		RegistryConfig registry = new RegistryConfig();
		registry.setAddress(registryAddress);
		
		ConsumerConfig consumer = new ConsumerConfig();
		consumer.setTimeout(timeout);
		consumer.setRetries(0);
		
		ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<GenericService>();
		referenceConfig.setApplication(application);
		referenceConfig.setRegistry(registry);
		referenceConfig.setConsumer(consumer);
    	referenceConfig.setInterface(gatewayServiceInfo.getInvokeInterface());
    	referenceConfig.setVersion(gatewayServiceInfo.getVersion());
    	referenceConfig.setGeneric(true);
    	service = referenceConfig.get();
    	if (service != null) {
    		services.put(gatewayServiceInfo.getId(), service);
    	}
    	
    	return service;
	}

	public void setAppName(String appName) {
		DubboUtil.appName = appName;
	}

	public void setRegistryAddress(String registryAddress) {
		DubboUtil.registryAddress = registryAddress;
	}

	public void setTimeout(Integer timeout) {
		DubboUtil.timeout = timeout;
	}
}
