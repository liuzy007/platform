package com.alibaba.napoli.spi;

import com.alibaba.napoli.client.async.BizInvokationException;
import com.alibaba.napoli.client.async.NapoliMessage;

import com.alibaba.napoli.mqImpl.NapoliConnection;
import com.alibaba.napoli.mqImpl.TransportException;
import com.alibaba.napoli.sender.NapoliSenderContext;

/**
 * User: heyman
 * Date: 12/2/11
 * Time: 11:21 上午
 */
public interface TransportSender {
    void send(String name, NapoliMessage message) throws TransportException;

    void send(String name, NapoliMessage message, Runnable bizCall) throws TransportException,BizInvokationException;

    void close();

    NapoliConnection getNapoliConnection();

    TransportSender createTransportSender(NapoliSenderContext napoliSenderContext) throws TransportException;
    
    String getName();
}