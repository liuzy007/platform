package com.alibaba.napoli.sender.impl;

import com.alibaba.napoli.client.async.SendResult;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.client.NapoliClient;
import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.spi.StartupListener;
import com.alibaba.napoli.client.async.BizInvokationException;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.PendingNotify;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.common.util.ExtensionLoader;
import com.alibaba.napoli.common.util.NapoliMessageUtil;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.domain.client.ClientTopic;
import com.alibaba.napoli.domain.client.ClientVirtualTopic;
import com.alibaba.napoli.metamorphosis.client.producer.MessageProducer;
import com.alibaba.napoli.metamorphosis.exception.MetaClientException;
import com.alibaba.napoli.mqImpl.metaq.MetaqFactory;
import com.alibaba.napoli.mqImpl.metaq.MetaqSender;
import com.alibaba.napoli.sender.NapoliResult;
import com.alibaba.napoli.sender.NapoliSender;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.sender.Sender;

/**
 * User: heyman Date: 11/24/11 Time: 5:05 下午
 */
public class DefaultSenderImpl extends NapoliClient implements Sender {
    private static final Log                                log                  = LogFactory
                                                                                         .getLog(DefaultSenderImpl.class);

    protected PendingNotify                                 pendingNotifier;
    protected int                                           pendingBatch         = 1000;
    protected int                                           pendingInterval      = 600000;
    protected int                                           pendingTimeout       = 100;
    private volatile ScheduledExecutorService               pendingNotifyExecutor;

    private ConcurrentMap<String, ScheduledExecutorService> reprocessFutureMap   = new ConcurrentHashMap<String, ScheduledExecutorService>();
    private ConnectionParam                                 connectionParam;

    private NapoliSender                                    napoliSender;
    private MessageProducer                                 metaqProducer;
    private boolean                    order                 = false;

    public static final String                              PROP_PENDINGBATCH    = "pendingBatch";
    public static final String                              PROP_PENDINGINTERVAL = "pendingInterval";
    public static final String                              PROP_PENDINGTIMEOUT  = "pendingTimeout";

    /**
     * 与线程关联的事务上下文
     */
    //final protected ThreadLocal<TransactionContext>         transactionContext   = new ThreadLocal<TransactionContext>();

    public DefaultSenderImpl() {
    }

