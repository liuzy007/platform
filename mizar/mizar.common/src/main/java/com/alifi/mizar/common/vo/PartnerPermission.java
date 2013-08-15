package com.alifi.mizar.common.vo;

import java.io.Serializable;

/**
 * 用户权限
 * @author tongpeng.chentp
 *
 */
public class PartnerPermission implements Serializable {

    private static final long serialVersionUID = 127932868891939667L;
    
    private int id;
    
    private int partnerId;
    
    private int serviceId;

    public int getPartnerId() {
        return partnerId;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }
}
