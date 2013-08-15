package com.alifi.mizar.registry.module.screen;


import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alifi.mizar.manager.ServiceInputParamManager;


public class InputParams {
    
    @Autowired
    private ServiceInputParamManager serviceInputParamManager;
    
    public void execute(@Param(name="service_id") int serviceId, Context context) throws Exception {
        context.put("inputParams", serviceInputParamManager.listInputParamByServiceId(serviceId));
        context.put("serviceId", serviceId);
    }

}
