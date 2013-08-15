package com.alifi.mizar.gateway.module.screen;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alifi.mizar.service.AlipayNotificationHandleService;

public class AlipayNotification {
	
	@Resource(name="alipayNotificationHandleService.local")
	private AlipayNotificationHandleService alipayNotificationHandleService;

    public void execute(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String queryString = request.getQueryString();
    	String result = alipayNotificationHandleService.handle(queryString);
		response.getWriter().write(result);
    }
}