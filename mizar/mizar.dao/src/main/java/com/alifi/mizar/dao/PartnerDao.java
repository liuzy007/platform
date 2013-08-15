package com.alifi.mizar.dao;

import java.util.List;

import com.alifi.mizar.common.vo.Partner;


public interface PartnerDao {
    
    List<Partner> list();
    
    int insert(Partner partner);
    
    int deleteById(int id);

    Partner queryById(int id);
}
