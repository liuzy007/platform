package com.alibaba.napoli.normandy;

import com.alibaba.napoli.client.NapoliClient;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.util.JsonUtil;
import com.alibaba.napoli.connector.ConfigLoaderSchedule;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.domain.client.ClientDestination;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.domain.client.ClientVirtualTopic;
import com.alibaba.napoli.receiver.Receiver;
import com.alibaba.napoli.spi.StartupListener;
import com.alibaba.normandy.client.AbstractSubscriberListener;
import com.alibaba.normandy.client.Configuration;
import com.alibaba.normandy.client.SimpleNormandyClient;
import com.alibaba.normandy.client.SubscriberListener;
import com.alibaba.normandy.common.domain.ConfigValue;
import com.alibaba.normandy.common.exception.NormandyException;
import com.alibaba.normandy.common.rules.ConfigRule;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 5/22/12 Time: 11:44 上午
 */
public class NormandyClientStartup implements StartupListener {
    private static final Log log = LogFactory.getLog(NormandyClientStartup.class);
    private static final String ip;
    private static final Map<String, SimpleNormandyClient> normandyClientMap = new HashMap<String, SimpleNormandyClient>();

    static {
        ip = getIP();
    }

    @Override
    public void startup(final NapoliClient napoliClient) {
        final SimpleNormandyClient normandyClient;
        String normandyAddress = napoliClient.getConnector().getConfigService().getNormandyAddress();
        if (normandyAddress == null) {
            return;
        }
        String[] consoleAddress = napoliClient.getConnector().getAddress().split("/");
        String[] addressArray = normandyAddress.split("/");
        if (consoleAddress.length != 2) {
            log.error("connector address[" + napoliClient.getConnector().getAddress() + "] has no domain declare!");
            return;
        }
        if (addressArray.length != 4) {
            log.error("normandyAddress is error![" + normandyAddress + "]");
            return;
        }
        StringBuilder normandyClientKey = new StringBuilder();
        for (String addressKey : addressArray) {
            normandyClientKey.append(addressKey);
        }

        if (normandyClientMap.get(normandyClientKey.toString()) == null) {
            synchronized (normandyClientMap) {
                if (normandyClientMap.get(normandyClientKey.toString()) == null) {
                    try {
                        Configuration configuration = new Configuration();
                        configuration.setHost(addressArray[0]);
                        configuration.setPort(Integer.valueOf(addressArray[1]));
                        configuration.setAutoReconnecForInit(true);
                        normandyClient = new SimpleNormandyClient();
                        normandyClient.setUsername(addressArray[2]);
                        normandyClient.setPassword(addressArray[3]);
                        normandyClient.setConfiguration(configuration);
                        normandyClient.setDefaultApplication("napoli");
                        normandyClient.setDefaultDomain(consoleAddress[1]);
                        normandyClient.setLazyInit(false);
                        normandyClient.setSessions(100);
                        normandyClient.init();
                        normandyClientMap.put(normandyClientKey.toString(), normandyClient);
                    } catch (NumberFormatException e) {
                        log.error("normandyAddress is error![" + normandyAddress + "]");
                        return;
                    } catch (NormandyException e) {
                        log.error("normandy init error![" + normandyAddress + "]", e);
                        return;
                    }
                } else {
                    normandyClient = normandyClientMap.get(normandyClientKey.toString());
                }
            }
        } else {
            normandyClient = normandyClientMap.get(normandyClientKey.toString());
        }

        SubscriberListener subscribeListener = new AbstractSubscriberListener() {
            private List<String> nowQueueListOfTopic;
            private ClientDestination nowDestination;

            @Override
            public void onConfigInitialized(final Map<String, ConfigValue> initConfigs) throws NormandyException {
                if (initConfigs == null || initConfigs.size() != 1) {
                    log.warn("destination=" + napoliClient.getName()
                            + " can't get config from normandy,or there more than one config");
                }
                if (napoliClient instanceof Receiver) {
                    normandyClient.publishAppend("/clients/" + napoliClient.getName() + "/receiver", ip + "-"
                            + NapoliConstant.version, new ConfigRule[]{ConfigRule.LifeCycleAware});
                } else if (napoliClient.getDestinationContext().getDestination() instanceof ClientVirtualTopic) {
                    ClientVirtualTopic vtopic = (ClientVirtualTopic) napoliClient.getDestinationContext()
                            .getDestination();
                    for (ClientQueue queue : vtopic.getClientQueueList()) {
                        normandyClient.publishAppend("/clients/" + queue.getName() + "/sender", ip + "-"
                                + NapoliConstant.version, new ConfigRule[]{ConfigRule.LifeCycleAware});
                    }
                } else {
                    normandyClient.publishAppend("/clients/" + napoliClient.getName() + "/sender", ip + "-"
                            + NapoliConstant.version, new ConfigRule[]{ConfigRule.LifeCycleAware});
                }

                String result = null;
                for (Map.Entry<String, ConfigValue> entry : initConfigs.entrySet()) {
                    ConfigValue configValue = entry.getValue();
                    result = configValue.getStringValue();
                }
                if (result == null) {
                    return;
                }
                try {
                    nowDestination = JsonUtil.fromJson(result);
                } catch (Exception e) {
                    nowQueueListOfTopic = JsonUtil.getVtopicQueues(result);
                    int nowQueueListOfTopicSize = nowQueueListOfTopic.size();
                    if (nowQueueListOfTopic != null && nowQueueListOfTopicSize >= 1) {
                        nowQueueListOfTopic.remove(nowQueueListOfTopicSize - 1);
                    }
                    try {
                        nowDestination = getVirtualTopic(result);
                    } catch (Exception e1) {
                        log.error(e1.getMessage(), e1);
                    }
                }
            }

            @Override
            public void onConfigUpdated(Map<String, ConfigValue> configs) throws NormandyException {
                if (configs == null || configs.size() != 1) {
                    log.warn("destination=" + napoliClient.getName()
                            + " can't get config from normandy,or there more than one config");
                    return;
                }
                ClientDestination destination;
                String result = null;
                for (Map.Entry<String, ConfigValue> entry : configs.entrySet()) {
                    ConfigValue configValue = entry.getValue();
                    result = configValue.getStringValue();
                }
                if (result == null) {
                    return;
                }

                try {
                    destination = JsonUtil.fromJson(result);
                } catch (Exception e) {
                    ClientVirtualTopic clientVirtualTopic = getVirtualTopic(result);
                    if (clientVirtualTopic == null) {
                        return;
                    }
                    List<String> newQueueListOfTopic = new ArrayList<String>();

                    for (ClientQueue clientQueue : clientVirtualTopic.getClientQueueList()) {
                        if (!nowQueueListOfTopic.contains(clientQueue.getName())) {
                            normandyClient.publishAppend("/clients/" + clientQueue.getName() + "/sender", ip + "-"
                                    + NapoliConstant.version, new ConfigRule[]{ConfigRule.LifeCycleAware});
                        }
                        newQueueListOfTopic.add(clientQueue.getName());
                    }

                    for (String queueName : nowQueueListOfTopic) {
                        if (!newQueueListOfTopic.contains(queueName)) {
                            normandyClient.publishRemove("/clients/" + queueName + "/sender", ip + "-"
                                    + NapoliConstant.version);
                        }
                    }
                    destination = clientVirtualTopic;
                    nowQueueListOfTopic = newQueueListOfTopic;
                }

                try {
                    //refresh destination
                    ConsoleConnector connector = napoliClient.getConnector();
                    ConfigLoaderSchedule configLoaderSchedule = connector.getConfigLoaderSchedule();
                    configLoaderSchedule.updateDestination(destination);

                    //heartbeat add consumers
                    if (napoliClient instanceof Receiver && napoliClient.getHeartbeatSchedule() != null) {
                        napoliClient.getHeartbeatSchedule().run();
                    }

                    nowDestination = destination;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                if (log.isInfoEnabled()) {
                    log.info("update destination[" + napoliClient.getName() + "] by normandy notify!");
                }
            }

            private ClientVirtualTopic getVirtualTopic(String vtopicString) {
                List<String> queuesOfVtopic = JsonUtil.getVtopicQueues(vtopicString);
                int queueSize = queuesOfVtopic.size();
                if (queueSize < 1) {
                    return null;
                }
                ClientVirtualTopic clientVirtualTopic = new ClientVirtualTopic();
                long modified = Long.valueOf(queuesOfVtopic.get(queueSize - 1));
                clientVirtualTopic.setModified(modified);
                clientVirtualTopic.setName(napoliClient.getName());
                clientVirtualTopic.setType(ClientDestination.TYPE_VTOPIC);
                queuesOfVtopic.remove(queueSize - 1);

                Set<String> queues = new HashSet<String>();
                for (String queueName : queuesOfVtopic) {
                    queues.add("/" + queueName);
                }
                if (queues.size() == 0) {
                    return clientVirtualTopic;
                }
                Map<String, ConfigValue> vtopicConfig = normandyClient.getConfigMulti(queues);

                List<ClientQueue> queueList = new ArrayList<ClientQueue>();
                for (Map.Entry<String, ConfigValue> entry : vtopicConfig.entrySet()) {
                    ClientQueue clientQueue = (ClientQueue) JsonUtil.fromJson(entry.getValue().getStringValue());
                    queueList.add(clientQueue);
                    normandyClient.publishAppend("/clients/" + clientQueue.getName() + "/sender", ip + "-"
                            + NapoliConstant.version, new ConfigRule[]{ConfigRule.LifeCycleAware});
                }
                clientVirtualTopic.setClientQueueList(queueList);
                return clientVirtualTopic;
            }
        };

        normandyClient.subscribe("/" + napoliClient.getName(), subscribeListener);

        if (log.isInfoEnabled()) {
            log.info("normandy listener[" + napoliClient.getName() + "] start ok!");
        }
    }

    public static String getIP() {
        List<String> list = new ArrayList<String>();

        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            if (e == null) {
                return "127.0.0.1";
            }

            while (e.hasMoreElements()) {
                NetworkInterface item = e.nextElement();

                if (item == null) {
                    continue;
                }

                if (item.isLoopback()) {
                    continue;
                }

                if (item.isVirtual()) {
                    continue;
                }

                if (!item.isUp()) {
                    continue;
                }

                for (InterfaceAddress address : item.getInterfaceAddresses()) {
                    if (address == null) {
                        continue;
                    }

                    if (address.getAddress() instanceof Inet4Address) {
                        Inet4Address inet4Address = (Inet4Address) address.getAddress();

                        list.add(inet4Address.getHostAddress());
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        if (list.size() == 0) {
            return "127.0.0.1";
        }

        return list.get(0);
    }

    /*
     * private void reloadConsumer(final NapoliClient napoliClient) { try {
     *//*
        * if (logger.isInfoEnabled()) { logger.info("run heartbeat schedule"); }
        *//*
           * DestinationContext destinationContext =
           * napoliClient.getDestinationContext();
           * //对于新增的machine,启动listener,放在heartbeat中是为了避免init且没有start时
           * ，避免listener的启动 synchronized
           * (destinationContext.getReceiveMachineList()) { for (Machine machine
           * : destinationContext.getReceiveMachineList()) { if
           * (destinationContext.getConsumerMachineMap().get(machine) == null) {
           * //create consumer try { if (log.isInfoEnabled()) {
           * log.info("open consumer for new machine " + machine); }
           * DefaultReceiverImpl defaultReceiver = (DefaultReceiverImpl)
           * napoliClient; TransportConsumer transportConsumer =
           * transportFactory.getTransportConsumer(
           * defaultReceiver.getConnector(), machine,
           * destinationContext.getConnectionParam(),
           * defaultReceiver.getWorker(), defaultReceiver.getFilterList());
           * destinationContext.getConsumerMachineMap().put(machine,
           * transportConsumer); transportConsumer.startListen(); } catch
           * (Throwable e) { if
           * (!e.getCause().toString().contains("java.io.InterruptedIOException"
           * ) && !e.getMessage().equals("Interrupted.") &&
           * !e.getMessage().equals("The Consumer is closed")) {
           * log.error("yanny e.getClass is " + e.getClass() + " getCause is " +
           * e.getCause()); log.error(e.getMessage(), e); } } } } } } catch
           * (Exception e) { log.warn("normandy reloadConsumer happen error",
           * e); } }
           */

}
