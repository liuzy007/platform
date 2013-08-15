package com.alifi.mizar.dao;

import java.util.List;

import com.alifi.mizar.common.vo.GatewayInParam;


public interface ServiceInputParamDao {

	List<GatewayInParam> listByServiceId(int serviceId);
	
	int add(GatewayInParam inputParam);
	
	int del(int id);
	
	int delByServiceId(int id);
	
	GatewayInParam getById(int id);
}
