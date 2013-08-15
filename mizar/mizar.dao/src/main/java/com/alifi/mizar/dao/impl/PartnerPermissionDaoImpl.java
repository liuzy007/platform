package com.alifi.mizar.dao.impl;

import java.util.List;

import com.alifi.mizar.common.vo.PartnerPermission;
import com.alifi.mizar.dao.PartnerPermissionDao;


@SuppressWarnings("unchecked")
public class PartnerPermissionDaoImpl extends BaseDaoImpl implements PartnerPermissionDao {

    public int deleteByPartnerId(int partnerId) {
        return this.delete("partnerPermission.deleteByPartnerId", partnerId);
    }

    public int delete(PartnerPermission partnerPermission) {
        return this.delete("partnerPermission.deleteByPartnerIdAndServiceId", partnerPermission);
    }

    public int deleteByServiceId(int serviceId) {
        return this.delete("partnerPermission.deleteByServiceId", serviceId);
    }

    public List<PartnerPermission> listByPartnerId(int partnerId) {
        return this.getList("partnerPermission.listByPartnerId", partnerId);
    }
    
    public int insert(PartnerPermission partnerPermission) {
        return super.insert("partnerPermission.insert", partnerPermission);
    }

    public PartnerPermission queryById(int id) {
        return (PartnerPermission) this.get("partnerPermission.queryById", id);
    }

}
