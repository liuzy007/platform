package com.alibaba.napoli.sender.impl;

import org.junit.AfterClass;

import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.common.util.ConfigServiceHttpImpl;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.mqImpl.TransportFactory;
import com.alibaba.napoli.sender.NapoliSender;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.spi.TransportSender;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: heyman Date: 12/28/11 Time: 2:43 下午
 */
public abstract class AbstractSenderTest {
    protected NapoliConnector consoleConnector;

    protected DefaultSenderImpl createQueueSenderWithMock(String queueName, boolean storeEnable) throws Exception {
        ClientQueue queueEntity = createQueueEntity(queueName, 1, true, true, true, true);

        consoleConnector = new NapoliConnector("N/A", 5, 5);
        ConfigServiceHttpImpl configService = mock(ConfigServiceHttpImpl.class);
        when(configService.fetchDestination(queueName)).thenReturn(queueEntity);
        consoleConnector.setConfigService(configService);
        consoleConnector.setInterval(3000 * 1000);
        consoleConnector.init();

        DefaultSenderImpl sender = new DefaultSenderImpl();
        sender.setConnector(consoleConnector);
        sender.setName(queueName);
        sender.setStoreEnable(storeEnable);
        sender.setReprocessInterval(3000 * 1000);
        TransportFactory mockFactory = mock(TransportFactory.class);
        TransportSender mockTransSender = mock(TransportSender.class);
        when(mockFactory.getTrasportSender(any(NapoliSenderContext.class))).thenReturn(mockTransSender);
        NapoliSender mockNapoliSender = mock(NapoliSender.class);
        sender.setNapoliSender(mockNapoliSender);
        sender.init();
        return sender;
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
      ConsoleConnector.closeAll();
    }
    protected ClientQueue createQueueEntity(String name, int id, boolean firstMachine, boolean SecondMachine,
                                            boolean firstSendable, boolean secondSendable) {
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
        queueEntity.setModified(System.currentTimeMillis());
        return queueEntity;
    }
}
