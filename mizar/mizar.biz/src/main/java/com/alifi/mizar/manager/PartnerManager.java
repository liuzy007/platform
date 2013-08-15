package com.alifi.mizar.manager;

import java.util.List;

import com.alifi.mizar.common.vo.Partner;


public interface PartnerManager {
    
    List<Partner> list();
    
    Partner add(Partner partner);
    
    boolean deleteById(int id);

    Partner getById(int id);
}