package com.alibaba.napoli.sender.impl;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.PendingNotify;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.util.NapoliMessageUtil;
import com.alibaba.napoli.common.util.TransactionSupport;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.connector.DestinationContext;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.mqImpl.TransportException;
import com.alibaba.napoli.mqImpl.TransportFactory;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.sender.SenderException;
import com.alibaba.napoli.spi.TransportConsumer;
import com.alibaba.napoli.spi.TransportSender;

import java.util.List;

import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 12/2/11 Time: 5:16 下午
 */
public class PendingNotifySchedule implements Runnable {
    private static final Log   log              = LogFactory.getLog(PendingNotifySchedule.class);
    private ConsoleConnector   connector;
    private DestinationContext destinationContext;
    private int                pendingTimeout;
    private int                pendingBatch     = NapoliConstant.PENDING_BATCH_SIZE;

    private PendingNotify      pendingNotify;
    private TransportFactory   transportFactory = new TransportFactory();

    public PendingNotifySchedule(ConsoleConnector connector, DestinationContext destinationContext,
                                 PendingNotify pendingNotify, int pendingTimeout) {
        this.connector = connector;
        this.destinationContext = destinationContext;
        this.pendingNotify = pendingNotify;
        this.pendingTimeout = pendingTimeout;
    }

    public void run() {
        try {
            ClientQueue queueEntity = (ClientQueue) destinationContext.getDestination();
            List<ClientMachine> machineList = destinationContext.getReceiveMachineList();

            //handle pending messages
            for (ClientMachine m : machineList) {
                TransportConsumer pendingConsumer;
                try {
                    ConnectionParam connectionParam = new ConnectionParam();
                    connectionParam.setTransacted(false);
                    connectionParam.setAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
                    connectionParam.setName(queueEntity.getName());
                    connectionParam.setMessageSelector(TransactionSupport.genPendingSelector());
                    pendingConsumer = transportFactory.getTransportConsumer(connector, m, connectionParam, null, null);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    continue;
                }

                try {
                    for (int i = 0; i < pendingBatch; i++) {
                        if (!pendingOneMessageNotify(pendingConsumer, queueEntity, m)) {
                            break;
                        }
                    }
                } finally {
                    pendingConsumer.stopListen();
                }
            }

            //handle rollback half messages
            for (ClientMachine m : machineList) {

                if (log.isInfoEnabled()) {
                    log.info("try to rollback messages for machine " + m);
                }

                TransportConsumer rollbackConsumer;
                try {
                    ConnectionParam connectionParam = new ConnectionParam();
                    connectionParam.setTransacted(false);
                    connectionParam.setAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
                    connectionParam.setName(queueEntity.getName());
                    connectionParam.setMessageSelector(TransactionSupport.genRollbackSelector());
                    rollbackConsumer = transportFactory.getTransportConsumer(connector, m, connectionParam, null, null);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    continue;
                }

                try {
                    for (int i = 0; i < pendingBatch; i++) {
                        if (rollbackConsumer.receive(pendingTimeout) == null) {
                            if (log.isInfoEnabled()) {
                                if (i > 0) {
                                    log.info("no more rollback messages, exit");
                                } else {
                                    log.info("no rollback messages, exit");
                                }

                            }
                            break;
                        } else {
                            if (log.isInfoEnabled()) {
                                log.info("clean one rollback message " + i + " ******************************");
                            }
                        }
                    }
                } finally {
                    rollbackConsumer.stopListen();
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private boolean pendingOneMessageNotify(TransportConsumer consumer, ClientQueue queueEntity, ClientMachine m) {

        try {
            Message msg = consumer.receive(pendingTimeout);

            if (msg == null) {
                return false;
            }
            NapoliMessage napoliMessage = NapoliMessageUtil.fromJmsMessage(msg);
            TransactionSupport.removeTransactionProperties(napoliMessage);
            PendingNotify.PendingNotifyStateEnum state = null;
            try {
                state = pendingNotify.notify(napoliMessage);
            } catch (Throwable e) {
                //catch any exception to avoid distort handle process.
                if (log.isWarnEnabled()) {
                    log.warn("pendingNotifier notify for queue[" + queueEntity.getName() + "] error," + e.getMessage());
                }
            }
            if (state == PendingNotify.PendingNotifyStateEnum.COMMIT) {
                if (log.isDebugEnabled()) {
                    log.debug("Receiver a pending message from queue:" + queueEntity.getName() + " and message is "
                            + napoliMessage);
                }
                boolean commit = true;
                try {
                    napoliMessage.setStore2Local(false);//disable local store
                    if (m == null) {
                        throw new SenderException("There is no machine for queue=" + queueEntity.getName());
                    }
                    NapoliSenderContext senderContext = new NapoliSenderContext(queueEntity.getName(), m,
                            napoliMessage, destinationContext.getConnectionParam(), connector);
                    TransportSender transportSender = transportFactory.getTrasportSender(senderContext);
                    transportSender.send(queueEntity.getName(), napoliMessage);
                } catch (TransportException e) {
                    commit = false;
                }
                if (commit) {
                    msg.acknowledge();
                }
            } else if (state == PendingNotify.PendingNotifyStateEnum.ROLLBACK) {
                msg.acknowledge();//do nothing when rollback
            }
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
        }
        return true;
    }
}
