package com.alifi.mizar.registry.module.screen;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alifi.mizar.manager.PartnerManager;

public class DeletePartner {
	
	@Autowired
    private PartnerManager partnerManager;
	
	public void execute(@Param(name="partnerId") int partnerId, HttpServletResponse response) throws Exception {
	    partnerManager.deleteById(partnerId);
        response.sendRedirect("partners.htm");
	}

}
