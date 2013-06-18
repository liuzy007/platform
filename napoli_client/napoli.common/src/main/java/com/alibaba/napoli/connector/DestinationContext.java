package com.alibaba.napoli.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.domain.client.ClientDestination;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.domain.client.ClientTopic;
import com.alibaba.napoli.domain.client.ClientVirtualTopic;
import com.alibaba.napoli.sender.Sender;
import com.alibaba.napoli.sender.selector.RoundRobinSelector;
import com.alibaba.napoli.sender.selector.Selector;
import com.alibaba.napoli.sender.selector.SelectorUtil;
import com.alibaba.napoli.spi.TransportConsumer;

/**
 * User: heyman Date: 12/1/11 Time: 11:30 上午
 */
public class DestinationContext {
    private static final Log                                         log                 = LogFactory
                                                                                                 .getLog(DestinationContext.class);
    private ClientDestination                                        destination;
    private ConnectionParam                                          connectionParam;

    protected int                                                    instances           = 5;

    private volatile ConcurrentMap<String, List<ClientMachine>>      sendQueueMachineMap = new ConcurrentHashMap<String, List<ClientMachine>>();
    private volatile ConcurrentMap<String, Selector<ClientMachine>>  queueSelectorMap    = new ConcurrentHashMap<String, Selector<ClientMachine>>();
    private volatile List<Sender>                                    senders             = new ArrayList<Sender>();

    //private List<ReceiveObserver> receiveObserverList = new ArrayList<ReceiveObserver>();

    private volatile List<ClientMachine>                             receiveMachineList  = new ArrayList<ClientMachine>();
    private volatile ConcurrentMap<ClientMachine, TransportConsumer> consumerMachineMap  = new ConcurrentHashMap<ClientMachine, TransportConsumer>();

    public void setInstances(int instances) {
        if (instances < 1) {
            this.instances = 1;
        } else {
            this.instances = instances;
        }
    }

    public int getInstances() {
        int count = 0;
        for (Map.Entry<ClientMachine, TransportConsumer> entry : consumerMachineMap.entrySet()) {
            TransportConsumer transportConsumer = entry.getValue();
            count += transportConsumer.getInstance();
        }
        return count;
    }

    public int getExpectedInstances() {
        int size = receiveMachineList.size();
        int revSessions = 0;
        if (size > 0) {
            revSessions = (int) ((instances + (size - 1)) / size) * size;
        }

        return revSessions;
    }

    public DestinationContext(ClientDestination destination) {
        this.destination = destination;
        init();
    }

    private void init() {
        ConcurrentMap<String, List<ClientMachine>> newSendQueueMachineMap = new ConcurrentHashMap<String, List<ClientMachine>>();
        ConcurrentMap<String, Selector<ClientMachine>> newQueueSelectorMap = new ConcurrentHashMap<String, Selector<ClientMachine>>();
        List<ClientMachine> newReceiveMachineList = new ArrayList<ClientMachine>();

        if (destination instanceof ClientQueue) {
            addQueueEntity((ClientQueue) destination, newSendQueueMachineMap, newQueueSelectorMap,
                    newReceiveMachineList);
        } else if (destination instanceof ClientVirtualTopic) {
            ClientVirtualTopic vtopicDestination = (ClientVirtualTopic) destination;
            if (vtopicDestination.getClientQueueList() != null) {
                for (ClientQueue clientQueue : vtopicDestination.getClientQueueList()) {
                    //QueueEntity queueEntity = entry.getValue();
                    addQueueEntity(clientQueue, newSendQueueMachineMap, newQueueSelectorMap, newReceiveMachineList);
                }
            }
        } else if (destination instanceof ClientTopic) {
            ClientTopic topicEntity = (ClientTopic) destination;
            addTopicEntity(topicEntity, newSendQueueMachineMap, newQueueSelectorMap, newReceiveMachineList);
        } else {
            throw new RuntimeException("destinationContext type isn't be supported!");
        }
        sendQueueMachineMap = newSendQueueMachineMap;
        queueSelectorMap = newQueueSelectorMap;
        receiveMachineList = newReceiveMachineList;
    }

    public synchronized void refresh() {
        init();
        for (Sender sender : senders) {
            sender.refreshSchedule();
        }
        reloadConsumer();
    }

