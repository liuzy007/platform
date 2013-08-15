package com.alifi.mizar.registry.module.screen;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alifi.mizar.common.vo.PartnerPermission;
import com.alifi.mizar.manager.PartnerPermissionManager;


public class ChangePermission {
    
    @Autowired
    private PartnerPermissionManager partnerPermissionManager;
    
    public void execute(@Param(name="service_id") int serviceId, 
                        @Param(name="partner_id") int partnerId,
                        @Param(name="method") String action,
                        HttpServletResponse response) throws Exception {
        PartnerPermission permission = new PartnerPermission();
        permission.setPartnerId(partnerId);
        permission.setServiceId(serviceId);
        String result;
        if (action.equals("add")) {
            result = partnerPermissionManager.add(permission) == null ? "false" : "true";
        } else {
            result = partnerPermissionManager.delete(permission) ? "true" : "false";
        }
        response.getWriter().write(result);
    }

}
