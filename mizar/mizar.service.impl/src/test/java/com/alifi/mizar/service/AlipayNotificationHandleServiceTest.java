package com.alifi.mizar.service;

import java.util.HashMap;
import java.util.Map;

import com.alifi.mizar.exception.ServiceNotFoundException;
import com.alifi.mizar.handler.AlipayNotificationHandler;
import com.alifi.mizar.service.impl.AlipayNotificationHandleServiceImpl;
import com.alifi.signature.service.SignatureForAlipayService;

import junit.framework.TestCase;

import static org.mockito.Mockito.*;

public class AlipayNotificationHandleServiceTest extends TestCase {
    
    private AlipayNotificationHandleServiceImpl alipayNotificationHandleService;
    
    public void setUp() {
        SignatureForAlipayService signatureForAlipayService = mock(SignatureForAlipayService.class);
        when(signatureForAlipayService.check(isA(String.class), isA(String.class))).thenReturn(true);
        
        Map<String, AlipayNotificationHandler> handlers = new HashMap<String, AlipayNotificationHandler>();
        
        alipayNotificationHandleService = new AlipayNotificationHandleServiceImpl();
        alipayNotificationHandleService.setSignatureForAlipayService(signatureForAlipayService);
        alipayNotificationHandleService.setHandlers(handlers);
    }
    
    public void testServiceNotFound() {
        ServiceNotFoundException exception = null;
        try {
            alipayNotificationHandleService.handle("service=not_exist_service");
        } catch (ServiceNotFoundException e) {
            exception = e;
        }
        assertNotNull(exception);
    }
    
    public void testServiceNotFoundWhenQueryStringNotContainsServiceName() {
        ServiceNotFoundException exception = null;
        try {
            alipayNotificationHandleService.handle("");
        } catch (ServiceNotFoundException e) {
            exception = e;
        }
        assertNotNull(exception);
    }
}
