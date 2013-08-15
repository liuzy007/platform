package com.alifi.mizar.common.util;

import java.io.Serializable;

public class CommonResult<T> implements Serializable{
    
    public CommonResult() {}
    
    public CommonResult(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

	/**
	 * 
	 */
	private static final long serialVersionUID = -9042193287553583618L;
	/**
	 * 结果状态
	 */
	private boolean isSuccess;
	/**
	 * 错误详细信息描述
	 */
	private String errorMessage;
	/**
	 * 错误码
	 */
	private String errorCode;
	/**
	 * 返回的业务对象
	 */
	private T object;

    /**
     * 签名值
     */
    private String sign;

    /**
     * 签名类型
     */
    private String signType;
	
	public boolean isSuccess() {
		return isSuccess;
	}
	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public T getObject() {
		return object;
	}
	public void setObject(T object) {
		this.object = object;
	}

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }
}
