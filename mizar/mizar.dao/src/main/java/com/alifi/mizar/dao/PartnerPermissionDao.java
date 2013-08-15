package com.alifi.mizar.dao;

import java.util.List;

import com.alifi.mizar.common.vo.PartnerPermission;


public interface PartnerPermissionDao {
    
    public List<PartnerPermission> listByPartnerId(int partnerId);
    
    public int delete(PartnerPermission partnerPermission);
    
    public int deleteByPartnerId(int partnerId);
    
    public int deleteByServiceId(int serviceId);
    
    public int insert(PartnerPermission partnerPermission);

    public PartnerPermission queryById(int id);
}
