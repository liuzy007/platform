package com.alifi.mizar.registry.module.screen;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;


public class AddInputParam {
    
    public void execute(@Param(name="serviceId") int serviceId, Context context) throws Exception {
        context.put("serviceId", serviceId);
    }

}
