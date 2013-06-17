package com.alibaba.napoli.sender;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.BizInvokationException;

/**
 * User: heyman Date: 2/16/12 Time: 2:07 下午
 */
public class NapoliResult implements Serializable {
    private static final long                serialVersionUID = -6925924956850004727L;
    private static final Log                 log              = LogFactory.getLog(NapoliResult.class);

    private boolean                          success;
    private LinkedHashMap<String, Throwable> exceptionMap     = new LinkedHashMap<String, Throwable>(1);
    private Map<String, String>              props            = new HashMap<String, String>();
    private boolean                          command          = false;

    public NapoliResult(boolean success) {
        this.success = success;
    }

    public NapoliResult(String errorMsg, Throwable exception) {
        exceptionMap.put(errorMsg, exception);
        success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void addException(String errorMsg, Throwable e) {
        exceptionMap.put(errorMsg, e);
    }

    public boolean hasException() {
        return exceptionMap.size() > 0;
    }

    public void printError() {
        for (Map.Entry<String, Throwable> entry : exceptionMap.entrySet()) {
            String errMsg = entry.getKey();
            Throwable throwable = entry.getValue();
            if (success && log.isWarnEnabled()) {
                log.warn(errMsg, throwable);
            }
            if (!success) {
                log.error(errMsg, throwable);
            }
        }
    }

    public void throwOutException() throws NapoliClientException, BizInvokationException {
        for (Map.Entry<String, Throwable> entry : exceptionMap.entrySet()) {
            String errMsg = entry.getKey();
            Throwable throwable = entry.getValue();
            if (throwable instanceof BizInvokationException) {
                throw (BizInvokationException) throwable;
            } else {
                throw new NapoliClientException(errMsg, throwable);
            }
        }
    }

    public Throwable[] getThrowables() {
        Throwable[] throwables = new Throwable[exceptionMap.size()];
        int i = 0;
        for (Map.Entry<String, Throwable> entry : exceptionMap.entrySet()) {
            throwables[i] = entry.getValue();
            i += 1;
        }
        return throwables;
    }

    public void addProp(String key, String value) {
        props.put(key, value);
    }

    public void setMsgId(String value) {
        props.put("_msgid", value);
    }

    public String getMsgId() {
        return props.get("_msgid");
    }

    public String getProp(String key) {
        return props.get(key);
    }

    public boolean isCommand() {
        return command;
    }

    public void setCommand(boolean command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "success=" + success + ", exception=" + exceptionMap + "]";
    }
}
