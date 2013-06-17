/**
 * Project: napoli.client
 *
 * File Created at Sep 18, 2009
 * $Id: DefaultAsyncReceiver.java 180098 2012-06-18 05:53:41Z haihua.chenhh $
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
import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.client.connector.NapoliConnector;
import javax.jms.Session;

import com.alibaba.napoli.receiver.RedeliveryStrategy;
import com.alibaba.napoli.receiver.impl.DefaultReceiverImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author guolin.zhuanggl
 * @author ding.lid
 */
public class DefaultAsyncReceiver extends DefaultReceiverImpl {
    private static final Log log = LogFactory.getLog(DefaultAsyncReceiver.class);
    
    public void setMessageNotHandle(boolean messageNotHandle) {
    }
    
    public long getPeriod() {
        return period;
    }
    
    public void setRedeliveryMultiplier(short redeliveryMultiplier) {
        if (redeliveryMultiplier <= 0) {
            throw new IllegalArgumentException("redeliveryMultiplier must be greater than 0");
        }
        if (redeliveryStrategy == null) {
            redeliveryStrategy = new RedeliveryStrategy();
        }
        redeliveryStrategy.setRedeliveryMultiplier(redeliveryMultiplier);
    }

    public void setRedeliveryExponential(boolean redeliveryExponential) {
        if (redeliveryStrategy == null) {
            redeliveryStrategy = new RedeliveryStrategy();
        }
        redeliveryStrategy.setRedeliveryExponential(redeliveryExponential);
    }

    /**
     * 当使用重发策略时，客户端可以设在最大次数，如果没有设置，使用服务器端值
     *
     * @param maxRedeliveries
     */
    public void setMaxRedeliveries(int maxRedeliveries) {
        if (maxRedeliveries <= 0) {
            throw new IllegalArgumentException("maxRedeliveries must be greater than 0");
        }
        if (redeliveryStrategy == null) {
            redeliveryStrategy = new RedeliveryStrategy();
        }
        redeliveryStrategy.setMaxRedeliveries(maxRedeliveries);
    }

    public void setInitialRedeliveryDelay(int initialRedeliveryDelay) {
        if (initialRedeliveryDelay <= 0) {
            throw new IllegalArgumentException("maxRedeliveries must be greater than 0");
        }
        if (redeliveryStrategy == null) {
            redeliveryStrategy = new RedeliveryStrategy();
        }
        redeliveryStrategy.setInitialRedeliveryDelay(initialRedeliveryDelay);
    }
    
    public void setAcknownedgeMode(int acknownedgeMode) {
        if (acknownedgeMode == Session.AUTO_ACKNOWLEDGE || acknownedgeMode == Session.CLIENT_ACKNOWLEDGE
        		|| acknownedgeMode == Session.DUPS_OK_ACKNOWLEDGE) {
        	if(connectionParam != null) {
        		connectionParam.setAcknowledgeMode(acknownedgeMode);
        	}
        }
    }

    public static DefaultAsyncReceiver createReceiver(NapoliConnector connector,String destination, boolean storeable, int instances,
                                               NapoliWorker worker, String group) throws NapoliClientException {
        DefaultAsyncReceiver defaultReceiver = new DefaultAsyncReceiver();
        defaultReceiver.setConnector(connector);
        defaultReceiver.setName(destination);
        defaultReceiver.setStoreEnable(storeable);
        defaultReceiver.setInstances(instances);
        defaultReceiver.setWorkerPri(worker);
        if (group != null) {
            defaultReceiver.setGroup(group);
        }
        //defaultReceiver.init();
        return defaultReceiver;
    }

    public static DefaultAsyncReceiver createReceiver(NapoliConnector connector,String destination, boolean storeable, int instances, NapoliWorker worker)
            throws NapoliClientException {
        return createReceiver(connector,destination, storeable, instances, worker, null);
    }
    
/***    
    public void setRouterWorker(AsyncRouterWorker worker) {
        throw new IllegalArgumentException("interface depreacated");
    }

    private static boolean canPhysicalQueueReceive(final PhysicalQueue physicalQueue) {
        if (!physicalQueue.isReceivable()) {
            return false;
        }

        if (physicalQueue.getState() != PhysicalQueue.STATE_WORKING
                && physicalQueue.getState() != PhysicalQueue.STATE_STOPPING) {
            return false;
        }

        final Machine machine = physicalQueue.getMachine();
        if (machine.getState() != Machine.STATE_WORKING) {
            // 正常情况下不能执行到这里，能加上物理Queue中的Machine一定是WORKING的状态！
            // 这一点由 Napoli Server的控制台保证。
            if (log.isErrorEnabled()) {
                log.error("the state of machine(id:" + machine.getId() + ") is NOT working, "
                        + "but this machine belongs to a physical queue(id:"
                        + physicalQueue.getId() + ")!");
            }

            return false;
        }

        return true;
    }
***/
    
}