package com.alifi.mizar.manager;

import java.util.List;
import java.util.Map;

import com.alifi.mizar.common.vo.GatewayServiceInfo;

public interface ServiceManager {
	
	/**
	 * 根据服务名称查询服务相关信息
	 * 
	 * @param serviceName
	 * @param id
	 * @return
	 */
	GatewayServiceInfo queryClassName(String serviceName);
	
	/**
	 * 统计服务数量
	 * 
	 * @param
	 * @return
	 */
    long countServicesByMap(Map<String, Object> parameters);
    
    /**
	 * 服务分页
	 * 
	 * @param
	 * @return
	 */
	List<GatewayServiceInfo> getServicesByMap(Map<String, Object> parameters);
	
	/**
	 * 列出所有服务
	 * 
	 * @param
	 * @return
	 */
	List<GatewayServiceInfo> listServices();
	
	/**
	 * 增加服务
	 * 
	 * @param
	 * @return
	 */
	GatewayServiceInfo addService(GatewayServiceInfo serviceInfo);
	
	/**
	 * 根据ID更新服务
	 * 
	 * @param
	 * @return
	 */
    boolean updateServiceById(GatewayServiceInfo serviceInfo);
    
    /**
	 * 根据ID删除服务
	 * 
	 * @param
	 * @return
	 */
    boolean deleteServiceById(int id);
    
    /**
	 * 根据ID列出服务
	 * 
	 * @param
	 * @return
	 */
    GatewayServiceInfo getServiceById(int id);

}
