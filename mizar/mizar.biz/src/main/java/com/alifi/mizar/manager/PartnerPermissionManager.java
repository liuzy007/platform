package com.alifi.mizar.manager;

import java.util.List;

import com.alifi.mizar.common.vo.PartnerPermission;


public interface PartnerPermissionManager {
    
    List<PartnerPermission> listByPartnerId(int partnerId);
    
    PartnerPermission add(PartnerPermission partnerPermission);
    
    boolean delete(PartnerPermission partnerPermission);
    
    boolean hasPermission(int partnerId, int serviceId);

}
