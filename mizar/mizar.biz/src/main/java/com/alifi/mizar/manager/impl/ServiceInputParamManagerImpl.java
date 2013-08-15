package com.alifi.mizar.manager.impl;

import java.util.List;

import com.alifi.mizar.common.vo.GatewayInParam;
import com.alifi.mizar.dao.ServiceInputParamDao;
import com.alifi.mizar.manager.BaseCachedManager;
import com.alifi.mizar.manager.ServiceInputParamManager;

public class ServiceInputParamManagerImpl extends BaseCachedManager implements ServiceInputParamManager {

	private ServiceInputParamDao serviceInputParamDao;
	
	public List<GatewayInParam> listInputParamByServiceId(int serviceId) {
		List<GatewayInParam> gatewayInParams = (List<GatewayInParam>) memCachedManager.get(addNameSpace(serviceId));
		if (gatewayInParams != null) {
			return gatewayInParams;
		}
		gatewayInParams = serviceInputParamDao.listByServiceId(serviceId);
		if (gatewayInParams != null) {
			memCachedManager.add(addNameSpace(serviceId), gatewayInParams, nextDay());
		}
		return gatewayInParams;
	}
	
	public GatewayInParam addInputParam(GatewayInParam inputParam) {
		memCachedManager.delete(addNameSpace(inputParam.getServiceId()));
		return serviceInputParamDao.getById(serviceInputParamDao.add(inputParam));
	}
	
	public boolean deleteInputParamById(int id) {
		GatewayInParam inParam = serviceInputParamDao.getById(id);
		int flag = serviceInputParamDao.del(id);
		if (flag > 0) {
			memCachedManager.delete(addNameSpace(inParam.getServiceId()));
			return true;
		}
		return false;
	}
	
	public void setServiceInputParamDao(ServiceInputParamDao serviceInputParamDao) {
        this.serviceInputParamDao = serviceInputParamDao;
    }

	@Override
	protected String addNameSpace(Object key) {
		return "mizar.serviceInputParam." + key;
	}

}
