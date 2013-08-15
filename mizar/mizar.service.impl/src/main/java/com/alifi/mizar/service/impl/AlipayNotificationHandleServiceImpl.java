package com.alifi.mizar.service.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alifi.mizar.exception.ServiceNotFoundException;
import com.alifi.mizar.handler.AlipayNotificationHandler;
import com.alifi.mizar.service.AlipayNotificationHandleService;
import com.alifi.mizar.util.OperationStringUtil;
import com.alifi.signature.service.SignatureForAlipayService;

/**
 * 
 * @author tongpeng.chentp
 *
 */
public class AlipayNotificationHandleServiceImpl implements AlipayNotificationHandleService {
    
    private static final Log log = LogFactory.getLog(AlipayNotificationHandleServiceImpl.class);
    private static final String ILLEGAL_SIGN = "ILLEGAL_SIGN";
    
    private Map<String, AlipayNotificationHandler> handlers;
    private SignatureForAlipayService signatureForAlipayService;

    public String handle(String queryString) throws ServiceNotFoundException {
        Map<String, String> params = OperationStringUtil.parseAndDecodeQueryString(queryString);
        if (!params.containsKey("service")) {
            log.error("cann't find service from query string");
            throw new ServiceNotFoundException("cann't find service from query string");
        }
        String serviceName = params.get("service");
        if (!handlers.containsKey(serviceName)) {
            log.error("cann't find handler for service:[" + serviceName + "]");
            throw new ServiceNotFoundException("cann't find handler for service:[" + serviceName + "]");
        }
        AlipayNotificationHandler handler = handlers.get(serviceName);
        if (!checkSignature(params)) {
            log.error("signature cannot verify when handle notification from alipay!");
            return handler.fail(ILLEGAL_SIGN);
        }
        return handler.handle(params);
    }
    
    private boolean checkSignature(Map<String, String> params) {
        if (!params.containsKey("sign")) {
            return false;
        }
        String sign = params.get("sign");
        params.remove("sign");
        params.remove("sign_type");
        String content = OperationStringUtil.getSortParameters(params);
        return signatureForAlipayService.check(content, sign);
    }
    
    public void setHandlers(Map<String, AlipayNotificationHandler> handlers) {
        this.handlers = handlers;
    }
    
    public void setSignatureForAlipayService(SignatureForAlipayService signatureForAlipayService) {
        this.signatureForAlipayService = signatureForAlipayService;
    }

}
