package com.alifi.mizar.manager.impl;

import java.util.List;

import com.alifi.mizar.common.vo.Partner;
import com.alifi.mizar.dao.PartnerDao;
import com.alifi.mizar.dao.PartnerPermissionDao;
import com.alifi.mizar.manager.PartnerManager;


public class PartnerManagerImpl implements PartnerManager {
    
    private PartnerDao partnerDao;
    private PartnerPermissionDao partnerPermissionDao;

    public Partner add(Partner partner) {
        int id = partnerDao.insert(partner);
        return partnerDao.queryById(id);
    }

    public boolean deleteById(int id) {
        partnerPermissionDao.deleteByPartnerId(id);
    	return partnerDao.deleteById(id) > 0;
    }

    public List<Partner> list() {
        return partnerDao.list();
    }
    
    public void setPartnerDao(PartnerDao partnerDao) {
        this.partnerDao = partnerDao;
    }
    
    public void setPartnerPermissionDao(PartnerPermissionDao partnerPermissionDao) {
        this.partnerPermissionDao = partnerPermissionDao;
    }

	public Partner getById(int id) {
		return partnerDao.queryById(id);
	}
}