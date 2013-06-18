package com.alibaba.napoli.mqImpl.metaq;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.common.util.StringUtils;
import com.alibaba.napoli.metamorphosis.client.MessageSessionFactory;
import com.alibaba.napoli.metamorphosis.client.MetaClientConfig;
import com.alibaba.napoli.metamorphosis.client.MetaMessageSessionFactory;
import com.alibaba.napoli.metamorphosis.client.consumer.ConsumerConfig;
import com.alibaba.napoli.metamorphosis.client.consumer.MessageConsumer;
import com.alibaba.napoli.metamorphosis.client.extension.MetaBroadcastMessageSessionFactory;
import com.alibaba.napoli.metamorphosis.client.extension.OrderedMetaMessageSessionFactory;
import com.alibaba.napoli.metamorphosis.client.producer.MessageProducer;
import com.alibaba.napoli.metamorphosis.exception.MetaClientException;
import com.alibaba.napoli.metamorphosis.utils.ZkUtils;

/**
 * User: heyman Date: 4/9/12 Time: 4:17 下午
 */
public class MetaqFactory {
    private static final Log                          log                      = LogFactory.getLog(MetaqFactory.class);
    private static Map<String, MessageSessionFactory> messageSessionFactoryMap = new HashMap<String, MessageSessionFactory>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                for (Map.Entry<String, MessageSessionFactory> entry : messageSessionFactoryMap.entrySet()) {
                    MessageSessionFactory sessionFactory = entry.getValue();
                    try {
                        sessionFactory.shutdown();
                    } catch (MetaClientException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                System.out.println("close MetaqSessionFactory ok!");
            }
        }));
    }

    public synchronized static MessageProducer getMessageProducer(String zkAddress, String topicName,boolean order)
            throws NapoliClientException {
        // create producer
        final MessageProducer producer = getSessionFactory(zkAddress,false,order).createProducer();
        // publish topic
        producer.publish(topicName);
        return producer;
    }

    public synchronized static MessageConsumer getMessageConsumer(String zkAddress, String topicName,
                                                                  String consumerGroup, final NapoliWorker worker,
                                                                  final MetaqConsumerContext consumerContext)
            throws NapoliClientException {
        ConsumerConfig consumerConfig = new ConsumerConfig(consumerGroup);
        consumerConfig.setConsumeFromMaxOffset();
        String messageSelector = consumerContext.getConnectionParam().getMessageSelector();
        if (!StringUtils.isEmpty(messageSelector)) {
            consumerConfig.setMessageSelector(messageSelector);
        }
        final MessageConsumer consumer;
        if (consumerContext.isBoardcast()) {
            consumer = ((MetaBroadcastMessageSessionFactory) getSessionFactory(zkAddress, true,false))
                    .createBroadcastConsumer(consumerConfig);
        } else {
            consumer = getSessionFactory(zkAddress,false,false).createConsumer(consumerConfig);
        }
        MetaqListener metaqListener = new MetaqListener(worker, consumerContext);
        // subscribe topic
        try {
            consumer.subscribe(topicName, 1024 * 1024, metaqListener);
            // complete subscribe
            consumer.completeSubscribe();
        } catch (MetaClientException e) {
            log.error(e.getMessage(),e);
            throw new NapoliClientException(e.getMessage(),e);
        }
        return consumer;
    }

    private static MessageSessionFactory getSessionFactory(String zkAddress, boolean boardCast, boolean order)
            throws NapoliClientException {
        String cacheKey = zkAddress + boardCast + order;
        MessageSessionFactory sessionFactory = messageSessionFactoryMap.get(cacheKey);
        if (sessionFactory != null) {
            return sessionFactory;
        }
        MetaClientConfig config = new MetaClientConfig();
        ZkUtils.ZKConfig zkConfig = new ZkUtils.ZKConfig();
        zkConfig.zkConnect = zkAddress;
        config.setZkConfig(zkConfig);
        try {
            if (boardCast) {
                sessionFactory = new MetaBroadcastMessageSessionFactory(config);
            } else if (order) {
                sessionFactory = new OrderedMetaMessageSessionFactory(config);
            } else {
                sessionFactory = new MetaMessageSessionFactory(config);
            }
        } catch (MetaClientException e) {
            throw new NapoliClientException(e.getMessage(), e);
        }
        messageSessionFactoryMap.put(cacheKey, sessionFactory);
        return sessionFactory;
    }
}
