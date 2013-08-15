package com.alifi.mizar.exception;

/**
 * 找不到支付宝传过来的service值所对应的handler时扔出该错误
 * @author tongpeng.chentp
 *
 */
public class ServiceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -2545961087147365592L;
    
    public ServiceNotFoundException(String message) {
        super(message);
    }

}
