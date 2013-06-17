package com.alibaba.napoli.mqImpl.activemq;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.client.async.router.AsyncRouterWorker;
import com.alibaba.napoli.client.async.router.RouterWorker;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.NapoliException;
import com.alibaba.napoli.common.util.ExtensionLoader;
import com.alibaba.napoli.common.util.NapoliMessageUtil;
import com.alibaba.napoli.mqImpl.ConsumerContext;
import com.alibaba.napoli.receiver.NapoliReceiver;
import com.alibaba.napoli.receiver.NapoliReceiverContext;
import com.alibaba.napoli.receiver.RedeliveryStrategy;
import com.alibaba.napoli.sender.NapoliResult;
import com.alibaba.napoli.spi.TransportConsumer;

/**
 * @author sait.xuc
 */
public class AMQConsumerListener implements MessageListener {

    protected static final Log  logger = LogFactory.getLog(AMQConsumerListener.class);

    protected ConsumerContext   consumerContext;

    protected TransportConsumer transportConsumer;

    protected NapoliWorker      worker;

    protected long              idle;
    private NapoliReceiver      napoliReceiver;

    public AMQConsumerListener(TransportConsumer transportConsumer, ConsumerContext consumerContext, NapoliWorker worker) {
        this.transportConsumer = transportConsumer;
        this.consumerContext = consumerContext;
        this.worker = worker;
        idle = System.currentTimeMillis();
        ConnectionParam connectionParam = consumerContext.getConnectionParam();
        if (connectionParam.getFilterChain() != null && connectionParam.getFilterChain().length() > 0) {
            napoliReceiver = ExtensionLoader.buildReceiverFilterChain(connectionParam.getFilterChain().split(","));
        } else {
            napoliReceiver = ExtensionLoader.buildReceiverFilterChain(new String[] { "monitor", "bdbStore" });
        }
    }

    public boolean isIdle() {
        return (System.currentTimeMillis() - idle) > consumerContext.getIldePeriod();
    }

    public void onMessage(Message message) {
        idle = System.currentTimeMillis();
        NapoliResult result;

        if (worker instanceof AsyncRouterWorker) {
            boolean success = ((AsyncRouterWorker) worker).doWork(message);
            result = new NapoliResult(success);
        } else if (worker instanceof RouterWorker) {
            boolean success = ((RouterWorker) worker).doWork(message);
            result = new NapoliResult(success);
        } else {
            try {
                NapoliMessage napoliMessage = NapoliMessageUtil.fromJmsMessage(message);
                napoliMessage.setStore2Local(consumerContext.getConnectionParam().isStoreEnable());
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
            acknowledge(message);
        } else {
            recover(message, result.getThrowables());
        }
    }

    private void recover(Message message, Throwable[] ret) {
        /*if (consumerContext.getAcknowledgeMode() != Session.CLIENT_ACKNOWLEDGE) {
            // NOTE: 调用 recover 方法，来回滚这条消息
            // 如果 抛异常，会 导致这个Session不可用！
            if (logger.isInfoEnabled()) {
                logger.info("the process result of message(" + message + ") is false(Queue: "
                        + consumerContext.getQueueName() + ")");
            }
            doRecover(message, ret);
        }*/
        doRecover(message, ret);
    }

    private void doRecover(Message message, Throwable[] ret) {
        if (logger.isInfoEnabled()) {
            logger.info("Recover session of Queue(" + consumerContext.getQueueName() + ")");
        }
        try {
            onRedeliveryCallback(message, ret);
            consumerContext.getSession().recover();
            // FIXME recover方法的调用，会有什么影响？
            /*if (consumerContext.getAcknowledgeMode() == Session.AUTO_ACKNOWLEDGE) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Recover session of Queue(" + consumerContext.getQueueName() + ")");
                }
                consumerContext.getSession().recover();
            }*/
        } catch (final JMSException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception when recover: " + e.getMessage(), e);
            }
            onException(e);
        }
    }

    private void acknowledge(Message message) {
        if (consumerContext.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE) {
            try {
                message.acknowledge();
            } catch (JMSException e) {
                //会整样？消息重复消费？
                logger.error("message [" + message + "] ack error,may be duplicate message.", e);
            }
        }
    }

    private void onException(JMSException e) {
        transportConsumer.onSessionException(e, consumerContext.getSession(), consumerContext.getMessageConsumer());
    }

    public void onRedeliveryCallback(final Message message, final Throwable[] ret) {
        if (!(message instanceof ActiveMQMessage)) {
            logger.error("not amq message,redelivery callback not support.");
            return;
        }
        ActiveMQMessage am = (ActiveMQMessage) message;
        if (am.isRedelivered()) {
            int count = am.getRedeliveryCounter();
            if (logger.isDebugEnabled()) {
                logger.debug("redelevery count is " + count);
            }
            RedeliveryStrategy redeliveryStrategy = consumerContext.getRedeliveryStrategy();
            if (redeliveryStrategy != null) {
                if (count >= redeliveryStrategy.getMaxRedeliveries()) {
                    if (redeliveryStrategy.getRedeliveryCallback() != null) {
                        try {
                            //任何callback异常都抓住，避免影响正常消息处理流程
                            Serializable content = NapoliMessageUtil.getContent(message);
                            redeliveryStrategy.getRedeliveryCallback().redeliveryFailed(content,
                                    (ret != null && ret.length > 0) ? ret[0] : null);
                            if (logger.isInfoEnabled()) {
                                logger.info("Redelivery callback is called at queue " + consumerContext.getQueueName()
                                        + " , and message is " + content);
                            }
                        } catch (Throwable e) {
                            logger.error("redelivery callback call failed. ", e);
                        }
                    }
                }
            }
        }
    }

}
