package com.alibaba.demo.verify;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.common.util.ConfigServiceHttpImpl;
import com.alibaba.napoli.connector.ConfigLoaderSchedule;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.domain.client.ClientDestination;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.mqImpl.TransportException;
import com.alibaba.napoli.mqImpl.TransportFactory;
import com.alibaba.napoli.sender.NapoliResult;
import com.alibaba.napoli.sender.NapoliSender;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.sender.impl.DefaultSenderImpl;
import com.alibaba.napoli.sender.impl.ResendSchedule;
import com.alibaba.napoli.spi.TransportSender;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * User: heyman
 * Date: 12/19/11
 * Time: 6:07 下午
 */
public class QueueSenderTest {
    private NapoliConnector consoleConnector;
    private TransportFactory mockFactory;
    private ClientDestination heymantest;
    private ConfigServiceHttpImpl configService;
    private TransportSender mockTransSender;
    private NapoliSender mockNapoliSender;

   

    public void setup() throws Exception{
        heymantest = createDestination(true,true);
        consoleConnector = new NapoliConnector("N/A");
        configService = mock(ConfigServiceHttpImpl.class);
        when(configService.fetchDestination("heymantest")).thenReturn(heymantest);
        consoleConnector.setConfigService(configService);
        consoleConnector.setInterval(3000*1000);
        consoleConnector.init();

        mockFactory = mock(TransportFactory.class);
        mockTransSender = mock(TransportSender.class);
        when(mockFactory.getTrasportSender(any(NapoliSenderContext.class))).thenReturn(mockTransSender);
        mockNapoliSender = mock(NapoliSender.class);
        NapoliResult result = new NapoliResult(true);
        when(mockNapoliSender.sendMessage(any(NapoliSenderContext.class))).thenReturn(result);
    }

    public void close() {
        consoleConnector.close();
    }

    public void testNormalSend() throws Exception {
        DefaultSenderImpl sender =createSender("heymantest",false);
        assertTrue(sender.sendMessage("mytest1"));
        assertTrue(sender.sendMessage("mytest2"));
        sender.close();
        assertFalse(sender.sendMessage("mytest3"));

        sender.init();
        assertTrue(sender.sendMessage("mytest4"));
        verify(mockFactory, times(3)).getTrasportSender(any(NapoliSenderContext.class));
    }

    public void testLocalStore() throws Exception {
        doThrow(new TransportException()).when(mockTransSender).send(eq("heymantest"),any(NapoliMessage.class));
        DefaultSenderImpl sender = createSender("heymantest",false);
        if (sender.sendMessage("test")){
            fail("should be fail,because throw exception and storeable is false");
        }
        sender.close();
        sender = createSender("heymantest",true);
    
        KVStore kvStore = ConsoleConnector.getorCreateBdbKvStore(consoleConnector.getStorePath(), "heymantest", NapoliConstant.CLIENT_TYPE_SENDER);
        
        kvStore.clear();
        if (!sender.sendMessage("test")){
            fail("should success,because the storeenable = true");
        }
        assertEquals("there should one localstore item",1,kvStore.getStoreSize());

        ResendSchedule resendSchedule = new ResendSchedule(kvStore,1000,sender);
        resendSchedule.run();
        assertEquals("there should one localstore item again",1,kvStore.getStoreSize());


        for (int i=0;i<300;i++){
            sender.sendMessage("test"+i);
        }
        assertEquals("there should 301 localstore item again",301,kvStore.getStoreSize());
        resendSchedule.run();
        assertEquals("there should 301 localstore item again",301,kvStore.getStoreSize());
        resendSchedule.run();
        assertEquals("there should 301 localstore item again",301,kvStore.getStoreSize());
        sender.close();
        mockTransSender = mock(TransportSender.class);
        when(mockFactory.getTrasportSender(any(NapoliSenderContext.class))).thenReturn(mockTransSender);
        sender =  createSender("heymantest",false);
        resendSchedule = new ResendSchedule(kvStore,1000,sender);
        resendSchedule.run();
        assertEquals("there should one localstore item",0,kvStore.getStoreSize());
    }

    public void testConfigLoadChangeState() throws Exception {
        System.out.println("start to run testConfigLoadChangeState");
        DefaultSenderImpl sender = new DefaultSenderImpl();
        sender.setConnector(consoleConnector);
        sender.setName("heymantest");
        sender.setStoreEnable(false);
        sender.setNapoliSender(mockNapoliSender);
        sender.init();
        heymantest = createDestination(true,true);
        when(configService.fetchDestination("heymantest")).thenReturn(heymantest);
        ConfigLoaderSchedule configLoaderSchedule = new ConfigLoaderSchedule(configService,consoleConnector);
        System.out.println("yanny test run configLoaderSchedule.run();");
        configLoaderSchedule.run();
        assertFalse("machine is stopped,should fail",sender.sendMessage("1"));

        heymantest = createDestination(true,true);
        when(configService.fetchDestination("heymantest")).thenReturn(heymantest);
        System.out.println("yanny test run configLoaderSchedule.run();");
        configLoaderSchedule.run();
        assertTrue("one machine is ok,should success",sender.sendMessage("2"));

        heymantest = createDestination(false,true);
        
        when(configService.fetchDestination("heymantest")).thenReturn(heymantest);
        System.out.println("yanny test run configLoaderSchedule.run();");
        configLoaderSchedule.run();
        assertFalse("all not sendable should fail",sender.sendMessage("3"));
    }

    private ClientDestination createDestination(boolean sendable,boolean receiveable){
        ClientQueue queueEntity = new ClientQueue();
        queueEntity.setName("heymantest");
        
        ClientMachine machine1 = new ClientMachine();
        machine1.setIp("127.0.0.1");
        machine1.setPort(61616);
        machine1.setSendable(sendable);
        machine1.setReceiveable(receiveable);

        ClientMachine machine2 = new ClientMachine();
        machine2.setIp("127.0.0.1");
        machine2.setPort(62616);
        machine2.setSendable(sendable);
        machine2.setReceiveable(receiveable);

        Set<ClientMachine> machineSet = new HashSet<ClientMachine>();
        machineSet.add(machine1);
        machineSet.add(machine2);
        queueEntity.setMachineSet(machineSet);
        
        

        long time = System.currentTimeMillis();
        System.out.println("modified time is set to " + time);
        queueEntity.setModified(time);
        
        //make sure different test round triggers destination context refresh by force the modified increase.
        
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return queueEntity;
    }
    
    private  DefaultSenderImpl createSender(String destinationName,boolean storeable) throws Exception{
        DefaultSenderImpl sender = new DefaultSenderImpl();
        sender.setConnector(consoleConnector);
        sender.setName(destinationName);
        sender.setStoreEnable(storeable);
        sender.setNapoliSender(mockNapoliSender);
        sender.setReprocessInterval(3000*1000);
        sender.init();
        return sender;
    }

    public static void main(String[] args) throws Exception{
        QueueSenderTest queueSenderTest = new QueueSenderTest();
        queueSenderTest.setup();
        queueSenderTest.testConfigLoadChangeState();
        queueSenderTest.close();
        queueSenderTest.setup();
        queueSenderTest.testLocalStore();
        queueSenderTest.close();
        queueSenderTest.setup();
        queueSenderTest.testNormalSend();
        queueSenderTest.close();
        
    }
}
