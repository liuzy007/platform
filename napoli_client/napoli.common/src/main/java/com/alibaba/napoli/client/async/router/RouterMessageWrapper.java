package com.alibaba.napoli.client.async.router;

import java.io.Serializable;

import javax.jms.Message;
/**
 * 仅仅是为了接口适配
 * @author zhuanggl
 *
 */
public class RouterMessageWrapper implements Serializable{
    private static final long serialVersionUID = 1L;
    private Message message;

    public RouterMessageWrapper(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
    
}
