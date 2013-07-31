package com.alibaba.napoli.common.util;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.common.NapoliException;
import com.alibaba.napoli.domain.client.ClientDestination;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.domain.client.ClientVirtualTopic;
import com.alibaba.napoli.receiver.filter.Filter;
import com.alibaba.napoli.receiver.filter.FilterFinder;
import com.alibaba.napoli.receiver.filter.SimpleContext;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NapoliMessageUtil {
    private static final Pattern IP_PATTERN = Pattern
                                                    .compile("^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3,3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?):"
                                                            + "([1-9]\\d{0,3}|[1-5]\\d{4,4}|6[0-4]\\d{3,3}|65[0-4]\\d{2,2}|655[0-2]\\d|6553[0-5])$");

    private static final Log     logger     = LogFactory.getLog(NapoliMessageUtil.class);

    /**
     * 校验ip地址
     * 
     * @param machine ip地址
     * @return 是否规范
     */
    public static boolean verifyUrl(String machine) {
        Matcher m = IP_PATTERN.matcher(machine);
        return m.matches();
    }

    public static Serializable getContent(final Message message) throws JMSException {
        Serializable content;
        if (message instanceof TextMessage) {
            content = ((TextMessage) message).getText();
        } else if (message instanceof ObjectMessage) {
            content = ((ObjectMessage) message).getObject();
        } else {
            // NOTE: 收到其它类型的消息。这种消息，不能被消费，最后会被放在Dead Letter Queue中。
            content = null;
        }
        return content;
    }

    public static void makeDirs(final String storePath) {
        final File storeFile = new File(storePath);
        if (storeFile.isFile()) {
            throw new IllegalArgumentException("Store path " + storePath + " must be a dirrectory.");
        }

        if (!storeFile.exists()) {
            final boolean success = storeFile.mkdirs();
            if (!success) {
                throw new IllegalStateException("Fail to create directory(" + storeFile.getAbsolutePath() + ")");
            }
        }
    }

    public static ConfigServiceHttpImpl getConfigService(String address) {
        /*
         * String url = NapoliMessageUtil.createNapoliUrl(address); final
         * HttpInvokerProxyFactoryBean factoryBean = new
         * HttpInvokerProxyFactoryBean(); factoryBean.setServiceUrl(url);
         * factoryBean.setServiceInterface(ConfigService.class);
         * factoryBean.afterPropertiesSet(); return (ConfigService)
         * factoryBean.getObject();
         */
        return new ConfigServiceHttpImpl(address);
    }
    
    /*public static Set<ClientMachine> getClientMachines(ConfigServiceHttpImpl config, String destinationName){
        final Set<ClientMachine> machines = new HashSet<ClientMachine>();

        ClientDestination dest = config.fetchDestination(destinationName);

        if (dest instanceof ClientQueue) {
            return ((ClientQueue)dest).getMachineSet();
        } else if (dest instanceof ClientVirtualTopic) {
            ClientVirtualTopic vtopic = (ClientVirtualTopic)dest;
            List<ClientQueue> queueList = vtopic.getClientQueueList();
            for (ClientQueue clientQueue : queueList){
                machines.addAll(clientQueue.getMachineSet());
            }
            return machines;
        }
        return machines;
    }*/

    /*public static ArrayList<ClientQueue> getPhysicalQueues(String address, String destinationName) {
        ConfigServiceHttpImpl config = getConfigService(address);
        ClientDestination dest = config.fetchDestination(destinationName);
        return getPhysicalQueues(config, destinationName);

    }*/

    /*public static Set<ClientMachine> getPhysicalQueues(ConfigServiceHttpImpl config, String destinationName) {
        final Set<ClientMachine> queues = new HashSet<ClientMachine>();

        ClientDestination dest = config.fetchDestination(destinationName);

        if (dest instanceof ClientQueue) {
            ClientQueue entity = (ClientQueue) dest;
            Set<ClientMachine> machineSet = entity.getMachineSet();
            for (ClientMachine machine: machineSet){
                queues.add(machine);
            }
        } else if (dest instanceof VirtualTopic) {
            return getPhysicalQueuesFromTopic((VirtualTopic) dest);
        }

        return queues;
    }

    public static ArrayList<String> getQueueNamess(String address, String destinationName) {

        ConfigService config = getConfigService(address);

        return getQueueNamess(config, destinationName);
    }

    public static ArrayList<String> getQueueNamess(ConfigService config, String destinationName) {
        final ArrayList<String> queues = new ArrayList<String>();

        Destination dest = config.fetchDestination(destinationName);

        if (dest instanceof QueueEntity) {

            QueueEntity entity = (QueueEntity) dest;
            int count = entity.getPhysicalQueueMap().size();
            for (int i = 0; i < count; i++) {
                queues.add(destinationName);
            }
        } else if (dest instanceof VirtualTopic) {
            VirtualTopic topic = (VirtualTopic) dest;

            for (Map.Entry<Integer, QueueEntity> queue : topic.getQueueMap().entrySet()) {
                int count = queue.getValue().getPhysicalQueueMap().size();
                for (int i = 0; i < count; i++) {
                    queues.add(queue.getValue().getName());
                }
            }
        }

        return queues;
    }*/

    /*public static ArrayList<ClientQueue> getPhysicalQueuesFromTopic(ClientVirtualTopic topic) {
        topic.get
        final ArrayList<PhysicalQueue> queues = new ArrayList<PhysicalQueue>();

        for (Map.Entry<Integer, QueueEntity> queue : topic.getQueueMap().entrySet()) {

            queues.addAll(queue.getValue().getPhysicalQueueMap().values());
        }

        return queues;
    }*/

    public static ScheduledExecutorService startSchedule(String scheduleName, Runnable scheduleTask, long init,
                                                         long interval) {
        ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(
                "napoli-" + scheduleName, true));
        scheduleExecutor.scheduleWithFixedDelay(scheduleTask, init, interval, TimeUnit.MILLISECONDS);
        return scheduleExecutor;
    }

    public static Serializable getMessageBody(Message message) throws NapoliException {
        try {
            if (message instanceof TextMessage) {
                return ((TextMessage) message).getText();
            } else if (message instanceof ObjectMessage) {
                return ((ObjectMessage) message).getObject();
            } else if (message instanceof BytesMessage) {
                BytesMessage theMessage = (BytesMessage) message;
                byte[] content = new byte[(int) theMessage.getBodyLength()];
                theMessage.readBytes(content);
                return content;
            } else {
                throw new NapoliException("not support message type " + message.getJMSType());
            }
        } catch (JMSException e) {
            throw new NapoliException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static NapoliMessage fromJmsMessage(Message jmsMessage) throws NapoliException {
        try {
            Serializable content;
            // 获取消息内容
            if (jmsMessage instanceof TextMessage) {
                content = ((TextMessage) jmsMessage).getText();
            } else if (jmsMessage instanceof ObjectMessage) {
                content = ((ObjectMessage) jmsMessage).getObject();
            } else if (jmsMessage instanceof BytesMessage){
                BytesMessage bytesMessage = (BytesMessage)jmsMessage;
                byte[] data = new byte[(int)bytesMessage.getBodyLength()];
                bytesMessage.readBytes(data);
                content = HessianUtil.deserialize(data);
            } else {
                throw new JMSException("not support message type " + jmsMessage.getJMSType());
            }
            NapoliMessage napoliMessage = new NapoliMessage(content);
            // 优先级和超时属性
            napoliMessage.setExpiration(jmsMessage.getJMSExpiration());
            napoliMessage.setPriority(jmsMessage.getJMSPriority());
            // 其它属性，包括用户自定义属性
            Enumeration e = jmsMessage.getPropertyNames();
            for (; e != null && e.hasMoreElements();) {
                String key = (String) e.nextElement();
                //inner property don't copy
                if (isInnerProperties(key)) {
                    continue;
                }
                napoliMessage.setProperty(key, jmsMessage.getObjectProperty(key));
            }
            return napoliMessage;
        } catch (JMSException e1) {
            throw new NapoliException("fromJmsMessage jmsExceptoin happened!", e1);
        } catch (Throwable e) {
            throw new NapoliException("deSerialize object error", e);
        }
    }

    public static boolean isInnerProperties(String key) {
        return TransactionConstants.NAPOLI_MSG_PRO_KEY_TX_ID.equals(key)
                || TransactionConstants.NAPOLI_MSG_PRO_KEY_TX_STATE.equals(key)
                || NapoliMessage.MSG_PROP_KEY_STORE_ENABLE.equals(key)
                || NapoliMessage.MSG_PROP_KEY_EXPIRATION.equals(key) || NapoliMessage.MSG_PROP_KEY_PRIORITY.equals(key);
    }

    public static Serializable filterMsg(final List<Filter> filterList, final Serializable msg) {
        if (filterList == null || filterList.isEmpty()) {
            return msg;
        }
        try {
            final SimpleContext context = new SimpleContext(msg);
            final Filter cur = filterList.get(0);
            cur.filter(context, new NapoliFilterFind(filterList));
            return context.getOutputObject();
        } catch (final Throwable e) {
            if (logger.isErrorEnabled()) {
                logger.error("SenderFilter msg is failed:" + e.getMessage(), e);
            }
            // TODO 这个异常会传播回应用调用者，这样做是否合适？
            throw new RuntimeException("Illegal filter,pls check it.", e);
        }
    }

    private static class NapoliFilterFind implements FilterFinder {
        private List<Filter> filterList;

        private NapoliFilterFind(List<Filter> filterList) {
            this.filterList = filterList;
        }

        public Filter nextFilter(Filter filter) {
            final int index = filterList.indexOf(filter);
            //fix bug,应该index>=0--zgl
            if (index >= 0 && index < filterList.size() - 1) {
                return filterList.get(index + 1);
            }
            return null;
        }
    }
}
