package com.alibaba.napoli.receiver.impl;

import com.alibaba.napoli.client.NapoliClient;
import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.client.async.RedeliveryCallback;
import com.alibaba.napoli.client.async.ext.AsyncWorkerEx;
import com.alibaba.napoli.client.async.router.AsyncRouterWorker;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.persistencestore.StoreDiscard;
import com.alibaba.napoli.common.util.NamedThreadFactory;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.domain.client.ClientTopic;
import com.alibaba.napoli.domain.client.ClientVirtualTopic;
import com.alibaba.napoli.metamorphosis.client.consumer.MessageConsumer;
import com.alibaba.napoli.metamorphosis.exception.MetaClientException;
import com.alibaba.napoli.mqImpl.TransportFactory;
import com.alibaba.napoli.mqImpl.metaq.MetaqConsumerContext;
import com.alibaba.napoli.mqImpl.metaq.MetaqFactory;
import com.alibaba.napoli.receiver.Receiver;
import com.alibaba.napoli.receiver.ReceiverException;
import com.alibaba.napoli.receiver.RedeliveryStrategy;
import com.alibaba.napoli.receiver.filter.Filter;
import com.alibaba.napoli.spi.StartupListener;
import com.alibaba.napoli.spi.TransportConsumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 12/8/11 Time: 3:25 下午
 */
public class DefaultReceiverImpl extends NapoliClient implements Receiver {
    private static final Log           logger                = LogFactory.getLog(DefaultReceiverImpl.class);

    protected int                      instances             = 5;
    private List<Filter>               filterList            = Collections.synchronizedList(new ArrayList<Filter>());

    protected volatile long            period                = 60 * 1000;
    private int                        dataBatchReadCount    = 1000;
    private TransportFactory           transportFactory      = new TransportFactory();

    protected NapoliWorker             worker;
    protected ConnectionParam          connectionParam;
    protected RedeliveryStrategy       redeliveryStrategy;

    protected ScheduledExecutorService heartbeatExecutor;
    protected Future<?>                heartbeatFuture;

    protected ScheduledExecutorService reprocessExecutor;
    protected Future<?>                reprocessFuture;

    public static final String         PROP_HEARTBEAT_PERIOD = "period";
    private String                     messageSelector       = "";

    private int                        reprocessNum          = 0;                                                    //本地存储重新执行的次数
    private StoreDiscard               storeDiscard;

    private String                     group;
    private boolean                    boardcast             = false;

    private MessageConsumer            messageConsumer;

    public DefaultReceiverImpl() {
    }

    public void init() throws NapoliClientException {
        if (destinationContext != null) {
            logger.warn("Receiver[" + name + "] has been inited!");
            return;
        }
        super.init();
        if (destinationContext.getDestination() instanceof ClientVirtualTopic) {
            throw new ReceiverException("receiver don't support virtual_topic the name=" + name + " !");
        }
        try {
            if (filterList != null) {
                for (final Filter filter : filterList) {
                    filter.init();
                }
            }
            // init connectionParam
            if (connectionParam == null) {
                connectionParam = connector.getConnectionParam();
                destinationContext.setInstances(instances);
                // 如果关闭store，那么必须启动客户端应答模式以避免消息丢失
                if (!storeEnable) {
                    // 客户端要求用redilevy模式除外
                    int acknownedgeMode = redeliveryStrategy != null ? NapoliConstant.AUTO_ACKNOWLEDGE
                            : NapoliConstant.CLIENT_ACKNOWLEDGE;
                    connectionParam.setAcknowledgeMode(acknownedgeMode);
                }
            }
            connectionParam.setName(destinationContext.getDestination().getName());
            if (destinationContext.getDestination() instanceof ClientQueue) {
                storeEnable = needPersistence();
            } else {
                storeEnable = true;
            }
            connectionParam.setStoreEnable(storeEnable);
            connectionParam.setFilterChain(filterChain);
            connectionParam.setReprocessNum(reprocessNum);
            connectionParam.setMessageSelector(messageSelector);
            destinationContext.setConnectionParam(connectionParam);

            connectionParam.setRedeliveryStrategy(getRedeliveryStrategy());
        } catch (Exception e) {
            throw new NapoliClientException("DefaultReceiver init happen error!", e);
        }
    }

