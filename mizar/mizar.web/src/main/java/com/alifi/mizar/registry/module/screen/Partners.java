package com.alifi.mizar.registry.module.screen;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alifi.mizar.common.vo.Partner;
import com.alifi.mizar.manager.PartnerManager;


public class Partners {
    
    @Autowired
    private PartnerManager partnerManager;
    
    public void execute(Context context) throws Exception {
        List<Partner> partners = partnerManager.list();
        context.put("partners", partners);
    }

}
