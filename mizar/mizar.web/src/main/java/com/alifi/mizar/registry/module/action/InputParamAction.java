package com.alifi.mizar.registry.module.action;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alifi.mizar.common.vo.GatewayInParam;
import com.alifi.mizar.manager.ServiceInputParamManager;


public class InputParamAction {
    
    @Autowired
    private ServiceInputParamManager serviceInputParamManager;
    
    public void doAdd(@FormGroup("inputParam") GatewayInParam inputParam, HttpServletResponse response) throws IOException {
        inputParam.setCreater("admin");
        inputParam.setModifier("admin");

        serviceInputParamManager.addInputParam(inputParam);
        response.sendRedirect("inputParams.htm?service_id=" + inputParam.getServiceId());
    }
}
