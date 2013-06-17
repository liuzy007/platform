package com.alibaba.demo.verify;

import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.common.util.ConfigServiceHttpImpl;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.domain.client.ClientVirtualTopic;
import com.alibaba.napoli.mqImpl.TransportFactory;
import com.alibaba.napoli.sender.NapoliResult;
import com.alibaba.napoli.sender.NapoliSender;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.sender.impl.DefaultSenderImpl;
import com.alibaba.napoli.spi.TransportSender;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * User: heyman
 * Date: 12/28/11
 * Time: 10:42 上午
 */
public class VtopicSenderTest {
    public void testEmptyTopicSend() throws Exception {
        ClientVirtualTopic vtopic = new ClientVirtualTopic();
        vtopic.setName("emptyTopic");
        NapoliConnector consoleConnector = new NapoliConnector("N/A",5,5);
        ConfigServiceHttpImpl configService = mock(ConfigServiceHttpImpl.class);
        when(configService.fetchDestination("emptyTopic")).thenReturn(vtopic);
        consoleConnector.setConfigService(configService);
        consoleConnector.setInterval(3000 * 1000);
        consoleConnector.init();

        DefaultSenderImpl sender = new DefaultSenderImpl();
        sender.setConnector(consoleConnector);
        sender.setName("emptyTopic");
        sender.setStoreEnable(true);
        //sender.setTransportFactory(mockFactory);
        sender.setReprocessInterval(3000 * 1000);
        sender.init();
        assertFalse(sender.sendMessage("test"));
        sender.close();
        consoleConnector.close();
    }

    public void testNormalTopic() throws Exception {
        ClientVirtualTopic myTopic = new ClientVirtualTopic();
        myTopic.setName("myTopic");
        List<ClientQueue> topicQueues = new ArrayList<ClientQueue>();

        topicQueues.add(createQueueEntity("q1", 1, true, true, true, true));
        topicQueues.add(createQueueEntity("q2", 2, true, true, true, true));
        topicQueues.add(createQueueEntity("q3", 3, true, true, true, true));
        myTopic.setClientQueueList(topicQueues);
        
        NapoliConnector consoleConnector = new NapoliConnector("N/A",5,5);
        ConfigServiceHttpImpl configService = mock(ConfigServiceHttpImpl.class);
        when(configService.fetchDestination("myTopic")).thenReturn(myTopic);
        consoleConnector.setConfigService(configService);
        consoleConnector.setInterval(3000 * 1000);
        consoleConnector.init();

        DefaultSenderImpl sender = new DefaultSenderImpl();
        sender.setConnector(consoleConnector);
        sender.setName("myTopic");
        sender.setStoreEnable(false);
        sender.setReprocessInterval(3000 * 1000);
        TransportFactory mockFactory = mock(TransportFactory.class);
        TransportSender mockTransSender = mock(TransportSender.class);
        when(mockFactory.getTrasportSender(any(NapoliSenderContext.class))).thenReturn(mockTransSender);
        NapoliSender mockNapoliSender = mock(NapoliSender.class);
        NapoliResult result = new NapoliResult(true);
        when(mockNapoliSender.sendMessage(any(NapoliSenderContext.class))).thenReturn(result);
        sender.setNapoliSender(mockNapoliSender);
        sender.init();
        System.out.println("yanny start sending...");
        assertTrue(sender.sendMessage("test"));
        verify(mockFactory, times(3)).getTrasportSender(any(NapoliSenderContext.class));
        sender.close();
        consoleConnector.close();

    }

    private ClientQueue createQueueEntity(String name, int id, boolean firstMachine, boolean SecondMachine, boolean firstSendable, boolean secondSendable) {
        ClientQueue queueEntity = new ClientQueue();
        queueEntity.setName(name);
        
        ClientMachine machine1 = new ClientMachine();
        machine1.setIp("127.0.0.1");
        machine1.setPort(61616);
        machine1.setSendable(firstMachine);

        ClientMachine machine2 = new ClientMachine();
        machine2.setIp("127.0.0.1");
        machine2.setPort(62616);
        machine2.setSendable(SecondMachine);

        Set<ClientMachine> machines = new HashSet<ClientMachine>();
        machines.add(machine1);
        machines.add(machine2);
        queueEntity.setMachineSet(machines);

        queueEntity.setModified(System.currentTimeMillis());
        return queueEntity;
    }

    public static void main(String[] args) throws Exception{
        VtopicSenderTest vtopicSenderTest = new VtopicSenderTest();
        vtopicSenderTest.testEmptyTopicSend();
        vtopicSenderTest.testNormalTopic();
    }


}
