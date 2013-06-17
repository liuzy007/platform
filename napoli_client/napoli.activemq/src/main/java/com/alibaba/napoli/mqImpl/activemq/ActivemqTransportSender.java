package com.alibaba.napoli.mqImpl.activemq;

import com.alibaba.napoli.client.async.BizInvokationException;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.Extension;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.common.util.TransactionConstants;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.mqImpl.NapoliConnection;
import com.alibaba.napoli.mqImpl.ProducerExceptionListener;
import com.alibaba.napoli.mqImpl.TransportException;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.spi.TransportSender;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.activemq.ActiveMQSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 12/8/11 Time: 2:05 下午
 */
@Extension("activemq")
public class ActivemqTransportSender implements TransportSender, TransactionConstants {
    private static final Log               log          = LogFactory.getLog(ActivemqTransportSender.class);
    private ConsoleConnector               napoliConnector;
    private AMQConnection                  napoliConnection;
    private ClientMachine                        machine;
    private ConnectionParam                param;
    //private static final Object            lock         = new Object();
    private static final int               MAX_SESSIONS = 100;

    private BlockingQueue<ActiveMQSession> poolSessions;

    //private final AtomicInteger sessionCreateCount = new AtomicInteger(0);

    public String getName() {
        return "activemq";
    }

    public TransportSender createTransportSender(NapoliSenderContext napoliSenderContext) throws TransportException {
        ActivemqTransportSender transportSender = new ActivemqTransportSender();
        transportSender.napoliConnector = napoliSenderContext.getConnector();
        transportSender.machine = napoliSenderContext.getMachine();
        transportSender.param = napoliSenderContext.getConnectionParam();
        transportSender.napoliConnection = AMQConnectionFactory.createConnection(napoliSenderContext.getMachine(),
                napoliSenderContext.getConnectionParam());
        transportSender.poolSessions = new ArrayBlockingQueue<ActiveMQSession>(MAX_SESSIONS);
        try {
            transportSender.napoliConnection.getConnection().setExceptionListener(
                    new ProducerExceptionListener(transportSender));
        } catch (JMSException e) {
            log.error(e.getMessage(), e);
        }
        return transportSender;
    }

    public void send(String name, NapoliMessage message) throws TransportException {
        ActiveMQSession session = createSession();
        try {
            Queue queue = session.createQueue(name);
            MessageProducer producer = session.createProducer(queue);
            Message jmsMessage = AMQMessageUtil.toJmsMessage(message, session);
            producer.send(queue, jmsMessage, DeliveryMode.PERSISTENT, message.getPriority(), message.getExpiration());
            offerSessionToPool(producer, session);
        } catch (JMSException e) {
            clearPoolSessions(session);
            throw new TransportException("JmsException happened!machine=" + machine, e);
        }
    }

    public void send(String name, NapoliMessage message, Runnable bizCall) throws TransportException,
            BizInvokationException {
        //undo two phases session for biz call send to avoid create session failed for second half message.
        /*String txId = UUID.randomUUID().toString();
        message.setProperty(NAPOLI_MSG_PRO_KEY_TX_ID, txId);
        message.setProperty(NAPOLI_MSG_PRO_KEY_TX_STATE, NAPOLI_MSG_PRO_VAL_TX_STATE_HALF);*/

        ActiveMQSession session = createSessionWithTransaction();
        Queue queue;
        MessageProducer producer;
        Message jmsMessage;
        try {
            queue = session.createQueue(name);
            producer = session.createProducer(queue);
            jmsMessage = AMQMessageUtil.toJmsMessage(message, session);
            producer.send(queue, jmsMessage, DeliveryMode.PERSISTENT, message.getPriority(), message.getExpiration());
        } catch (JMSException e) {
            sessionRollbackAndClose(session);
            throw new TransportException("JmsException happened!machine=" + machine, e);
        } 
        
        try {
            bizCall.run();
        } catch (Throwable t) {
            sessionRollbackAndClose(session);
            throw new BizInvokationException("BizCall Error Happened!", t);
        }

        try {
            //throw new JMSException("some thing else happened!");
            session.commit();
        } catch (JMSException e) {
            //commit error,save to localdb
            KVStore kvStore = napoliConnector.getSenderKVStore(name);
            kvStore.storeMessage(message);
            if (log.isInfoEnabled()){
                log.info("commit message error!store the message to localstore! message is:"+message,e);
            }
        } finally {
            try {
                session.close();
            } catch (JMSException e) {
                log.warn(e.getMessage(),e);
            }
        }

        
    }

