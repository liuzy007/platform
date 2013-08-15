package com.alifi.mizar.registry.module.action;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.manager.ServiceManager;


public class ServiceAction {
    
    @Autowired
    private ServiceManager serviceManager;
    
    public void doAdd(@FormGroup("service") GatewayServiceInfo service, HttpServletResponse response) throws IOException {
        service.setCreated("admin");
        
        serviceManager.addService(service);
        response.sendRedirect("services.htm");
    }
    
    public void doEdit(@FormGroup("service") GatewayServiceInfo service, Context context, HttpServletResponse response) throws IOException {
    	serviceManager.updateServiceById(service);
    	context.put("serviceInfo", serviceManager.getServiceById(service.getId()));
       response.sendRedirect("servicesInfo.htm?service_id=" + service.getId());
    }

}
