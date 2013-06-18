package com.alibaba.napoli.mqImpl.hornetQ;

import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.Extension;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.mqImpl.NapoliConnection;
import com.alibaba.napoli.mqImpl.TransportException;
import com.alibaba.napoli.receiver.ReceiverException;
import com.alibaba.napoli.receiver.filter.Filter;
import com.alibaba.napoli.spi.TransportConsumer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.SessionFailureListener;
import org.hornetq.core.client.impl.ClientSessionImpl;

/**
 * User: yanny.wang Date: 2012/1/16
 */
@Extension("hornetQ")
public class HornetQTransportConsumer implements TransportConsumer {
    protected static final Log logger = LogFactory.getLog(HornetQTransportConsumer.class);

    protected HQConnection connection;

    protected List<HQConsumerListener> consumerList;
    protected ConnectionParam connectionParam;
    protected NapoliWorker worker;
    protected ClientMachine machine;
    protected ConsoleConnector connector;

    private List<Filter> filterList;
    protected static final Object lock = new Object();

    public String getName() {
        return "hornetQ";
    }

    public TransportConsumer createTransportConsumer(ConsoleConnector connector, ClientMachine machine,
                                                     ConnectionParam connectionParam, NapoliWorker worker,
                                                     List<Filter> filterList) throws TransportException {
        HornetQTransportConsumer hornetQTransportConsumer = new HornetQTransportConsumer();
        hornetQTransportConsumer.connector = connector;
        hornetQTransportConsumer.machine = machine;
        hornetQTransportConsumer.consumerList = new ArrayList<HQConsumerListener>();
        hornetQTransportConsumer.connectionParam = connectionParam;
        hornetQTransportConsumer.worker = worker;
        hornetQTransportConsumer.filterList = filterList;

        //each sub class need to handle getConnection;
        if (connector.getConsumerConnection(machine) != null) {
            NapoliConnection napoliConnection = connector.getConsumerConnection(machine);
            if (napoliConnection instanceof HQConnection) {
                hornetQTransportConsumer.connection = (HQConnection) napoliConnection;
            } else {
                throw new TransportException(" connection for machine " + machine + " is not HQConnection "
                        + napoliConnection);
            }
        } else {
            synchronized (lock) {
                if (connector.getConsumerConnection(machine) == null) {
                    hornetQTransportConsumer.connection = HQConnectionFactory
                            .createConnection(machine, connectionParam);
                    hornetQTransportConsumer.connector.addConsumerConnection(machine,
                            hornetQTransportConsumer.connection);
                } else {
                    hornetQTransportConsumer.connection = (HQConnection) connector.getConsumerConnection(machine);
                }
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("get connection to machine " + machine + " is " + hornetQTransportConsumer.connection);
        }
        return hornetQTransportConsumer;
    }

    public Message receive(int during) {
        throw new ReceiverException("Hornetq not support receive function,it is used by pendingnotify");
    }

    public synchronized void startListen() {
        closeAllConsumer();
        for (int i = 0; i < connectionParam.getReceiverSessions(); i++) {
            consumerList.add(startOneListener());
        }

    }

    public synchronized void stopListen() {
        if (logger.isInfoEnabled()) {
            logger.info("stopListen is called,all consumer will be closed");
        }
        closeAllConsumer();
    }

    //won't be used in hornetQ, keep it for interface compatible.
    public void onSessionException(Exception exception, Session session, MessageConsumer messageConsumer) {
    }

    public synchronized void heartbeat() {
        if (logger.isInfoEnabled()){
            logger.info("enter heartbeat!the machine["+machine+"]");
        }
        checkConnection();
        //refresh sessions hornetq will ping the server ttl;don't need refresh
        /*for (int i = 0; i < consumerList.size(); i++) {
            HQConsumerListener consumerWrap = consumerList.get(i);

            if (consumerWrap.isIdle()) {
                if (logger.isErrorEnabled()) {
                    logger.info("close idle consumer and reopen new one " + consumerWrap);
                }

                consumerWrap.close();
                HQConsumerListener listener = startOneListener();
                if (listener != null) {
                    consumerList.set(i, listener);
                }
            }

        }*/

        if (consumerList.size() == connectionParam.getReceiverSessions()) {
            return;
        }
        //调整大小，去除多余的session，增加缺少的session
        if (consumerList.size() > connectionParam.getReceiverSessions() && connectionParam.getReceiverSessions() > 0) {
            if (logger.isInfoEnabled()) {
                logger.info("adjustConsumers, close extra jms session, Queue(" + connectionParam.getName()
                        + "), Machine(" + machine.getIp()+":"+machine.getPort() + ") from " + consumerList.size() + " to "
                        + connectionParam.getReceiverSessions());
            }

            int closeCount = consumerList.size() - connectionParam.getReceiverSessions();
            while (closeCount > 0) {
                HQConsumerListener consumerWrap = consumerList.remove(0);
                consumerWrap.close();
                closeCount--;
                //this.connection.decreaseSessionCount();
            }
        } else if (consumerList.size() < connectionParam.getReceiverSessions()) {
            if (logger.isInfoEnabled()) {
                logger.info("adjustConsumers, open missing jms session, Queue(" + connectionParam.getName()
                        + "), Machine(" + machine.getIp()+":"+machine.getPort() + ") from " + consumerList.size() + " to "
                        + connectionParam.getReceiverSessions());
            }
            for (int i = consumerList.size(); i < connectionParam.getReceiverSessions(); i++) {
                HQConsumerListener listener = startOneListener();
                if (listener != null) {
                    consumerList.add(listener);
                }
            }
        }
    }

    private HQConsumerListener startOneListener() {
        HQConsumerListener handler = null;
        try {
            ClientSession session;
            synchronized (HQConnectionFactory.hqLock) {
                session = connection.getConnection().createSession(true, true, 1);
            }
            session.start();
            ClientConsumer messageConsumer;
            if (StringUtils.isBlank(connectionParam.getMessageSelector())){
                messageConsumer = session.createConsumer(connectionParam.getName());
            }else{
                messageConsumer = session.createConsumer(connectionParam.getName(),connectionParam.getMessageSelector());
            }
            //    this.setupRedeliveryCallBack(messageConsumer, connectionParam);
            final HornetQConsumerContext context = new HornetQConsumerContext(session, messageConsumer, connectionParam, filterList);
            handler = new HQConsumerListener(context, worker);
            if (logger.isInfoEnabled()) {
                logger.info("create HQConsumerListener " + handler);
            }

            messageConsumer.setMessageHandler(handler);
            //session.addFailureListener(new JMSFailureListener());
            if (logger.isInfoEnabled()) {
                logger.info("start one listener on " + machine.getIp()+":"+machine.getPort() + " queuename=" + connectionParam.getName()
                        + " current active listener is " + consumerList.size());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return handler;
    }

    /*private class JMSFailureListener implements SessionFailureListener {
        private int sessionCount;
        public JMSFailureListener() {
            sessionCount =0;
        }

        public synchronized void connectionFailed(final HornetQException me, boolean failedOver) {
            logger.warn("HornetQ consumer connection[" + machine.getAddress() + "] exception happened!,sessions["+sessionCount++ +"] will be closed!", me);
            //closeAllConsumer();
        }

        public void beforeReconnect(HornetQException arg0) {
            // TODO Auto-generated method stub
        }

    }*/

    public int getInstance() {
        if (consumerList != null) {
            return consumerList.size();
        }
        return 0;
    }

    /* public void setupRedeliveryCallBack(MessageConsumer messageConsumer, ConnectionParam connectionParam) {
    *//*
         * TODO: check how hornetQ support RedeliveryPolicy. final
         * RedeliveryPolicy redeliveryPolicy =
         * messageConsumer.getRedeliveryPolicy(); RedeliveryStrategy
         * redeliveryStrategy = connectionParam.getRedeliveryStrategy(); if
         * (redeliveryStrategy != null) {
         * redeliveryPolicy.setInitialRedeliveryDelay
         * (redeliveryStrategy.getInitialRedeliveryDelay());
         * connection.setBackOffMultiplier(redeliveryPolicy,
         * redeliveryStrategy.getRedeliveryMultiplier());
         * redeliveryPolicy.setUseExponentialBackOff
         * (redeliveryStrategy.isRedeliveryExponential());
         * redeliveryPolicy.setMaximumRedeliveries
         * (redeliveryStrategy.getMaxRedeliveries()); }
         *//*
    }
*/
    private void checkConnection() {
        if (connection.isIdle()){
            connector.removeIdleReceiverConnection();
        }
        if (connection == null || connection.isClose() || connection.getConnection().isClosed()) {
            recreateConnection();
        }
    }

    private void recreateConnection() {
        if (logger.isInfoEnabled()) {
            logger.info("enter recreateConnection[" + machine + "]");
        }
        if (connection != null && connection.getConnection() != null) {
            connection.getConnection().close();
        }
        try {
            connection = HQConnectionFactory.createConnection(machine, connectionParam);
            connector.addConsumerConnection(machine, connection);
            consumerList.clear();
            //closeAllConsumer();
            if (logger.isInfoEnabled()) {
                logger.info("heartbeat make hornetq connection[" + machine + "]has been reconnector");
            }
        } catch (Exception e) {
            logger.error("connection can't be recreate!", e);
            //throw new ReceiverException(e.getMessage(), e);
        }
    }

    private synchronized void closeAllConsumer() {
        Iterator it = consumerList.iterator();
        while (it.hasNext()) {
            HQConsumerListener consumerWrap = (HQConsumerListener) it.next();

            if (consumerWrap != null) {
                consumerWrap.close();
                if (logger.isInfoEnabled()) {
                    logger.info("stop one listener on " + machine.getIp()+":"+machine.getPort() + " queuename="
                            + connectionParam.getName());
                }
                //this.connection.decreaseSessionCount();
            }
        }
        consumerList.clear();
    }
}
