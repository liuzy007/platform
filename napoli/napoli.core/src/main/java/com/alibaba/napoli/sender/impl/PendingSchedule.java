package com.alibaba.napoli.sender.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.PendingNotify;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.client.async.inner.StoreItem;
import com.alibaba.napoli.common.util.TransactionSupport;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.connector.DestinationContext;

/**
 * User: heyman Date: 5/4/12 Time: 1:51 下午
 */
public class PendingSchedule implements Runnable {
    private static final Log   log              = LogFactory.getLog(PendingSchedule.class);
    private ConsoleConnector   connector;
    private DestinationContext destinationContext;
    private int                pendingTimeout;
    private int                pendingBatch     = NapoliConstant.PENDING_BATCH_SIZE;

    private PendingNotify      pendingNotify;

    private KVStore            kvStore;
    private String             name;
    private DefaultSenderImpl  sender;

    public PendingSchedule(ConsoleConnector connector, DestinationContext destinationContext,
                           PendingNotify pendingNotify, int pendingTimeout, DefaultSenderImpl sender) {
        this.connector = connector;
        this.destinationContext = destinationContext;
        this.pendingNotify = pendingNotify;
        this.pendingTimeout = pendingTimeout;
        this.name = destinationContext.getDestination().getName();
        this.kvStore = connector.getSenderKVStore(name);
        this.sender = sender;
    }

    @Override
    public void run() {
        final Map<String, StoreItem> data = kvStore.batchRead(pendingBatch);
        final int readDataCount = data.size();
        if (readDataCount == 0) {
            return;
        }

        for (final Map.Entry<String, StoreItem> entry : data.entrySet()) {
            final String key = entry.getKey();
            final StoreItem storeItem = entry.getValue();
            final NapoliMessage message = (NapoliMessage) storeItem.getContent();

            TransactionSupport.removeTransactionProperties(message);
            PendingNotify.PendingNotifyStateEnum state = null;
            try {
                state = pendingNotify.notify(message);
            } catch (Throwable e) {
                //catch any exception to avoid distort handle process.
                if (log.isWarnEnabled()) {
                    log.warn("pendingNotifier notify for queue[" + name + "] error," + e.getMessage());
                }
                continue;
            }
            if (state == PendingNotify.PendingNotifyStateEnum.COMMIT) {
                if (log.isDebugEnabled()) {
                    log.debug("Receiver a pending message from queue:" + name + " and message is " + message);
                }
                message.setStore2Local(false);//disable local store
                if (sender.sendMessage(message)) {
                    kvStore.delete(key);
                } else {
                    log.warn("pending send commit message error! message is" + message);
                }
            } else if (state == PendingNotify.PendingNotifyStateEnum.ROLLBACK) {
                kvStore.delete(key);
            }
        }
    }
}