    public synchronized void reloadConsumer() {
        if (connectionParam == null) {
            return;
        }

        int size = receiveMachineList.size();
        if (size == 0) {
            connectionParam.setReceiverSessions(0);
        } else {
            int revSessions = (instances + (size - 1)) / size;
            connectionParam.setReceiverSessions(revSessions);
        }

        //删除多余的machine consumer
        Iterator<Map.Entry<ClientMachine, TransportConsumer>> iterator = consumerMachineMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ClientMachine, TransportConsumer> entry = iterator.next();
            ClientMachine machine = entry.getKey();
            TransportConsumer consumer = entry.getValue();
            if (!receiveMachineList.contains(machine)) {
                consumer.stopListen();
                iterator.remove();
            }
        }

        //move start new machine consumer to heartBeat.run to avoid bug NP-215
    }

    public Map<String, ClientMachine> getSendMachineMap() {
        Map<String, ClientMachine> machineMap = new HashMap<String, ClientMachine>();
        for (Map.Entry<String, List<ClientMachine>> entry : sendQueueMachineMap.entrySet()) {
            String queueName = entry.getKey();
            List<ClientMachine> machineList = entry.getValue();
            Selector<ClientMachine> selector = queueSelectorMap.get(queueName);
            if (selector != null && machineList.size() > 0) {
                ClientMachine machine = selector.select(machineList);
                machineMap.put(queueName, machine);
            } else {
                machineMap.put(queueName, null);
            }
        }
        return machineMap;
    }
    
    public ClientMachine getSendMachine(String queueName){
        List<ClientMachine> machineList = sendQueueMachineMap.get(queueName);
        if (machineList == null || machineList.size() == 0){
            return null;
        }else{
            Selector<ClientMachine> selector = queueSelectorMap.get(queueName);
            return selector.select(machineList);
        }
    }

    public ClientDestination getDestination() {
        return destination;
    }

    private void addQueueEntity(ClientQueue queueEntity,
                                ConcurrentMap<String, List<ClientMachine>> newSendQueueMachineMap,
                                ConcurrentMap<String, Selector<ClientMachine>> newQueueSelectorMap,
                                List<ClientMachine> newReceiveMachineList) {
        //final Map<Integer, PhysicalQueue> physicalQueueMap = queueEntity.getPhysicalQueueMap();
        final List<ClientMachine> sendMachineList = new ArrayList<ClientMachine>();
        Set<ClientMachine> machines = queueEntity.getMachineSet();
        if (machines != null){
            for (ClientMachine machine : machines) {
                if (machine.isSendable()) {
                    sendMachineList.add(machine);
                }
                if (machine.isReceiveable()) {
                    newReceiveMachineList.add(machine);
                }
            } 
        }
        /*
         * for (final PhysicalQueue pq : physicalQueueMap.values()) { if
         * (queueEntity.isSendable() && SelectorUtil.canPhysicalQueueSend(pq)) {
         * sendMachineList.add(pq.getMachine()); } if
         * (queueEntity.isReceivable() && canPhysicalQueueReceive(pq)) {
         * newReceiveMachineList.add(pq.getMachine()); } }
         */
        newSendQueueMachineMap.put(queueEntity.getName(), sendMachineList);
        Selector<ClientMachine> selector = SelectorUtil.createSelector(queueEntity.getSendStrategy(), queueEntity);
        if (selector != null) {
            newQueueSelectorMap.put(queueEntity.getName(), selector);
        } else {
            newQueueSelectorMap.put(queueEntity.getName(), new RoundRobinSelector<ClientMachine>());
        }
    }

    private void addTopicEntity(ClientTopic topicEntity,
                                ConcurrentMap<String, List<ClientMachine>> newSendQueueMachineMap,
                                ConcurrentMap<String, Selector<ClientMachine>> newQueueSelectorMap,
                                List<ClientMachine> newReceiveMachineList) {
        //final Map<Integer, PhysicalTopic> physicalTopicMap = topicEntity.getPtMap();
        final List<ClientMachine> sendMachineList = new ArrayList<ClientMachine>();
        if (topicEntity.getMachineSet() != null){
            for (ClientMachine machine : topicEntity.getMachineSet()) {
                if (machine.isSendable()) {
                    sendMachineList.add(machine);
                }
                if (machine.isReceiveable()) {
                    newReceiveMachineList.add(machine);
                }
            }
        }
        /*
         * for (final PhysicalTopic pt : physicalTopicMap.values()) { if
         * (SelectorUtil.canPhysicalTopicSend(pt)) {
         * sendMachineList.add(pt.getMachine()); } if
         * (canPhysicalTopicReceive(pt)) {
         * newReceiveMachineList.add(pt.getMachine()); } }
         */
        newSendQueueMachineMap.put(topicEntity.getName(), sendMachineList);
        newQueueSelectorMap.put(topicEntity.getName(), new RoundRobinSelector<ClientMachine>());
    }

    /**
     * used by router app
     */
    /*
     * public boolean isSendable(String queueName) { List<ClientMachine>
     * machineList = sendQueueMachineMap.get(queueName); return machineList !=
     * null && machineList.size() > 0; } public static boolean
     * canPhysicalQueueReceive(final ClientQueue physicalQueue) { if
     * (!physicalQueue.isReceiveable()) { return false; } if
     * (physicalQueue.getState() != PhysicalQueue.STATE_WORKING &&
     * physicalQueue.getState() != PhysicalQueue.STATE_STOPPING) { return false;
     * } final Machine machine = physicalQueue.getMachine(); if
     * (machine.getState() != Machine.STATE_WORKING) { //
     * 正常情况下不能执行到这里，能加上物理Queue中的Machine一定是WORKING的状态！ // 这一点由 Napoli
     * Server的控制台保证。 if (log.isErrorEnabled()) {
     * log.error("the state of machine(id:" + machine.getAddress() +
     * ") is NOT working, " + "but this machine belongs to a physical queue(id:"
     * + physicalQueue.getId() + ")!"); } return false; } return true; } public
     * static boolean canPhysicalTopicReceive(final PhysicalTopic physicalTopic)
     * {
     *//*
        * if (!physicalTopic.isReceivable()) { return false; }
        *//*
           * if (physicalTopic.getState() != PhysicalQueue.STATE_WORKING &&
           * physicalTopic.getState() != PhysicalQueue.STATE_STOPPING) { return
           * false; } final Machine machine = physicalTopic.getMachine(); if
           * (machine.getState() != Machine.STATE_WORKING) { //
           * 正常情况下不能执行到这里，能加上物理Queue中的Machine一定是WORKING的状态！ // 这一点由 Napoli
           * Server的控制台保证。 if (log.isErrorEnabled()) {
           * log.error("the state of machine(id:" + machine.getAddress() +
           * ") is NOT working, " +
           * "but this machine belongs to a physical queue(id:" +
           * physicalTopic.getId() + ")!"); } return false; } return true; }
           */

    public void setDestination(ClientDestination destination) {
        this.destination = destination;
    }

    /*
     * public void attachReceiveObserver(ReceiveObserver receiveObserver) {
     * receiveObserverList.add(receiveObserver); } public void
     * detachReceiveObserver(ReceiveObserver receiveObserver) {
     * receiveObserverList.remove(receiveObserver); }
     */

    public void attachSender(Sender sender) {
        senders.add(sender);
    }

    public ConcurrentMap<ClientMachine, TransportConsumer> getConsumerMachineMap() {
        return consumerMachineMap;
    }

    public List<ClientMachine> getReceiveMachineList() {
        return receiveMachineList;
    }

    public List<ClientMachine> getSendQueueMachineList(String name) {
        return this.sendQueueMachineMap.get(name);
    }

    public void setConnectionParam(ConnectionParam connectionParam) {
        this.connectionParam = connectionParam;
    }

    public ConnectionParam getConnectionParam() {
        return connectionParam;
    }

    /**
     * used by router app
     */
    public boolean isSameMachineType(String machineType) {
        for (Map.Entry<String, List<ClientMachine>> entry : sendQueueMachineMap.entrySet()) {
            List<ClientMachine> machineList = entry.getValue();
            for (ClientMachine machine : machineList) {
                if (!machineType.equals(machine.getMachineType())) {
                    return false;
                }
            }
        }
        return true;
    }
}
