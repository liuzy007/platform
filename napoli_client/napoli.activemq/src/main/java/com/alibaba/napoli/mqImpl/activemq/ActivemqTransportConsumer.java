package com.alibaba.napoli.mqImpl.activemq;

import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.Extension;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.mqImpl.ConsumerContext;
import com.alibaba.napoli.mqImpl.NapoliConnection;
import com.alibaba.napoli.mqImpl.ConsumerExceptionListener;
import com.alibaba.napoli.mqImpl.TransportException;
import com.alibaba.napoli.receiver.ReceiverException;
import com.alibaba.napoli.receiver.RedeliveryStrategy;
import com.alibaba.napoli.receiver.filter.Filter;
import com.alibaba.napoli.spi.TransportConsumer;
import java.util.ArrayList;
import java.util.List;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 12/14/11 Time: 3:18 下午
 */
@Extension("activemq")
public class ActivemqTransportConsumer implements TransportConsumer {
    private static final Log    logger = LogFactory.getLog(ActivemqTransportConsumer.class);
    private static final Object lock   = new Object();

    private AMQConnection       connection;
    private List<ConsumerWrap>  consumerList;

    private ConnectionParam     connectionParam;
    private NapoliWorker        worker;
    private ClientMachine machine;
    private ConsoleConnector    connector;

    private List<Filter>        filterList;

    private ActiveMQSession     session;
    private MessageConsumer     messageConsumer;

    public String getName() {
        return "activemq";
    }

    public TransportConsumer createTransportConsumer(ConsoleConnector connector, ClientMachine machine,
                                                     ConnectionParam connectionParam, NapoliWorker worker,
                                                     List<Filter> filterList) throws TransportException {
        ActivemqTransportConsumer activemqTransportConsumer = new ActivemqTransportConsumer();
        activemqTransportConsumer.connector = connector;
        activemqTransportConsumer.machine = machine;
        activemqTransportConsumer.consumerList = new ArrayList<ConsumerWrap>();

        activemqTransportConsumer.connectionParam = connectionParam;
        activemqTransportConsumer.worker = worker;
        activemqTransportConsumer.filterList = filterList;

        if (connector.getConsumerConnection(machine) != null) {
            NapoliConnection napoliConnection = connector.getConsumerConnection(machine);
            if (napoliConnection instanceof AMQConnection) {
                activemqTransportConsumer.connection = (AMQConnection) connector.getConsumerConnection(machine);
            } else {
                throw new TransportException(" connection for machine " + machine + " is not AMQConnection "
                        + napoliConnection);
            }
        } else {
            synchronized (lock) {
                if (activemqTransportConsumer.connector.getConsumerConnection(machine) == null) {
                    activemqTransportConsumer.connection = AMQConnectionFactory.createConnection(machine,
                            connectionParam);
                    try {
                        activemqTransportConsumer.connection.getConnection().setExceptionListener(new ConsumerExceptionListener(activemqTransportConsumer));
                    } catch (JMSException e) {
                        logger.error(e.getMessage(),e);
                    }
                    activemqTransportConsumer.connector.addConsumerConnection(machine,
                            activemqTransportConsumer.connection);
                } else {
                    activemqTransportConsumer.connection = (AMQConnection) connector.getConsumerConnection(machine);
                }
            }
        }
        return activemqTransportConsumer;
    }

    public Message receive(int during) {
        Message message = null;
        try {
            /*
             * if (messageConsumer != null) { messageConsumer.close(); } if
             * (session != null) { session.close(); }
             */
            ActiveMQConnection jmsconnection = connection.getConnection();
            if (jmsconnection == null) {
                return null;
            }
            if (messageConsumer == null) {
                session = (ActiveMQSession) jmsconnection.createSession(connectionParam.isTransacted(),
                        connectionParam.getAcknowledgeMode());
                final Queue queue = session.createQueue(connectionParam.getName());
                messageConsumer = session.createConsumer(queue, connectionParam.getMessageSelector());
            }
            message = messageConsumer.receive(during);
        } catch (JMSException e) {
            logger.error(e.getMessage(), e);
        } /*
           * finally { try { if (messageConsumer != null) {
           * messageConsumer.close(); } if (session != null) { session.close();
           * } if (logger.isInfoEnabled()) {
           * logger.info("close receive session:" + connection.getConnection());
           * } } catch (JMSException e) { logger.warn(e.getMessage(), e); } }
           */
        return message;
    }

    public synchronized void startListen() {
        closeAllConsumer();
        for (int i = 0; i < connectionParam.getReceiverSessions(); i++) {
            consumerList.add(startOneListener());
        }
    }

    public synchronized void stopListen() {
        try {
            if (messageConsumer != null) {
                messageConsumer.close();
            }
        } catch (JMSException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            if (session != null) {
                session.close();
            }
        } catch (JMSException e) {
            logger.error(e.getMessage(), e);
        }
        messageConsumer = null;
        session = null;
        closeAllConsumer();
    }

    public void onSessionException(Exception exception, Session session, MessageConsumer messageConsumer) {
        if (logger.isWarnEnabled()) {
            logger.warn("session onException, close jms session, Queue(" + connectionParam.getName() + "), Machine("
                    + machine.getIp()+":"+machine.getPort() + "): " + exception.getMessage(), exception);
        }
        try {
            messageConsumer.close();
            session.close();
        } catch (JMSException e) {
            logger.warn(e.getMessage(), e);
        }

    }