    private void sessionRollbackAndClose(ActiveMQSession session){

        try {
            session.rollback();
        } catch (JMSException e) {
            log.warn(e.getMessage(),e);
        }
        try {
            session.close();
        } catch (JMSException e) {
            log.warn(e.getMessage(),e);
        }

    }

    public void close() {
        clearPoolSessions(null);
        if (napoliConnection != null) {
            napoliConnection.close();
        }
        napoliConnector.removeSenderTransport(machine);
        if (log.isInfoEnabled()) {
            log.info("the sender connection:" + machine + " be closed!");
        }
    }

    public NapoliConnection getNapoliConnection() {
        return napoliConnection;
    }

    private ActiveMQSession createSession() throws TransportException {
        checkConnection();
        //session数量控制,超时时间为5ms,如果超时后返回false,抛出SendRemoteException异常,表示pool满
        if (!napoliConnection.hasCapacity()) {
            throw new TransportException("session pool has full!");
        }

        ActiveMQSession session;
        try {
            session = poolSessions.poll(NapoliConstant.SESSION_POOL_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new TransportException("get session from pool error", e);
        }
        try {
            if (session == null || !session.isRunning()) {
                session = (ActiveMQSession) napoliConnection.getConnection().createSession(false,
                        Session.AUTO_ACKNOWLEDGE);
            }
            return session;
        } catch (JMSException e) {
            if (log.isWarnEnabled()){
                log.warn("create session error!the connection["+machine+"] will be closed");
            }
            close();
            throw new TransportException("create session error!machine=" + machine, e);
        }
    }

    private ActiveMQSession createSessionWithTransaction() throws TransportException{
        checkConnection();
        ActiveMQSession session;
        try {
            session = (ActiveMQSession) napoliConnection.getConnection().createSession(true,
                    Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            if (log.isWarnEnabled()){
                log.warn("create transactional session error!the connection["+machine+"] will be closed");
            }
            close();
            throw new TransportException("create transactional session error!machine=" + machine, e);
        }
        return session;
    }

    private void offerSessionToPool(MessageProducer producer, ActiveMQSession session) {
        try {
            if (producer != null) {
                producer.close();
            }
        } catch (JMSException e) {
            if (log.isWarnEnabled()) {
                log.warn("producer offerSessionToPool failed.", e);
            }
        }
        try {
            if (session != null) {
                try {
                    if (!poolSessions.offer(session, NapoliConstant.SESSION_POOL_TIMEOUT, TimeUnit.MILLISECONDS)) {
                        try {
                            session.close();
                        } catch (JMSException e1) {
                            log.error(e1.getMessage(), e1);
                        }
                        log.error("session is over the pool,pls check it");
                    }
                } catch (InterruptedException e) {
                    try {
                        session.close();
                    } catch (JMSException e1) {
                        log.error(e1.getMessage(), e);
                    }
                }
            }
        } finally {
            napoliConnection.returnCapacity();
        }
    }

    private void checkConnection() throws TransportException {
        if (napoliConnection == null || napoliConnection.isClose()) {
            if (log.isWarnEnabled()) {
                log.warn("connection is lost,now recreate");
            }
            napoliConnection = AMQConnectionFactory.createConnection(machine, param);
            poolSessions.clear();
        }
    }

    private void clearPoolSessions(ActiveMQSession usingSession) {
        if (usingSession != null) {
            try {
                usingSession.close();
            } catch (JMSException e) {
                //ignore
            } finally {
                napoliConnection.returnCapacity();
            }
        }
        ActiveMQSession session;
        while (true) {
            try {
                session = poolSessions.poll(2, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                poolSessions.clear();
                break;
            }
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    //ignore
                } finally {
                    napoliConnection.returnCapacity();
                }
            } else {
                break;
            }
        }
    }
}
