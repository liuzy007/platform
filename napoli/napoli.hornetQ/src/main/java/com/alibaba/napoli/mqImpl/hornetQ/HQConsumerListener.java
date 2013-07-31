package com.alibaba.napoli.mqImpl.hornetQ;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.client.async.router.AsyncRouterWorker;
import com.alibaba.napoli.client.async.router.RouterWorker;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.NapoliException;
import com.alibaba.napoli.common.util.ExtensionLoader;
import com.alibaba.napoli.receiver.NapoliReceiver;
import com.alibaba.napoli.receiver.NapoliReceiverContext;
import com.alibaba.napoli.receiver.RedeliveryStrategy;
import com.alibaba.napoli.sender.NapoliResult;
import java.io.Serializable;
import javax.jms.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.MessageHandler;

/**
 * User: yanny.wang Date: 2012/1/16
 */
public class HQConsumerListener implements MessageHandler {

    protected static final Log logger = LogFactory.getLog(HQConsumerListener.class);

    protected HornetQConsumerContext consumerContext;

    protected NapoliWorker worker;

    protected long idle;

    public HQConsumerListener(HornetQConsumerContext consumerContext, NapoliWorker worker) {
        this.consumerContext = consumerContext;
        this.worker = worker;
        idle = System.currentTimeMillis();
    }

    public boolean isIdle() {
        return ((System.currentTimeMillis() - idle) > consumerContext.getIldePeriod()) || consumerContext.getSession().isClosed();
    }

    public void onMessage(ClientMessage message) {
        idle = System.currentTimeMillis();

        NapoliResult result;
        NapoliMessage napoliMessage = null;

        if (worker instanceof AsyncRouterWorker) {
            throw new IllegalStateException("hornetq can't support AsyncRouterWorker");
        } else if (worker instanceof RouterWorker) {
            boolean success = ((RouterWorker) worker).doWork(message);
            result = new NapoliResult(success);
        } else {
            try {
                napoliMessage = HornetQMessageUtil.fromClientMessage(message);
                //fix for NP-265
                napoliMessage.setStore2Local(consumerContext.getConnectionParam().isStoreEnable());
                NapoliReceiver napoliReceiver;
                ConnectionParam connectionParam = consumerContext.getConnectionParam();
                if (connectionParam.getFilterChain() != null && connectionParam.getFilterChain().length() > 0) {
                    napoliReceiver = ExtensionLoader.buildReceiverFilterChain(connectionParam.getFilterChain().split(","));
                }else{
                    napoliReceiver = ExtensionLoader.buildReceiverFilterChain(new String[]{"monitor", "bdbStore"});
                }
                NapoliReceiverContext context = new NapoliReceiverContext(worker, napoliMessage,
                        consumerContext.getConnectionParam());
                context.setFilterList(consumerContext.getFilterList());
                result = napoliReceiver.onMessage(context);
            } catch (NapoliException e) {
                result = new NapoliResult(e.getMessage(), e);
            }
        }
        result.printError();

        if (result.isSuccess()) {
            try {
                message.acknowledge();
            } catch (HornetQException e) {
                //会整样？消息重复消费？
                logger.error("message [" + message + "] ack error,may be duplicate message.", e);
            }
        } else {
            recover(message,napoliMessage, result.getThrowables());
        }
    }

    private void recover(ClientMessage message, NapoliMessage napoliMessage, Throwable[] ret) {
//        if (consumerContext.getAcknowledgeMode() != Session.CLIENT_ACKNOWLEDGE) {
//            // NOTE: 调用 recover 方法，来回滚这条消息
//            // 如果 抛异常，会 导致这个Session不可用！
//            if (logger.isInfoEnabled()) {
//                logger.info("the process result of message(" + message + ") is false(Queue: "
//                        + consumerContext.getQueueName() + ")");
//            }
//            doRecover(message, ret);
//        }
        if (logger.isWarnEnabled()) {
            logger.warn("the process result of message(" + napoliMessage + ") is false(Queue: "
                    + consumerContext.getQueueName() + "),deliveryCount=" + message.getDeliveryCount());
        }
        if (message.getDeliveryCount() >= 7) { //7是hornetq server端配死的.
            onRedeliveryCallback(napoliMessage, ret);
        }
        try {
            consumerContext.getSession().rollback(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        //doRecover(message, ret);
    }

    /*private void doRecover(NapoliMessage message, Throwable[] ret) {
        if (logger.isInfoEnabled()) {
            logger.info("Recover session of Queue(" + consumerContext.getQueueName() + ")");
        }
        try {
            
            onRedeliveryCallback(message, ret);
            consumerContext.getSession().rollback(true);
            // FIXME recover方法的调用，会有什么影响？
            if (consumerContext.getAcknowledgeMode() == Session.AUTO_ACKNOWLEDGE) {
                *//*
                 * if (logger.isWarnEnabled()) {
                 * logger.warn("Recover session of Queue(" +
                 * consumerContext.getQueueName() + ")"); }
                 *//*
                //TODO: right now HornetQ doesn't support transaction in Napoli.

            }
        } catch (final Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception when recover: " + e.getMessage(), e);
            }
        }
    }*/

    /*private void acknowledge(ClientMessage message) {
        try {
            message.acknowledge();
        } catch (HornetQException e) {
            //会整样？消息重复消费？
            logger.error("message [" + message + "] ack error,may be duplicate message.", e);
        }

    }*/

    public void onRedeliveryCallback(final NapoliMessage message, final Throwable[] ret) {

        /*//TODO: check how hornet support RedeliveryCallback.    

        int count = message.getDeliveryCount();

        if (logger.isDebugEnabled()) {
            logger.debug("redelevery count is " + count);
        }*/

        RedeliveryStrategy redeliveryStrategy = consumerContext.getRedeliveryStrategy();
        if (redeliveryStrategy != null) {
            //right now HornetQ hard code to config redelivery count = 7 which means message will be delivered 7 times before moved to DLQ if all consumer failed.

            if (redeliveryStrategy.getRedeliveryCallback() != null) {
                try {
                    //任何callback异常都抓住，避免影响正常消息处理流程
                    redeliveryStrategy.getRedeliveryCallback().redeliveryFailed((Serializable) message.getContent(),
                            (ret != null && ret.length > 0) ? ret[0] : null);
                    /*if (logger.isInfoEnabled()) {
                        logger.info("Redelivery callback is called at queue " + consumerContext.getQueueName()
                                + " , and message is " + message.getContent());
                    }*/
                } catch (Throwable e) {
                    logger.error("redelivery callback call failed. " + e.getMessage(), e);
                }
            }

        }
    }

    public void close() {
        consumerContext.close();
    }
}
