package com.alifi.mizar.registry.module.screen;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alifi.mizar.service.B2BService;

public class ReceiveComplaint {
	
	@Resource(name="b2bService.local")
	private B2BService b2bService;

    public void execute(@Param(name="format", defaultValue="json2") String format, HttpServletRequest request, HttpServletResponse response) throws IOException {
    	Map<String, String[]> params = request.getParameterMap();
    	String result = b2bService.receiveComplaint(params);
		response.getWriter().write(result);
    }
}