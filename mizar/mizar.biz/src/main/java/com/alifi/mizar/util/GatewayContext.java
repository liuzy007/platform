package com.alifi.mizar.util;

import java.util.Map;

import org.apache.commons.chain.impl.ContextBase;

import com.alibaba.common.lang.StringUtil;

/**
 * 网关调用参数上下文
 * @author tongpeng.chentp
 *
 */
public class GatewayContext extends ContextBase {

    private static final long serialVersionUID = -3927016634382798276L;
    
    /**
     * 所有传入的业务业务参数
     * 
     */
    private Map<String, String> inputParams;
    
    /**
     * 调用结果
     */
    private Object object;

    /**
     * 调用是否成功
     */
    private boolean success;

    /**
     * 网关调用错误码
     */
    private String errorCode;

    /**
     * 网关调用错误信息
     */
    private String errorMessage;

    /**
     * 根据key值取得相应的输入参数
     * 假设key值为signType，允许的输入参数为signType,sign_type以及各SIGN_TYPE
     * @param key
     * @return
     */
    public String getInputParam(String key) {
        if (inputParams.containsKey(key)) {
            return inputParams.get(key);
        }
        String lowerKey = StringUtil.toLowerCaseWithUnderscores(key);
        if (inputParams.containsKey(lowerKey)) {
            return inputParams.get(lowerKey);
        }
        String upperKey = StringUtil.toUpperCaseWithUnderscores(key);
        if (inputParams.containsKey(upperKey)) {
            return inputParams.get(upperKey);
        }
        return null;
    }
    
    /**
     * 根据key值删除相应的输入参数
     * 假设key值为signType，允许的输入参数为signType,sign_type以及各SIGN_TYPE
     * @param key
     * @return
     */
    public String removeInputParam(String key) {
        if (inputParams.containsKey(key)) {
            return inputParams.remove(key);
        }
        String lowerKey = StringUtil.toLowerCaseWithUnderscores(key);
        if (inputParams.containsKey(lowerKey)) {
            return inputParams.remove(lowerKey);
        }
        String upperKey = StringUtil.toUpperCaseWithUnderscores(key);
        if (inputParams.containsKey(upperKey)) {
            return inputParams.remove(upperKey);
        }
        return null;
    }
    
    /**
     * 判断对应的key值是否存在，并且值不为空
     * 假设key值为signType，允许的输入参数为signType,sign_type以及各SIGN_TYPE
     * @param key
     * @return
     */
    public boolean containNotBlankInputParam(String key) {
        String value = getInputParam(key);
        return StringUtil.isNotBlank(value);
    }

    public Map<String, String> getInputParams() {
        return inputParams;
    }

    public void setInputParams(Map<String, String> inputParams) {
        this.inputParams = inputParams;
    }
    
    public Object getObject() {
        return object;
    }
    
    public void setObject(Object object) {
        this.object = object;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
