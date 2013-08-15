package com.alifi.mizar.dao;

import java.util.List;
import java.util.Map;

import com.alifi.mizar.common.vo.GatewayServiceInfo;

public interface ServiceDao {
	
	List<GatewayServiceInfo> list();
	
	GatewayServiceInfo getByServiceName(String serviceName);
	
	long countServicesByMap(Map<String, Object> parameters);
	
	List<GatewayServiceInfo> getServicesByMap(Map<String, Object> parameters);
	
	int add(GatewayServiceInfo serviceInfo);
	
	int update(GatewayServiceInfo serviceInfo);
	
	int del(int id);
	
	GatewayServiceInfo getServiceById(int id);

}
