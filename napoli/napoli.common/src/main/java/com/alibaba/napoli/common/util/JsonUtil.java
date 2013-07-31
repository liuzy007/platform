package com.alibaba.napoli.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.napoli.domain.client.ClientDestination;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.domain.client.ClientTopic;
import com.alibaba.napoli.domain.client.ClientVirtualTopic;
import java.util.List;

/**
 * User: heyman Date: 6/5/12 Time: 2:29 PM
 */
public class JsonUtil {
    private final static String TOPIC_TYPE  = "\"type\":\"topic\"";
    private final static String QUEUE_TYPE  = "\"type\":\"queue\"";
    private final static String VTOPIC_TYPE = "\"type\":\"vtopic\"";

    public static ClientDestination fromJson(String jsonStr) {
        if (jsonStr.contains(VTOPIC_TYPE)) {
            return JSON.parseObject(jsonStr, ClientVirtualTopic.class);
        } else if (jsonStr.contains(TOPIC_TYPE)) {
            return JSON.parseObject(jsonStr, ClientTopic.class);
        } else if (jsonStr.contains(QUEUE_TYPE)) {
            return JSON.parseObject(jsonStr, ClientQueue.class);
        } else {
            throw new IllegalStateException("the destination type is not support!");
        }
    }
    
    public static List<String> getVtopicQueues(String jsonStr){
        return JSON.parseArray(jsonStr,String.class);
    }

    /*
     * public static ClientDestination fromDestination(Destination destination){
     * if (destination instanceof QueueEntity){ return
     * convertToClientQueue((QueueEntity) destination); }else if (destination
     * instanceof VirtualTopic){ VirtualTopic virtualTopic =
     * (VirtualTopic)destination; ClientVirtualTopic clientVirtualTopic = new
     * ClientVirtualTopic(); clientVirtualTopic.setName(virtualTopic.getName());
     * clientVirtualTopic.setModified(virtualTopic.getModified());
     * clientVirtualTopic.setSendable(true);
     * clientVirtualTopic.setReceiveable(true); List<ClientQueue>
     * clientQueueList = new ArrayList<ClientQueue>(); Map<Integer, QueueEntity>
     * queueEntityMap = virtualTopic.getQueueMap(); if (queueEntityMap != null
     * && queueEntityMap.size() > 0){ for (QueueEntity
     * queueEntity:queueEntityMap.values()){ ClientQueue clientQueue =
     * convertToClientQueue(queueEntity); clientQueueList.add(clientQueue); } }
     * clientVirtualTopic.setClientQueueList(clientQueueList); return
     * clientVirtualTopic; }else if (destination instanceof TopicEntity){
     * TopicEntity topicEntity = (TopicEntity) destination; ClientTopic
     * clientTopic = new ClientTopic();
     * clientTopic.setName(topicEntity.getName());
     * clientTopic.setModified(topicEntity.getModified());
     * clientTopic.setSendable(true); clientTopic.setReceiveable(true); return
     * clientTopic; } return null; } public static ClientQueue
     * convertToClientQueue(QueueEntity queueEntity){ ClientQueue clientQueue =
     * new ClientQueue(); clientQueue.setName(queueEntity.getName());
     * clientQueue.setModified(queueEntity.getModified());
     * clientQueue.setSendable(queueEntity.isSendable());
     * clientQueue.setReceiveable(queueEntity.isReceivable());
     * clientQueue.setLoadBlance(queueEntity.getStrategy()); Set<ClientMachine>
     * machines = fromPqMap(queueEntity.getPhysicalQueueMap());
     * clientQueue.setMachineSet(machines); return clientQueue; } private static
     * Set<ClientMachine> fromPqMap(Map<Integer, PhysicalQueue> pqMap){
     * Set<ClientMachine> machines = new HashSet<ClientMachine>(); if (pqMap !=
     * null && pqMap.size() > 0){ for(PhysicalQueue physicalQueue :
     * pqMap.values()){ ClientMachine clientMachine = new ClientMachine();
     * Machine machine = physicalQueue.getMachine();
     * clientMachine.setIp(machine.getIp());
     * clientMachine.setPort(machine.getPort());
     * clientMachine.setMachineType(machine.getMachineType());
     * clientMachine.setReceiveable(physicalQueue.isReceivable());
     * clientMachine.setSendable(physicalQueue.isSendable());
     * clientMachine.setWeight(physicalQueue.getWeight());
     * machines.add(clientMachine); } } return machines; }
     */
}