    public synchronized void heartbeat() {
        checkConnection();
        for (int i = 0; i < consumerList.size(); i++) {
            ConsumerWrap consumerWrap = consumerList.get(i);
            if (consumerWrap.isIdle()) {
                consumerWrap.close();
                consumerList.set(i, startOneListener());
            }
        }
        if (consumerList.size() == connectionParam.getReceiverSessions()) {
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("consumer listen size=" + consumerList.size() + " and needsessions="
                    + connectionParam.getReceiverSessions());
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
                ConsumerWrap consumerWrap = consumerList.remove(0);
                consumerWrap.close();
                closeCount--;
            }
        } else if (consumerList.size() < connectionParam.getReceiverSessions()) {
            if (logger.isInfoEnabled()) {
                logger.info("adjustConsumers, open missing jms session, Queue(" + connectionParam.getName()
                        + "), Machine(" + machine.getIp()+":"+machine.getPort() + ") from " + consumerList.size() + " to "
                        + connectionParam.getReceiverSessions());
            }
            for (int i = consumerList.size(); i < connectionParam.getReceiverSessions(); i++) {
                consumerList.add(startOneListener());
            }
        }
    }

    public int getInstance() {
        int count = 0;
        for (ConsumerWrap consumerWrap : consumerList) {
            if (consumerWrap != null) {
                count++;
            }
        }
        return count;
    }

    private ConsumerWrap startOneListener() {
        ConsumerWrap consumerWrap = null;
        try {
            ActiveMQSession session = (ActiveMQSession) connection.getConnection().createSession(
                    connectionParam.isTransacted(), connectionParam.getAcknowledgeMode());

            Destination destination = session.createQueue(connectionParam.getName());
            ActiveMQMessageConsumer messageConsumer = (ActiveMQMessageConsumer) session.createConsumer(destination,
                    connectionParam.getMessageSelector());

            final RedeliveryPolicy redeliveryPolicy = messageConsumer.getRedeliveryPolicy();
            RedeliveryStrategy redeliveryStrategy = connectionParam.getRedeliveryStrategy();
            if (redeliveryStrategy != null) {
                redeliveryPolicy.setInitialRedeliveryDelay(redeliveryStrategy.getInitialRedeliveryDelay());
                redeliveryPolicy.setUseExponentialBackOff(redeliveryStrategy.isRedeliveryExponential());
                redeliveryPolicy.setMaximumRedeliveries(redeliveryStrategy.getMaxRedeliveries());
                connection.setBackOffMultiplier(redeliveryPolicy, redeliveryStrategy.getRedeliveryMultiplier());
            }

            final ConsumerContext context = new ConsumerContext(session, messageConsumer, connectionParam, filterList);
            AMQConsumerListener listener = new AMQConsumerListener(this, context, worker);
            messageConsumer.setMessageListener(listener);
            consumerWrap = new ConsumerWrap(session, messageConsumer, listener);
            if (logger.isInfoEnabled()) {
                logger.info("start one listener on " + machine.getIp()+":"+machine.getPort() + " queuename=" + connectionParam.getName());
            }
        } catch (JMSException e) {
            logger.error(e.getMessage(), e);
        }
        return consumerWrap;
    }

    private class ConsumerWrap {

        private ActiveMQSession     session;
        private MessageConsumer     messageConsumer;
        private AMQConsumerListener messageListener;

        public ConsumerWrap(ActiveMQSession session, MessageConsumer messageConsumer,
                            AMQConsumerListener messageListener) {
            this.session = session;
            this.messageConsumer = messageConsumer;
            this.messageListener = messageListener;
        }

        public boolean isIdle() {
            return (messageListener.isIdle() || !session.isRunning());
        }

        public void close() {
            try {
                messageConsumer.close();
            } catch (JMSException e) {
                logger.warn(e.getMessage(), e);
            }
            try {
                session.close();
            } catch (JMSException e) {
                if (logger.isInfoEnabled()){
                    logger.info(e.getMessage(), e);
                }
            }
        }
    }

    private void checkConnection() {
        if (connection == null || connection.isClose()) {
            recreateConnection();
        }
        try {
            Session session = connection.getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
            session.close();
        } catch (Exception e) {
            recreateConnection();
        }
    }

    private void recreateConnection() {
        if (connection != null && connection.getConnection() != null) {
            try {
                connection.getConnection().close();
            } catch (Exception e) {
                //ignore
            }
        }
        try {
            connection = AMQConnectionFactory.createConnection(machine, connectionParam);
            connection.getConnection().setExceptionListener(new ConsumerExceptionListener(this));
            connector.addConsumerConnection(machine, connection);
            closeAllConsumer();
            if (logger.isInfoEnabled()) {
                logger.info("heartbeat make activemq connection["+machine+ "]has been reconnector");
            }
        } catch (Exception e) {
            logger.info("connection can't be recreate!", e);
            throw new ReceiverException(e.getMessage(), e);
        }
    }

    private void closeAllConsumer() {
        for (ConsumerWrap consumerWrap : consumerList) {
            if (consumerWrap != null) {
                consumerWrap.close();
                if (logger.isInfoEnabled()) {
                    logger.info("stop one listener on " + machine.getIp()+":"+machine.getPort() + " queuename="
                            + connectionParam.getName());
                }
            }
        }
        consumerList.clear();
    }
}
