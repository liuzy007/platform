package com.alifi.mizar.registry.module.screen;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alifi.mizar.manager.PartnerPermissionManager;
import com.alifi.mizar.manager.ServiceManager;


public class PartnerPermission {
    
    @Autowired
    private PartnerPermissionManager partnerPermissionManager;
    
    @Autowired
    private ServiceManager serviceManager;
    
    public void execute(@Param(name="partner_id") int partnerId, Context context) throws Exception {
        
        context.put("partnerPermissions", partnerPermissionManager.listByPartnerId(partnerId));
        context.put("services", serviceManager.listServices());
        context.put("partnerId", partnerId);
    }
}
