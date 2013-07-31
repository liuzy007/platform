/**
 * Project: napoli.client
 *
 * File Created at Sep 18, 2009
 * $Id: DefaultAsyncSender.java 180098 2012-06-18 05:53:41Z haihua.chenhh $
 *
 * Copyright 2008 Alibaba.com Croporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.napoli.client.async.impl;

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.*;
import com.alibaba.napoli.client.async.router.AsyncRouterSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.sender.impl.DefaultSenderImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;
import java.io.Serializable;

/**
 * @author guolin.zhuanggl
 * @author ding.lid
 */
public class DefaultAsyncSender extends DefaultSenderImpl implements AsyncSenderEx, AsyncRouterSender {
    private static final Log log = LogFactory.getLog(DefaultAsyncSender.class);

    public void setPendingNotifier(PendingNotify pendingNotifier) {
        super.setPendingNotifier(pendingNotifier);
    }

    public void setInstances(int instances) {
        //do nothing, backward compatiable.
    }

    public void setDestinationFilter(DestinationFilter destinationFilter) {
        //TODO: do nothing, backward compatiable, Destinationfilter may still need to be supported
    }

    public boolean getStoreEnable() {
        return this.storeEnable;
    }

    public boolean send(Serializable msg) {
        if (msg instanceof NapoliMessage) {
            return send((NapoliMessage) msg).success();
        } else {
            NapoliMessage napoliMessage = new NapoliMessage(msg);
            return send(napoliMessage).success();
        }
    }

    public boolean send(Serializable msg, int priority) {
        if (msg instanceof NapoliMessage) {
            NapoliMessage nm = (NapoliMessage) msg;
            nm.setPriority(priority > 9 ? 9 : priority < 0 ? 0 : priority);
            return send(nm).success();
        } else {
            NapoliMessage nm = new NapoliMessage(msg);
            nm.setPriority(priority > 9 ? 9 : priority < 0 ? 0 : priority);
            return send(nm).success();
        }

    }

    public boolean send(Message msg) {
        NapoliMessage nm = new NapoliMessage(msg);
        return send(nm).success();
    }

    public SendResult send(NapoliMessage message) {
        if (closed) {
            throw new IllegalStateException("Sender(" + name + ") is closed!");
        }
        boolean ret = true;
        Throwable fail = null;
        if (sendMessage(message)) {
            return SendResult.newInstance(ret, fail);
        } else {
            ret = false;
            fail = new RuntimeException("error happened");
        }
        return SendResult.newInstance(ret, fail);
    }

    public void send(NapoliMessage msg, Runnable bizCall) throws NapoliClientException, BizInvokationException {
        super.sendMessage(msg, bizCall);
    }

    public void send(Serializable msg, Runnable bizCall) throws NapoliClientException, BizInvokationException {
        NapoliMessage napoliMessage;
        if (msg instanceof NapoliMessage) {
            napoliMessage = (NapoliMessage) msg;
        } else {
            napoliMessage = new NapoliMessage(msg);
        }
        send(napoliMessage, bizCall);
    }

    public static DefaultAsyncSender createSender(NapoliConnector connector, String destination, boolean storeable) throws NapoliClientException {
        DefaultAsyncSender defaultSender = new DefaultAsyncSender();
        defaultSender.setConnector(connector);
        defaultSender.setName(destination);
        defaultSender.setStoreEnable(storeable);
        //defaultSender.init();
        return defaultSender;
    }
}