    public void init() throws NapoliClientException {
        if (destinationContext != null){
            log.warn("Sender["+name+"] has inited!");
            return;
        }
        super.init();
        try {
            if (pendingNotifier != null
                    && destinationContext != null
                    && (destinationContext.getDestination() instanceof ClientQueue || destinationContext
                            .getDestination() instanceof ClientTopic)) {
                if (pendingNotifyExecutor != null) {
                    pendingNotifyExecutor.shutdown();
                }
                //start pending notify schedule
                /*
                 * Runnable pendingTask = new PendingNotifySchedule(connector,
                 * destinationContext, pendingNotifier, pendingTimeout);
                 */
                Runnable pendingTask = new PendingSchedule(connector, destinationContext, pendingNotifier,
                        pendingTimeout, this);
                pendingNotifyExecutor = NapoliMessageUtil.startSchedule("PendingNotify of " + name + "_" + this,
                        pendingTask, pendingInterval, pendingInterval);
            }
            //startReprocess schedule
            refreshReprocess();
            assert destinationContext != null;
            destinationContext.attachSender(this);

            //init connectionParam
            if (connectionParam == null) {
                connectionParam = connector.getConnectionParam();
                /*
                 * if (storeEnable){ connectionParam.setNeedPersistence(true); }
                 */
            }
            connectionParam.setStoreEnable(storeEnable);
            destinationContext.setConnectionParam(connectionParam);
        } catch (Exception e) {
            throw new NapoliClientException("DefaultSender init happen error!", e);
        }

        //metaq sender 
        if (destinationContext.getDestination() instanceof ClientTopic) {
            String zkAddress = ((ClientTopic) destinationContext.getDestination()).getZks();
            if (zkAddress == null) {
                throw new NapoliClientException("zkaddrss is null");
            }
            metaqProducer = MetaqFactory.getMessageProducer(zkAddress, destinationContext.getDestination().getName(), order);
            MetaqSender metaqSender = new MetaqSender(metaqProducer);
            napoliSender = ExtensionLoader.buildSenderFilterChain(filterChain.split(","), metaqSender);
        } else {
            napoliSender = ExtensionLoader.buildSenderFilterChain(filterChain.split(","), new SenderMq());
        }

        //begin startupListener only for queue & vtopic
        ServiceLoader<StartupListener> startupListeners = ServiceLoader.load(StartupListener.class);
        for (StartupListener startupListener : startupListeners) {
            try {
                startupListener.startup(this);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void refreshSchedule() {
        //only virtualtopic need to refresh
        if (destinationContext.getDestination() instanceof ClientVirtualTopic) {
            refreshReprocess();
        }
    }

    private void refreshReprocess() {
        for (Map.Entry<String, ScheduledExecutorService> entry : reprocessFutureMap.entrySet()) {
            //String queueName = entry.getKey();
            ScheduledExecutorService future = entry.getValue();
            future.shutdown();
        }
        reprocessFutureMap.clear();

        if (storeEnable && pendingNotifier == null) {
            if (destinationContext.getDestination() instanceof ClientVirtualTopic) {
                ClientVirtualTopic virtualTopic = (ClientVirtualTopic) destinationContext.getDestination();
                for (final ClientQueue queue : virtualTopic.getClientQueueList()) {
                    String queueName = queue.getName();
                    createReprocess(queueName);
                }
            } else {
                createReprocess(name);
            }
        }
    }

    private synchronized void createReprocess(String queueName) {
        KVStore kvStore = ConsoleConnector.getorCreateBdbKvStore(connector.getStorePath(), queueName,
                NapoliConstant.CLIENT_TYPE_SENDER);

        // start reprocess schedule
        ScheduledExecutorService reprocessFuture = NapoliMessageUtil.startSchedule("sender Reprocess handle schedule["
                + queueName + "]", new ResendSchedule(kvStore, connector.getDataBatchReadCount(), this),
                reprocessInterval, reprocessInterval);
        reprocessFutureMap.put(queueName, reprocessFuture);

    }

    public void close() {
        if (!closed) {
            for (Map.Entry<String, ScheduledExecutorService> entry : reprocessFutureMap.entrySet()) {
                //String queueName = entry.getKey();
                ScheduledExecutorService future = entry.getValue();
                future.shutdown();
            }
            reprocessFutureMap.clear();
            if (pendingNotifyExecutor != null) {
                pendingNotifyExecutor.shutdown();
            }
            super.close();
        }
    }

    public boolean sendMessage(Serializable message) {
        return sendMessage(new NapoliMessage(message));
    }

    public boolean sendMessage(NapoliMessage message) {
        return sendNapoliMessage(message).success();
    }
    
    public SendResult sendNapoliMessage(NapoliMessage message){
        if (isClosed()) {
            log.error("Sender[" + name + "] has been closed");
            return SendResult.newInstance(true,null);
        }
        Map<String, ClientMachine> machineMap = new HashMap<String, ClientMachine>();
        if (message.isLocalStoreMessage() && message.getQueueName() != null && !"N/A".equals(message.getQueueName())){
            String queueOfTopic = message.getQueueName();
            ClientMachine machine = destinationContext.getSendMachine(queueOfTopic);
            if (machine != null){
                machineMap.put(queueOfTopic,machine); 
            }
        }else{
            machineMap = destinationContext.getSendMachineMap();
        }

        //没有机器可用
        if (machineMap.size() == 0) {
            log.error("no available machine, check the config!vtopic[" + name + "] hasn't queue");
            return SendResult.newInstance(false,new NapoliClientException("no available machine, check the config!vtopic[" + name + "] hasn't queue"));
        }
        
        SendResult sendResult = SendResult.newInstance(false,null);
        //boolean ret = false;
        for (Map.Entry<String, ClientMachine> entry : machineMap.entrySet()) {
            String queueName = entry.getKey();
            ClientMachine machine = entry.getValue();
            /*
             * if (machine == null){ log.error("There is no machine for queue="
             * + queueName); ret = false; continue; }
             */
            //when machine is null && storeeable=true,the message should be storelocal 
            message.setStore2Local(storeEnable);
            NapoliSenderContext senderContext = new NapoliSenderContext(queueName, machine, message, connectionParam,
                    connector);
            NapoliResult result = napoliSender.sendMessage(senderContext);
            if (result.isSuccess()) {
                sendResult.setSuccess(true);
                sendResult.setMsgId(result.getMsgId());
            }
            result.printError();
        }
        return sendResult;
    }

    public void sendMessage(Serializable message, Runnable bizCallback) throws NapoliClientException,
            BizInvokationException {
        sendMessage(new NapoliMessage(message), bizCallback);
    }

    public void sendMessage(NapoliMessage message, Runnable bizCallback) throws NapoliClientException,
            BizInvokationException {
        if (isClosed()) {
            throw new NapoliClientException("Sender[" + name + "] has been closed");
        }
        if (bizCallback == null || message == null) {
            throw new NapoliClientException("message or bizCallback is null");
        }
        Map<String, ClientMachine> machineMap = destinationContext.getSendMachineMap();

        if (machineMap.size() != 1) {
            throw new NapoliClientException("please check the name=" + name
                    + " is a queue and has the sendable machine");
        }
        String queueName = null;
        ClientMachine machine = null;
        for (Map.Entry<String, ClientMachine> entry : machineMap.entrySet()) {
            queueName = entry.getKey();
            machine = entry.getValue();
        }
        if (machine == null) {
            throw new NapoliClientException("There is no machine for queue=" + queueName);
        }
        message.setStore2Local(false);
        NapoliSenderContext senderContext = new NapoliSenderContext(queueName, machine, message, connectionParam,
                connector);
        senderContext.setBizCall(bizCallback);
        NapoliResult result = napoliSender.sendMessage(senderContext);
        result.printError();
        if (!result.isSuccess() && result.hasException()) {
            result.throwOutException();
        }
    }

    public void setPendingNotifier(PendingNotify pendingNotify) {
        this.pendingNotifier = pendingNotify;
        for (Map.Entry<String, ScheduledExecutorService> entry : reprocessFutureMap.entrySet()) {
            ScheduledExecutorService reprocessFuture = entry.getValue();
            if (reprocessFuture != null) {
                reprocessFuture.shutdown();
            }
        }
        reprocessFutureMap.clear();
        if (destinationContext != null && destinationContext.getDestination() instanceof ClientQueue) {
            if (pendingNotifyExecutor != null) {
                pendingNotifyExecutor.shutdown();
            }
            //start pending notify schedule
            pendingNotifyExecutor = NapoliMessageUtil.startSchedule("PendingNotify Schedule[" + name + "]",
                    new PendingSchedule(connector, destinationContext, pendingNotify, pendingTimeout, this),
                    pendingInterval, pendingInterval);
        }
    }

    public void setPendingBatch(int pendingBatch) {
        this.pendingBatch = pendingBatch;
    }

    public void setPendingInterval(int pendingInterval) {
        this.pendingInterval = pendingInterval;
    }

    public void setPendingTimeout(int pendingTimeout) {
        this.pendingTimeout = pendingTimeout;
    }

    /**
     * for the mock thest
     * 
     * @param napoliSender the mock napoliSender
     */
    public void setNapoliSender(NapoliSender napoliSender) {
        this.napoliSender = napoliSender;
    }

    /*public void setOrder(boolean order) {
        this.order = order;
    }*/

    public void setProps(String key, String value) {
        if (PROP_NAME.equals(key)) {
            setName(value);
        } else if (PROP_REPROCESSINTERVAL.equals(key)) {
            setReprocessInterval(Integer.valueOf(value));
        } else if (PROP_STOREENABLE.equals(key)) {
            setStoreEnable(Boolean.valueOf(value));
        } else if (PROP_PENDINGBATCH.equals(key)) {
            setPendingBatch(Integer.valueOf(value));
        } else if (PROP_PENDINGINTERVAL.equals(key)) {
            setPendingInterval(Integer.valueOf(value));
        } else if (PROP_PENDINGTIMEOUT.equals(key)) {
            setPendingTimeout(Integer.valueOf(value));
        } else {
            if (log.isWarnEnabled()) {
                log.warn("the property " + key + " is not support! the value " + value + " is be ignored");
            }
        }
    }

    @Override
    public void beginTransaction() throws NapoliClientException {
        if (destinationContext.getDestination() instanceof ClientTopic) {
            if (metaqProducer != null) {
                try {
                    metaqProducer.beginTransaction();
                } catch (MetaClientException e) {
                    throw new NapoliClientException(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void commit() throws NapoliClientException {
        if (destinationContext.getDestination() instanceof ClientTopic) {
            if (metaqProducer != null) {
                try {
                    metaqProducer.commit();
                } catch (MetaClientException e) {
                    throw new NapoliClientException(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void rollback() throws NapoliClientException {
        if (destinationContext.getDestination() instanceof ClientTopic) {
            if (metaqProducer != null) {
                try {
                    metaqProducer.rollback();
                } catch (MetaClientException e) {
                    throw new NapoliClientException(e.getMessage(), e);
                }
            }
        }
    }
}
