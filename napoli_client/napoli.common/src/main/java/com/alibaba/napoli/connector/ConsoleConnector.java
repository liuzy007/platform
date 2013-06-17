package com.alibaba.napoli.connector;

import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.common.persistencestore.impl.BdbKVStore;
import com.alibaba.napoli.common.util.ConfigServiceHttpImpl;
import com.alibaba.napoli.common.util.NapoliMessageUtil;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.mqImpl.NapoliConnection;
import com.alibaba.napoli.spi.TransportSender;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 11/30/11 Time: 2:33 下午
 */
public class ConsoleConnector extends ConsoleConnectorBase {
    private static final Log                                      log                   = LogFactory
                                                                                                .getLog(ConsoleConnector.class);
    private static ConcurrentHashMap<String, KVStore>             kvStoreMap            = new ConcurrentHashMap<String, KVStore>();
    private static List<ConsoleConnector>                         connectorList         = new ArrayList<ConsoleConnector>();
    //private static ConcurrentHashMap<String, ConsoleConnector>    connectorMap          = new ConcurrentHashMap<String, ConsoleConnector>();
    private boolean                                               closed = true;

    protected ConfigServiceHttpImpl                                       configService;

    private ScheduledExecutorService                              configLoaderFuture;
    private ConfigLoaderSchedule                                  configLoaderSchedule;
    private ScheduledExecutorService                              connectionCheckFuture;

