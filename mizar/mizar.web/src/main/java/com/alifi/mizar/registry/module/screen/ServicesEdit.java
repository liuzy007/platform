package com.alifi.mizar.registry.module.screen;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alifi.mizar.manager.ServiceManager;

public class ServicesEdit {
	
	@Autowired
	private ServiceManager serviceManager;
	
	public void execute(@Param("service_id") Integer serviceId, Context context) throws Exception {
		if(serviceId != null) {
		context.put("serviceInfo", serviceManager.getServiceById(serviceId));
		}
	}
}
