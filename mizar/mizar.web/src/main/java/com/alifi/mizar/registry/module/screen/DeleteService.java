package com.alifi.mizar.registry.module.screen;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alifi.mizar.manager.ServiceManager;

public class DeleteService {
	
	@Autowired
	private ServiceManager serviceManager;
	
	public void execute(@Param(name="serviceId") int serviceId, HttpServletResponse response) throws Exception {
		serviceManager.deleteServiceById(serviceId);
        response.sendRedirect("services.htm");
	}

}
