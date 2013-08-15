package com.alifi.mizar.service;

import com.alifi.mizar.exception.ServiceNotFoundException;

/**
 * 支付宝通知处理接口
 * @author tongpeng.chentp
 *
 */
public interface AlipayNotificationHandleService {

    /**
     * 处理所有支付宝传过来的通知
     * @param queryString                   支付宝传过来的查询参数
     * @return                              处理结果
     * @throws ServiceNotFoundException     当查询参数中不包含service或者service的值找不到对应的handler，抛出该异常
     */
    public String handle(String queryString) throws ServiceNotFoundException;
}