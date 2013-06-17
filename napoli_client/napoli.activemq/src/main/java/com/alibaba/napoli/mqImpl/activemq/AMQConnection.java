package com.alibaba.napoli.mqImpl.activemq;

import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.mqImpl.NapoliConnection;

import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 12/7/11 Time: 10:11 上午
 */
public class AMQConnection implements NapoliConnection<ActiveMQConnection> {
    private static final Log   log            = LogFactory.getLog(AMQConnection.class);
    private ActiveMQConnection connection;
    private long               lastUsedTime;
    private Semaphore          sessionSemaphore;
    //set default to true for fix bug NP-223.
    private boolean            sessionControl = true;

    public AMQConnection(ActiveMQConnection connection, int capacity) {
        this.connection = connection;
        if (capacity == 0) {
            sessionControl = false;
        }
        if (capacity < 0 || capacity > 100) {
            sessionSemaphore = new Semaphore(5);
        } else {
            sessionSemaphore = new Semaphore(capacity);
        }
    }

    public ActiveMQConnection getConnection() {
        lastUsedTime = System.currentTimeMillis();
        return connection;
    }

    public void close() {
        if (log.isDebugEnabled()){
            log.debug("connection close is be called!",new RuntimeException("connection is closed!"));
        }
        if (connection == null || connection.isClosed() || connection.isClosing()) {
            return;
        }
        try {
            connection.stop();
        } catch (JMSException e) {
            log.warn("stop connection error!", e);
        }

        try {
            connection.close();
        } catch (JMSException e) {
            log.error("close connection error!", e);
        }
    }

    public boolean isClose() {
        return connection == null || connection.isClosed() || connection.isClosing();
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }

    public boolean isIdle() {
        return connection.getConnectionStats().getSessions().length == 0;
    }

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
            /*if (sessionSemaphore.availablePermits() >= 0) {
                sessionSemaphore.release();
            }*/
        }
    }

    /**
     * @param redeliveryPolicy policy
     * @param redeliveryMultiplier2
     *            使用反射机制调用RedeliveryPolicy.setBackOffMultiplier方法，因为activmq-core
     *            －5.2.0.jar中参数是short，而5.3.1中使用的是double<br>
     */
    public void setBackOffMultiplier(RedeliveryPolicy redeliveryPolicy, double redeliveryMultiplier2) {
        String classPath = "org.apache.activemq.RedeliveryPolicy";
        Class<?> redeliveryPolicyClass;
        Method method1 = null;
        try {
            redeliveryPolicyClass = Class.forName(classPath);
            Method[] methods = redeliveryPolicyClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("setBackOffMultiplier")) {
                    method1 = method;
                }
            }
            if (method1 == null){
                throw new RuntimeException("setBackOffMultiplier can't be find!");
            }
            Class<?>[] bofp = method1.getParameterTypes();
            Class<?> componentType = bofp[0];
            if (componentType.equals(short.class)) {
                Method methodshort = redeliveryPolicyClass.getMethod("setBackOffMultiplier", short.class);
                methodshort.invoke(redeliveryPolicy, (short) redeliveryMultiplier2);
            } else {
                Method methodshort = redeliveryPolicyClass.getMethod("setBackOffMultiplier", double.class);
                methodshort.invoke(redeliveryPolicy, redeliveryMultiplier2);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
