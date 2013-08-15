package com.alifi.mizar.handler.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alifi.mizar.handler.AlipayNotificationHandler;

/**
 * 处理资金驱动通知
 * @author tongpeng.chentp
 *
 */
public class CashNotifyLoanHandler implements AlipayNotificationHandler {
    
    private static final Log logger = LogFactory.getLog(CashNotifyLoanHandler.class);
    
    private DefaultAsyncSender sender;
    
    public String fail(String message) {
        return "f";
    }

    public String handle(Map<String, String> params) {
        logger.info("支付宝资金驱动参数：" + params.toString());
        if (!params.containsKey("user_id")) {
			return fail(null);
		}
		String userId = params.get("user_id");
		try {
			if (sender.send(userId)) {
				logger.info("result for [" + userId + "] is success");
				return "success";
			}
			logger.info("result for [" + userId + "] is failed");
			return "f";
		} catch (Exception e) {
			logger.error("result for [" + userId + "] is failed", e);
			return "f";
		}
    }
    
    public void setSender(DefaultAsyncSender sender) {
    	this.sender = sender;
    }
}