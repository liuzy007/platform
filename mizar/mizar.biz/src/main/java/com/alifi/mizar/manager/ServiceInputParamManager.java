package com.alifi.mizar.manager;

import java.util.List;

import com.alifi.mizar.common.vo.GatewayInParam;

public interface ServiceInputParamManager {
	
	/**
	 * 根据服务id查询服务所有输入参数
	 * @param serviceId
	 * @return
	 */
	List<GatewayInParam> listInputParamByServiceId(int serviceId);
	
	/**
	 * 增加输入参数
	 * @param
	 * @return
	 */
	GatewayInParam addInputParam(GatewayInParam inputParam);
	
	/**
	 * 删除输入参数
	 * @param
	 * @return
	 */
	boolean deleteInputParamById(int id);
	
	
}
