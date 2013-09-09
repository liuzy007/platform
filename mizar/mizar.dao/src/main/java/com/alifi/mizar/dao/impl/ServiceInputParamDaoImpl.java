package com.alifi.mizar.dao.impl;

import java.util.List;

import com.alifi.mizar.common.vo.GatewayInParam;
import com.alifi.mizar.dao.ServiceInputParamDao;

@SuppressWarnings("unchecked")
public class ServiceInputParamDaoImpl extends BaseDaoImpl implements ServiceInputParamDao{
	
	public List<GatewayInParam> listByServiceId(int serviceId) {
		return (List<GatewayInParam>) getList("serviceInputParam.listParamByServiceId", serviceId);
	}
	
	public int add(GatewayInParam inputParam) {
		return insert("serviceInputParam.insertInputParam", inputParam);
	}
	
	public int del(int id) {
		return delete("serviceInputParam.deleteInputParamById", id);
	}
	
	public int delByServiceId(int id) {
		return delete("serviceInputParam.deleteInputParamByServiceId", id);
	}
	
	public GatewayInParam getById(int id) {
		return (GatewayInParam) get("serviceInputParam.getInputParamById", id);
	}

    public List<GatewayInParam> list() {
        return getList("serviceInputParam.list", null);
    }
    
}
