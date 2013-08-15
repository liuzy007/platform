package com.alifi.mizar.handler;

import java.util.Map;

/**
 * 处理支付宝通知的handler接口，每个service对应一个handler
 * @author tongpeng.chentp
 *
 */
public interface AlipayNotificationHandler {

    /**
     * 处理通知
     * @param params    通知包含的参数
     * @return          处理结果
     */
    public String handle(Map<String, String> params);
    
    /**
     * 定义错误信息格式
     * @param message   出错信息      
     * @return
     */
    public String fail(String message);
}