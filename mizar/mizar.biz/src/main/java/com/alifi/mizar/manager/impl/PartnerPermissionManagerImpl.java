package com.alifi.mizar.manager.impl;

import java.util.List;

import com.alifi.mizar.common.vo.PartnerPermission;
import com.alifi.mizar.dao.PartnerPermissionDao;
import com.alifi.mizar.manager.BaseCachedManager;
import com.alifi.mizar.manager.PartnerPermissionManager;


public class PartnerPermissionManagerImpl extends BaseCachedManager implements PartnerPermissionManager {
    
    private PartnerPermissionDao partnerPermissionDao;

    public PartnerPermission add(PartnerPermission partnerPermission) {
    	memCachedManager.delete(addNameSpace(partnerPermission.getPartnerId()));
        int id = partnerPermissionDao.insert(partnerPermission);
        return partnerPermissionDao.queryById(id);
    }

    public boolean delete(PartnerPermission partnerPermission) {
        int flag = partnerPermissionDao.delete(partnerPermission);
        if (flag > 0) {
        	memCachedManager.delete(addNameSpace(partnerPermission.getPartnerId()));
        	return true;
        }
        return false;
    }

    public List<PartnerPermission> listByPartnerId(int partnerId) {
    	List<PartnerPermission> permissions = (List<PartnerPermission>) memCachedManager.get(addNameSpace(partnerId));
    	if (permissions != null) {
    		return permissions;
    	}
    	permissions = partnerPermissionDao.listByPartnerId(partnerId);
    	if (permissions != null) {
    		memCachedManager.add(addNameSpace(partnerId), permissions, nextDay());
    	}
        return permissions;
    }
    
    public boolean hasPermission(int partnerId, int serviceId) {
    	List<PartnerPermission> permissions = listByPartnerId(partnerId);
    	if (permissions == null || permissions.size() == 0) {
    		return false;
    	}
    	for (PartnerPermission permission : permissions) {
    		if (permission.getServiceId() == serviceId) {
    			return true;
    		}
    	}
        return false;
    }
    
    public void setPartnerPermissionDao(PartnerPermissionDao partnerPermissionDao) {
        this.partnerPermissionDao = partnerPermissionDao;
    }

	@Override
	protected String addNameSpace(Object key) {
		return "mizar.permission." + key;
	}
}
