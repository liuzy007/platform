package com.alibaba.napoli.mqImpl.hornetQ;

import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.mqImpl.NapoliConnection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.client.impl.ClientSessionFactoryImpl;

/**
 * User: yanny.wang Date: 2012/1/16
 */
public class HQConnection implements NapoliConnection<ClientSessionFactoryImpl> {
    private static final Log         log            = LogFactory.getLog(HQConnection.class);

    //private ServerLocator        serverLocator;
    private ClientSessionFactoryImpl clientSessionFactory;

    private boolean                  isClose        = true;
    private long                     lastUsedTime;
    private Semaphore                sessionSemaphore;
    //set default to true for fix bug NP-223.
    private boolean                  sessionControl = true;

    //AtomicInteger                sessionCount   = new AtomicInteger();

    //  private static Object        lock           = new Object();

    public HQConnection(ClientSessionFactoryImpl clientSessionFactory, int capacity) {
        //this.serverLocator = serverLocator;
        this.clientSessionFactory = clientSessionFactory;
        if (capacity == 0) {
            sessionControl = false;
        }
        if (capacity < 0 || capacity > 100) {
            sessionSemaphore = new Semaphore(5);
        } else {
            sessionSemaphore = new Semaphore(capacity);
        }
        isClose = false;
    }

    public ClientSessionFactoryImpl getConnection() {
        lastUsedTime = System.currentTimeMillis();
        return clientSessionFactory;
    }

    public  void close() {
        ServerLocator serverLocator = clientSessionFactory.getServerLocator();
        if (clientSessionFactory != null) {
            try {
                clientSessionFactory.close();
            } catch (Exception e) {
                log.warn("close clientSessionFactory error!", e);
            }
        }
        if (serverLocator != null) {
            serverLocator.close();
        }
        isClose = true;
    }

    /*
     * public ClientSession createSession(final boolean autoCommitSends, final
     * boolean autoCommitAcks, final int ackBatchSize) throws HornetQException {
     * lastUsedTime = System.currentTimeMillis(); //HornetQ ClientSessionFactory
     * close & createSession seem to have block. ClientSession session;
     * synchronized (HQConnectionFactory.hqLock) { session =
     * this.clientSessionFactory.createSession(autoCommitSends, autoCommitAcks,
     * ackBatchSize); } //this.sessionCount.incrementAndGet(); return session; }
     */

    public boolean isClose() {
        return isClose;
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }

    public boolean isIdle() {
        return clientSessionFactory.numSessions() == 0;
    }

    /*
     * public void decreaseSessionCount() { this.sessionCount.decrementAndGet();
     * }
     */

    public boolean hasCapacity() {
        lastUsedTime = System.currentTimeMillis();
        if (sessionControl && sessionSemaphore != null) {
            try {
                if (!sessionSemaphore.tryAcquire(NapoliConstant.SESSION_POOL_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    return false;
                }
            } catch (InterruptedException e) {
                return false;
            }
        }
        return true;
    }

    public void returnCapacity() {
        if (sessionControl && sessionSemaphore != null) {
            sessionSemaphore.release();
        }
    }
}
