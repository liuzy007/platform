package com.alibaba.napoli.sender;

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.BizInvokationException;
import com.alibaba.napoli.client.async.NapoliMessage;

import com.alibaba.napoli.client.async.SendResult;
import java.io.Serializable;

/**
 * User: heyman
 * Date: 11/24/11
 * Time: 4:02 下午
 */
public interface Sender {
    boolean sendMessage(Serializable message);

    /**
     * 发送负责消息，可以附带用户自定义属性，例如，唯一id
     *
     * @param message 带属性的消息
     * @return result
     */
    boolean sendMessage(NapoliMessage message);

    SendResult sendNapoliMessage(NapoliMessage message);

    //void sendJmsMessage(Message message) throws SenderException;

    void sendMessage(Serializable message, Runnable bizCallback) throws NapoliClientException, BizInvokationException;

    void sendMessage(NapoliMessage message, Runnable bizCallback) throws NapoliClientException, BizInvokationException;

    void refreshSchedule();
    
    void close();

    void beginTransaction() throws NapoliClientException ;

    void commit() throws NapoliClientException ;

    void rollback() throws NapoliClientException ;
}