package com.alibaba.napoli.mqImpl.hornetQ;

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
import com.alibaba.napoli.mqImpl.TransportException;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.spi.TransportSender;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.SessionFailureListener;

/**
 * User: yanny.wang Date: 2012/1/16
 */
@Extension("hornetQ")
public class HornetQTransportSender implements TransportSender, TransactionConstants {
    protected static final Log           log          = LogFactory.getLog(HornetQTransportSender.class);
    protected ConsoleConnector           napoliContext;
    protected HQConnection               hqConnection;

    protected ClientMachine machine;
    protected ConnectionParam            param;
    private BlockingQueue<ClientSession> poolSessions;
    private static final int             MAX_SESSIONS = 100;

    public TransportSender createTransportSender(NapoliSenderContext senderContext) throws TransportException {
        HornetQTransportSender transportSender = new HornetQTransportSender();
        transportSender.napoliContext = senderContext.getConnector();
        transportSender.machine = senderContext.getMachine();
        transportSender.param = senderContext.getConnectionParam();
        transportSender.hqConnection = HQConnectionFactory.createConnection(senderContext.getMachine(),
                senderContext.getConnectionParam());
        transportSender.poolSessions = new ArrayBlockingQueue<ClientSession>(MAX_SESSIONS);
        return transportSender;
    }

    public String getName() {
        return "hornetQ";
    }

    public void send(String name, NapoliMessage message) throws TransportException {
        ClientSession session = createSession();
        try {
            ClientProducer producer = session.createProducer(name);
            ClientMessage clientMessage = HornetQMessageUtil.toClientMessage(message, session);
            producer.send(clientMessage);
            offerSessionToPool(producer, session);
        } catch (HornetQException e) {
            clearPoolSessions(session);
            throw new TransportException("ClientSession happened!machine=" + machine, e);
        }
    }

    public void send(String name, NapoliMessage message, Runnable bizCall) throws TransportException,
            BizInvokationException {
        ClientSession session = createSessionWithTransaction();
        try {
            ClientProducer producer = session.createProducer(name);
            ClientMessage clientMessage = HornetQMessageUtil.toClientMessage(message, session);
            producer.send(clientMessage);
        } catch (HornetQException e) {
            sessionRollbackAndClose(session);
            throw new TransportException("Transactional ClientSession Exception happened!machine=" + machine + ";"
                    + e.getMessage(), e);
        }

        try {
            bizCall.run();
        } catch (Exception e) {
            sessionRollbackAndClose(session);
            throw new BizInvokationException("BizCall Error Happened!" + e.getMessage(), e);
        }

        try {
            session.commit();
        } catch (HornetQException e) {
            //commit error,save to localdb
            KVStore kvStore = napoliContext.getSenderKVStore(name);
            kvStore.storeMessage(message);
            if (log.isInfoEnabled()) {
                log.info("commit message error!store the message to localstore! message is:" + message);
            }
        } finally {
            try {
                session.close();
            } catch (HornetQException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private void sessionRollbackAndClose(ClientSession session) {
        try {
            session.rollback();

        } catch (HornetQException ex) {
            log.warn(ex.getMessage(), ex);
        }
        try {
            session.close();
        } catch (HornetQException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public void close() {
        clearPoolSessions(null);
        if (hqConnection != null) {
            hqConnection.close();
        }
        napoliContext.removeSenderTransport(machine);
        if (log.isInfoEnabled()) {
            log.info("HornetQTransportSender has been closed! machine[" + machine.getIp()+":"+machine.getPort() + "] poolSessions="
                    + poolSessions.size());
        }
    }

    public NapoliConnection getNapoliConnection() {
        return hqConnection;
    }

    private ClientSession createSession() throws TransportException {
        checkConnection();
        if (!hqConnection.hasCapacity()) {
            throw new TransportException("session pool has full!");
        }

        ClientSession session;
        try {
            session = poolSessions.poll(NapoliConstant.SESSION_POOL_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new TransportException("get session from pool has been interrupted", e);
        }
        try {
            if (session == null || session.isClosed()) {
                session = hqConnection.getConnection().createSession(false, true, true);
                //session.addFailureListener(new SenderConnectionListener());
            }
            return session;
        } catch (HornetQException e) {
            close();
            throw new TransportException("create session error!machine=" + machine, e);
        }
    }

    private ClientSession createSessionWithTransaction() throws TransportException {
        checkConnection();
        ClientSession session;
        try {
            session = hqConnection.getConnection().createSession(false, false, true);
            //session.addFailureListener(new SenderConnectionListener());
        } catch (HornetQException e) {
            close();
            throw new TransportException("create transactional session error!machine=" + machine, e);
        }
        return session;
    }

    private void offerSessionToPool(ClientProducer producer, ClientSession session) {
        try {
            if (producer != null) {
                producer.close();
            }
        } catch (Exception e) {
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
                        } catch (Exception e1) {
                            log.error(e1.getMessage(), e1);
                        }
                        log.error("session is over the pool,pls check it");
                    }
                } catch (InterruptedException e) {
                    try {
                        session.close();
                    } catch (Exception e1) {
                        log.error(e1.getMessage(), e);
                    }
                }
            }
        } finally {
            hqConnection.returnCapacity();
        }
    }

    private void checkConnection() throws TransportException {
        if (hqConnection == null || hqConnection.isClose()) {
            if (log.isInfoEnabled()) {
                log.info("connection is lost,now recreate");
            }
            hqConnection = HQConnectionFactory.createConnection(machine, param);
            poolSessions.clear();
        }
    }

    private void clearPoolSessions(ClientSession usingSession) {
        if (usingSession != null) {
            try {
                usingSession.close();
            } catch (Exception e) {
                //ignore
            } finally {
                hqConnection.returnCapacity();
            }
        }
        ClientSession session;
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
                } catch (Exception e) {
                    //ignore
                } finally {
                    hqConnection.returnCapacity();
                }
            } else {
                break;
            }
        }
    }

    /*private class SenderConnectionListener implements SessionFailureListener {
        public void connectionFailed(final HornetQException me, boolean failedOver) {
            log.error(
                    "HornetQ session sender connection exception happened! for machine "
                            + machine.getAddress(), me);
            //close();
        }

        public void beforeReconnect(HornetQException arg0) {
            // TODO Auto-generated method stub

        }

    }*/

}
