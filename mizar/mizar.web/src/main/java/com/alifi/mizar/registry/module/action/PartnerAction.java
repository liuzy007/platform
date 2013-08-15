package com.alifi.mizar.registry.module.action;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alifi.mizar.common.vo.Partner;
import com.alifi.mizar.manager.PartnerManager;


public class PartnerAction {
    
    @Autowired
    private PartnerManager partnerManager;
    
    public void doAdd(@FormGroup("partner") Partner partner, HttpServletResponse response) throws IOException {
        partner.setRole("Q");
        partnerManager.add(partner);
        response.sendRedirect("partners.htm");
    }
}
