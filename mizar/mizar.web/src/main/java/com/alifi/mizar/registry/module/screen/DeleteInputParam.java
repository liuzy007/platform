package com.alifi.mizar.registry.module.screen;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alifi.mizar.manager.ServiceInputParamManager;


public class DeleteInputParam {
    
    @Autowired
    private ServiceInputParamManager serviceInputParamManager;
    
    public void execute(@Param(name="id") int id, @Param(name="service_id") int serviceId, HttpServletResponse response) throws Exception {
    	serviceInputParamManager.deleteInputParamById(id);
        response.sendRedirect("input_params.htm?service_id=" + serviceId);
    }

}
