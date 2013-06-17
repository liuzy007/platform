package com.alibaba.napoli.client.async;

import java.io.Serializable;

public class SendResult implements Serializable {
	private static final long serialVersionUID = -6173600535521042018L;
	private boolean success;
	private Throwable error;
    private String msgId;

	public static SendResult newInstance(boolean success, Throwable error){
		return new SendResult(success, error);
	}
	
	private SendResult(boolean success, Throwable error) {
		this.success = success;
		this.error = error;
	}

	public boolean success() {
		return success;
	}

	public Throwable error() {
		return error;
	}

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
