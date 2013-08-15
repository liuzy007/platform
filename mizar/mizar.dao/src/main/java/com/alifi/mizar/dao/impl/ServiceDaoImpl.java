package com.alifi.mizar.dao.impl;

import java.util.List;
import java.util.Map;

import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.dao.ServiceDao;

@SuppressWarnings("unchecked")
public class ServiceDaoImpl extends BaseDaoImpl implements ServiceDao{
    
    
    
    public List<GatewayServiceInfo> list() {
    	return getList("service.listServiceInfo", null);
    }
    
    public GatewayServiceInfo getByServiceName(String serviceName) {
    	return (GatewayServiceInfo) get("service.getServiceInfoBySeviceName",
				serviceName);
    }
    
    public long countServicesByMap(Map<String, Object> parameters) {
	    return (Long) get("service.countServicesByMap", parameters);   
    }
    
    public List<GatewayServiceInfo> getServicesByMap(Map<String, Object> parameters) {
	    return (List<GatewayServiceInfo>) getList("service.getServicesByMap", parameters);
    }
    
    public int add(GatewayServiceInfo serviceInfo) {
    	return insert("service.insertServiceInfo", serviceInfo);
    }
    
    public int update(GatewayServiceInfo serviceInfo) {
    	return update("service.updateServiceById", serviceInfo);
    	
    }
    
    public int del(int id) {
    	return delete("service.deleteServiceById", id);
    }
    
    public GatewayServiceInfo getServiceById(int id) {
    	return (GatewayServiceInfo) get("service.getServiceInfoById", id);
    }
}
