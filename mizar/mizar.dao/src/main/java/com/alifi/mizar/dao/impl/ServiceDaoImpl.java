package com.alifi.mizar.dao.impl;

import java.util.HashMap;
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
        validateUnique(serviceInfo);
    	return insert("service.insertServiceInfo", serviceInfo);
    }
    
    public int update(GatewayServiceInfo serviceInfo) {
        validateUnique(serviceInfo);
    	return update("service.updateServiceById", serviceInfo);
    	
    }
    
    public void validateUnique(GatewayServiceInfo serviceInfo){
        GatewayServiceInfo orgGatewayServiceInfo= (GatewayServiceInfo) get("service.getServiceInfoBySeviceName",
                serviceInfo.getServiceName());
        if(orgGatewayServiceInfo != null) {
            if (orgGatewayServiceInfo.getId() != serviceInfo.getId()){
                throw new RuntimeException("存在同名的服务名称" + serviceInfo.getServiceName());
            }
        }
    }
    
    
    public int del(int id) {
    	return delete("service.deleteServiceById", id);
    }
    
    public GatewayServiceInfo getServiceById(int id) {
    	return (GatewayServiceInfo) get("service.getServiceInfoById", id);
    }

    public List<GatewayServiceInfo> listValid() {
        return getList("service.listValidServiceInfo", null);
    }

    public GatewayServiceInfo getByWebInterfaceNameAndMethodName(String invokeInterface, String invokeMethod) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("invokeInterface", invokeInterface);
        params.put("invokeMethod", invokeMethod);
        return (GatewayServiceInfo) get("service.getByWebInterfaceNameAndMethodName",
                params);
    }
}