    private static final Object                                   lock                  = new Object();
    private final ConcurrentMap<String, List<DestinationContext>> destinationMap        = new ConcurrentHashMap<String, List<DestinationContext>>();
    private final ConcurrentMap<ClientMachine, NapoliConnection>        consumerConnectionMap = new ConcurrentHashMap<ClientMachine, NapoliConnection>();
    private final ConcurrentMap<ClientMachine, TransportSender>         senderTransportMap    = new ConcurrentHashMap<ClientMachine, TransportSender>();
    
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                closeAll();
                System.out.println("close ConsoleConnector ok!");
            }
        }));
    }

    public ConsoleConnector(String address, int sendPoolSize, int prefetch) {
        this.address = address;
        this.poolSize = sendPoolSize;
        this.prefetch = prefetch;
    }

    public ConsoleConnector(String address) {
        this.address = address;
    }
    
    public ConsoleConnector() {
    }

    public int getSenderConnectionSize() {
        return this.senderTransportMap.size();
    }

    /**
     * 创建一个Napoli connector
     */
    public synchronized void init() {
        if (configLoaderFuture != null){
            log.warn("NapoliConnector has been inited!");
            return;
        }
        if (!closed){
            return;
        }
        //begin configLoader schedule
        if (address == null) {
            throw new IllegalArgumentException("address must be set.");
        }
        if (configService == null) {
            this.configService = new ConfigServiceHttpImpl(address);
        }
        //begin configload schedule
        configLoaderSchedule = new ConfigLoaderSchedule(configService, this);
        configLoaderFuture = NapoliMessageUtil.startSchedule("ConfigLoader Schedule[" + address + "]",
                configLoaderSchedule, interval, interval);
        //begin connection check schedule,don't need check connection,receive have the heartbeat,send check session

        connectionCheckFuture = NapoliMessageUtil.startSchedule("Connection Check Schedule on [" + address
                + "] connector", new ConnectionCheckSchedule(this), connectionCheckPeriod, connectionCheckPeriod);

        //connectorMap.put(address, this);
        connectorList.add(this);
        closed = false;
    }

    public void close() {
        if (configLoaderFuture != null) {
            configLoaderFuture.shutdown();
        }

        if (connectionCheckFuture != null) {
            connectionCheckFuture.shutdown();
        }

        for (Map.Entry<ClientMachine, TransportSender> entry : senderTransportMap.entrySet()) {
            TransportSender senderTransport = entry.getValue();
            if (senderTransport != null) {
                senderTransport.close();
            }
        }
        for (Map.Entry<ClientMachine, NapoliConnection> entry : consumerConnectionMap.entrySet()) {
            NapoliConnection receiverConnection = entry.getValue();
            if (receiverConnection != null) {
                receiverConnection.close();
            }
        }
        closed = true;
    }

    public static void closeAll() {
        for (ConsoleConnector connector : connectorList) {
            connector.close();
        }
        for (Map.Entry<String, KVStore> entry : kvStoreMap.entrySet()) {
            KVStore kvStore = entry.getValue();
            kvStore.close();
        }
        kvStoreMap.clear();
        connectorList.clear();
    }

    public synchronized void addDestination(String name, DestinationContext destination) {
        if (destinationMap.get(name) == null) {
            List<DestinationContext> destinationContextList = new ArrayList<DestinationContext>();
            destinationContextList.add(destination);
            destinationMap.put(name, destinationContextList);
        } else {
            List<DestinationContext> destinationContextList = destinationMap.get(name);
            destinationContextList.add(destination);
        }
    }

    public int getDestinationSize() {
        return destinationMap.size();
    }

    public List<String> getDestinationNames() {
        List<String> result = new ArrayList<String>();
        for (String name : destinationMap.keySet()) {
            result.add(name);
        }
        return result;
    }

    public List<DestinationContext> getDestinationList(String name) {
        return destinationMap.get(name);
    }

    public TransportSender getTransportSender(ClientMachine machine) {
        return senderTransportMap.get(machine);
    }

    public void addTransportSender(ClientMachine machine, TransportSender transportSender) {
        senderTransportMap.putIfAbsent(machine, transportSender);
    }

    public synchronized void removeSenderTransport(ClientMachine machine) {
        senderTransportMap.remove(machine);
        //TransportSender transportSender = senderTransportMap.remove(machine);
        /*
         * if (transportSender != null) { transportSender.close(); }
         */
    }

    public NapoliConnection getConsumerConnection(ClientMachine machine) {
        return consumerConnectionMap.get(machine);
    }

    public void addConsumerConnection(ClientMachine machine, NapoliConnection connection) {
        consumerConnectionMap.put(machine, connection);
    }
    
    public int getConsumerConnectionSize(){
        return consumerConnectionMap.size();
    }

    /*public void removeConsumerConnection(Machine machine) {
        consumerConnectionMap.remove(machine);
    }*/

    public ConfigServiceHttpImpl getConfigService() {
        return configService;
    }

    public void setConfigService(ConfigServiceHttpImpl configService) {
        this.configService = configService;
    }

    /**
     * 把空闲很久的发送端连接关闭
     */
    public void removeIdleSenderConnection() {
        //取快照
        Iterator<Map.Entry<ClientMachine, TransportSender>> senderIterator = senderTransportMap.entrySet().iterator();
        while (senderIterator.hasNext()) {
            long current = System.currentTimeMillis();
            Map.Entry<ClientMachine, TransportSender> next = senderIterator.next();
            //Machine machine = next.getKey();
            TransportSender transportSender = next.getValue();
            NapoliConnection napoliConnection = transportSender.getNapoliConnection();
            if ((current - napoliConnection.getLastUsedTime()) > NapoliConstant.MAX_IDLE_TIME
                    /*&& napoliConnection.isIdle()*/) {
                senderIterator.remove();
                //连接关闭时，如果有消息发送，会因为connection.isClosed or closing，
                //而无法创建session.但机率很小，只在正好close时，有发送进来。但后面的请求会重建连接
                if (log.isInfoEnabled()){
                    log.info("the connection["+next.getKey()+"] is idle "+NapoliConstant.MAX_IDLE_TIME+"ms ,it will be closed!");
                }
                transportSender.close();
            }
        }
    }

    /**
     * 把空闲很久的接收端连接关闭
     */
    public void removeIdleReceiverConnection() {
        Iterator<Map.Entry<ClientMachine, NapoliConnection>> receiverIterator = consumerConnectionMap.entrySet().iterator();
        while (receiverIterator.hasNext()) {
            //long current = System.currentTimeMillis();
            Map.Entry<ClientMachine, NapoliConnection> next = receiverIterator.next();
            ClientMachine machine = next.getKey();
            NapoliConnection napoliConnection = next.getValue();
            if (napoliConnection.isIdle()) {
                receiverIterator.remove();
                //连接关闭时，如果有消息发送，会因为connection.isClosed or closing，
                //而无法创建session.但机率很小，只在正好close时，有发送进来。但后面的请求会重建连接
                napoliConnection.close();
                if (log.isInfoEnabled()) {
                    log.info("remove and close one idle receiver connection:" + machine);
                }
            }
        }
    }

    public static KVStore getorCreateBdbKvStore(String storePath, String name, String clientType) {

        String pathKey = storePath + "/" + NapoliConstant.CLIENT_DOMAIN_ASYNC + "-" + clientType + "-" + name;

        KVStore kvStore = ConsoleConnector.kvStoreMap.get(pathKey);
        if (kvStore == null) {

            synchronized (lock) {
                //get kvStore again in case two thread enter the kvStore==null block.
                kvStore = ConsoleConnector.kvStoreMap.get(pathKey);
                if (kvStore == null) {

                    kvStore = new BdbKVStore(storePath, name, clientType);
                    ConsoleConnector.kvStoreMap.putIfAbsent(pathKey, kvStore);
                }
            }
        }

        return kvStore;
    }

    public KVStore getSenderKVStore(String queueName) {
        return ConsoleConnector.getorCreateBdbKvStore(getStorePath(), queueName, NapoliConstant.CLIENT_TYPE_SENDER);
    }

    public boolean isClosed() {
        return closed;
    }

    public ConfigLoaderSchedule getConfigLoaderSchedule() {
        return configLoaderSchedule;
    }
}