    public ConnectionParam getConnectionParam() {
        return this.connectionParam;
    }

    public void start() throws NapoliClientException {
        if (destinationContext == null) {
            init(); //先初始化
        }
        if (closed) {
            throw new IllegalStateException("Receiver(" + name + ") is closed!");
        }
        if (started) {
            return;
        }

        if (worker == null) {
            return;
        }

        try {
            // start consumer, still need to call reloadConsumer to adjust real instances.           
            destinationContext.reloadConsumer();
            if (destinationContext.getDestination() instanceof ClientTopic) {
                final MetaqConsumerContext consumerContext = new MetaqConsumerContext(connectionParam, filterList);
                consumerContext.setBoardcast(boardcast);
                String zks = ((ClientTopic) destinationContext.getDestination()).getZks();
                if (zks == null) {
                    throw new NapoliClientException("zkaddrss is null");
                }
                if (StringUtils.isBlank(group)) {
                    throw new NapoliClientException("Receiver start happen error,the group name isn't be set!");
                }
                messageConsumer = MetaqFactory.getMessageConsumer(zks, destinationContext.getDestination().getName(), group, worker, consumerContext);
            } else {
                //start consumer
                for (ClientMachine machine : destinationContext.getReceiveMachineList()) {
                    if (destinationContext.getConsumerMachineMap().get(machine) == null) {
                        if (logger.isInfoEnabled()) {
                            logger.info("open consumer for new machine " + machine);
                        }
                        //fix for bug NP-261
                        try {
                            TransportConsumer transportConsumer = transportFactory.getTransportConsumer(connector,
                                    machine, destinationContext.getConnectionParam(), worker, filterList);
                            destinationContext.getConsumerMachineMap().put(machine, transportConsumer);
                            transportConsumer.startListen();
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                // start heartbeat schedule daemon false,let receiver will no exit,when start()
                if (heartbeatExecutor == null) {
                    heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("napoli-"
                            + "Receiver Heartbeat[" + name + "]", false));
                }
                heartbeatSchedule = new HeartbeatSchedule(destinationContext, connector, worker, filterList);
                heartbeatFuture = heartbeatExecutor.scheduleWithFixedDelay(heartbeatSchedule, period, period,
                        TimeUnit.MILLISECONDS);
            }
            createReprocess();
            started = true;
            //begin startupListener
            ServiceLoader<StartupListener> startupListeners = ServiceLoader.load(StartupListener.class);
            for (StartupListener startupListener : startupListeners) {
                try {
                    startupListener.startup(this);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            started = false;
            logger.error(e.getMessage(), e);
            throw new NapoliClientException("DefaultReceiver start happen error!", e);
        }
    }

    public void stop() throws NapoliClientException {
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(true);
        }
        if (reprocessFuture != null) {
            reprocessFuture.cancel(true);
        }

        if (destinationContext != null) {
            Iterator<Map.Entry<ClientMachine, TransportConsumer>> iterator = destinationContext.getConsumerMachineMap()
                    .entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ClientMachine, TransportConsumer> entry = iterator.next();
                TransportConsumer transportConsumer = entry.getValue();
                transportConsumer.stopListen();
                iterator.remove();
            }
        }

        if (messageConsumer != null) {
            try {
                messageConsumer.shutdown();
            } catch (MetaClientException e) {
                logger.error(e.getMessage(), e);
            }
        }

        started = false;
    }

    public void close() {
        try {
            stop();
        } catch (final NapoliClientException e) {
            logger.error(e.getMessage(), e);
        }
        super.close();
    }

    public void setWorker(AsyncWorker worker) {
        setWorkerPri(worker);
    }

    public void setExWorker(AsyncWorkerEx worker) {
        setWorkerPri(worker);
    }

    public void setRouterWorker(AsyncRouterWorker worker) throws NapoliClientException {
        setWorkerPri(worker);
    }

    public void setRedeliveryCallback(RedeliveryCallback redeliveryCallback) {
        if (redeliveryCallback == null) {
            throw new IllegalArgumentException("Argument redeliveryCallback can not be null.");
        }
        if (redeliveryStrategy == null) {
            redeliveryStrategy = new RedeliveryStrategy();
        }
        redeliveryStrategy.setRedeliveryCallback(redeliveryCallback);
    }

    private synchronized void createReprocess() {
        if (storeEnable) {
            /*
             * KVStore kvStore =
             * ConsoleConnector.getorCreateBdbKvStore(connector.getStorePath(),
             * name, NapoliConstant.CLIENT_TYPE_RECEIVER);
             */
            // start reconnectTimer
            ReprocessSchedule reprocessSchedule = new ReprocessSchedule(connectionParam, dataBatchReadCount, worker);
            reprocessSchedule.setStoreDiscard(storeDiscard);
            if (reprocessExecutor == null) {
                reprocessExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("napoli-"
                        + "Receiver Reprocess handle schedule[" + name + "]", true));
            }
            reprocessFuture = reprocessExecutor.scheduleWithFixedDelay(reprocessSchedule, reprocessInterval,
                    reprocessInterval, TimeUnit.MILLISECONDS);
        }
    }

    public void setInstances(int instances) {
        if (instances < 1) {
            this.instances = 1;
        } else {
            this.instances = instances;
        }
    }

    public void setPeriod(long period) {
        if (period < NapoliConstant.MIN_HEARTBEAT_PERIOD) {
            logger.warn("The heart beat period less than the min_heartbeat_period("
                    + NapoliConstant.MIN_HEARTBEAT_PERIOD + ")");
        }
        this.period = period;
    }

    public void setWorkerPri(final NapoliWorker worker) {
        if (worker == null) {
            throw new IllegalArgumentException("Argument worker can not be null.");
        }
        if (this.worker != null) {
            throw new IllegalArgumentException("setWorker method only support call once.");
        }
        this.worker = worker;
    }

    public void setDataBatchReadCount(int dataBatchReadCount) {
        this.dataBatchReadCount = dataBatchReadCount;
    }

    public void setFilterList(List<Filter> filterList) {
        this.filterList = filterList;
    }

    private boolean needPersistence() {
        final ClientQueue queueEntity = (ClientQueue) destinationContext.getDestination();
        // zgl:增加一个条件，虽然不是HA，但是如果client设定redelivery次数并且设定回掉接口，也不需要客户端存储。
        // zgl:再增加一个条件，允许用户disable store
        return storeEnable && !(queueEntity.getMaxRedeliveries() > 0 && redeliveryStrategy != null)
                && !(worker instanceof AsyncRouterWorker || worker instanceof AsyncWorkerEx);
    }

    /**
     * 对于Receiver，表示NapoliSession的个数。 实际的Session的个数 不一定会是这个数值。
     * 
     * @return 表示NapoliSession的个数
     */
    public int getInstances() {
        return this.destinationContext.getInstances();
    }

    public int getExpectedInstances() {
        return this.destinationContext.getExpectedInstances();
    }

    public RedeliveryStrategy getRedeliveryStrategy() {
        if (redeliveryStrategy != null && redeliveryStrategy.getMaxRedeliveries() < 0 && destinationContext != null) {
            ClientQueue queueEntity = (ClientQueue) destinationContext.getDestination();
            redeliveryStrategy.setMaxRedeliveries(queueEntity.getMaxRedeliveries());
        }
        return redeliveryStrategy;
    }

    public void setProps(String key, String value) {
        if (PROP_NAME.equals(key)) {
            setName(value);
        } else if (PROP_REPROCESSINTERVAL.equals(key)) {
            setReprocessInterval(Integer.valueOf(value));
        } else if (PROP_STOREENABLE.equals(key)) {
            setStoreEnable(Boolean.valueOf(value));
        } else if (PROP_HEARTBEAT_PERIOD.equals(key)) {
            setPeriod(Integer.valueOf(value));
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("the property " + key + " is not support! the value " + value + " is be ignored");
            }
        }
    }

    public void setReprocessNum(int reprocessNum) {
        this.reprocessNum = reprocessNum;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    /*
     * public void setBoardcast(boolean boardcast) { this.boardcast = boardcast;
     * }
     */

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    public List<Filter> getFilterList() {
        return filterList;
    }

    /*public NapoliWorker getWorker() {
        return worker;
    }*/

    public void setStoreDiscard(StoreDiscard storeDiscard) {
        this.storeDiscard = storeDiscard;
    }
}
