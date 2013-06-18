package com.alibaba.napoli.mqImpl.hornetQ;

import com.alibaba.napoli.domain.client.ClientMachine;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.api.core.client.SessionFailureListener;
import org.hornetq.core.client.impl.ClientSessionFactoryImpl;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;

import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.mqImpl.TransportException;

/**
 * User: yanny.wang Date: 2012/1/16
 */
public class HQConnectionFactory {
    private static final Log           logger          = LogFactory.getLog(HQConnectionFactory.class);

    public static final Object         hqLock          = new Object();

    //private static final AtomicInteger connectionCount = new AtomicInteger(0);

    /*
     * static{ org.hornetq.core.logging.Logger.setDelegateFactory(new
     * Log4jLogDelegateFactory()); }
     */

    public static HQConnection createConnection(ClientMachine machine, ConnectionParam params) throws TransportException {
        /*
         * String address = machine.getAddress(); if (address == null ||
         * params.getJmsPassword() == null ||
         * !NapoliMessageUtil.verifyUrl(address)) { throw new
         * IllegalArgumentException("illegal url[" + address + "]"); } if
         * (!address.contains(":")) { address += ":" + machine.getJmxPort(); }
         */
        final Map<String, Object> connectionParams = new HashMap<String, Object>();
        connectionParams.put(TransportConstants.HOST_PROP_NAME, machine.getIp());
        connectionParams.put(TransportConstants.PORT_PROP_NAME, machine.getPort());
        final TransportConfiguration transportConfiguration = new TransportConfiguration(
                NettyConnectorFactory.class.getName(), connectionParams);

        ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(transportConfiguration);
        //fix bug NP-267
        serverLocator.setConsumerWindowSize(0);
        serverLocator.setAckBatchSize(1);
        //serverLocator.setClientFailureCheckPeriod(10000); //10s一次ping
        serverLocator.setCallTimeout(5000); //减少当hornetq server stop时，session.close的时间过长
        //serverLocator.setReconnectAttempts(2);

        ClientSessionFactoryImpl clientSessionFactory = null;
        try {
            clientSessionFactory = (ClientSessionFactoryImpl) serverLocator.createSessionFactory();
            clientSessionFactory.addFailureListener(new ConnectionListener(machine));
            return new HQConnection(clientSessionFactory, params.getSendPoolsize());
        } catch (Exception e) {
            if (clientSessionFactory != null) {
                try {
                    clientSessionFactory.close();
                } catch (Exception ex) {
                    logger.error("close clientSessionFactory error!", e);
                }
            }
            try {
                serverLocator.close();
            } catch (Exception ex) {
                logger.error("close serverLocator error!", e);
            }
            throw new TransportException("create connection failed with exception", e);
        }
    }

    private static class ConnectionListener implements SessionFailureListener {
        private ClientMachine machine;

        private ConnectionListener(ClientMachine machine) {
            this.machine = machine;
        }

        public void connectionFailed(final HornetQException me, boolean failedOver) {
            logger.warn("HornetQ connection exception happened!machine=" + machine, me);
        }

        public void beforeReconnect(HornetQException arg0) {

        }

    }

}
