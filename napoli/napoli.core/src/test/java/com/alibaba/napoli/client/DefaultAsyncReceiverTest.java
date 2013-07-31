package com.alibaba.napoli.client;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.client.async.RedeliveryCallback;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.util.ConfigServiceHttpImpl;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.connector.ConfigLoaderSchedule;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.domain.client.ClientDestination;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.receiver.RedeliveryStrategy;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * DefaultAsyncReceiver unit test
 * 
 * @author munch.wangr
 */
public class DefaultAsyncReceiverTest {
    private static final Log      log       = LogFactory.getLog("DefaultAsyncReceiverTest");

    private NapoliConnector       consoleConnector;

    private ConfigServiceHttpImpl configService;
    static String                 testName  = "";

    static String                 queueName = "DefaultAsyncReceiverTest_" + System.currentTimeMillis();
    static List<String>           queues    = new ArrayList<String>();

    DefaultAsyncReceiver          receiver;

    @BeforeClass
    public static void init() throws Exception {

        NapoliConstant.CONNECTION_CHECK_PERIOD = 1000 * 2;
        NapoliConstant.MAX_IDLE_TIME = 1000 * 3;

        NapoliConstant.MIN_HEARTBEAT_PERIOD = 1000 * 1;

    }

    @Before
    public void setup() {


        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName,
                NapoliTestUtil.getProperty("napoli.func.mixGroupName"));

        log.info("yanny: DefaultAsyncReceiverTest started with queueName=" + queueName);

        queues.clear();
        queues.add(queueName);
        
        log.info("test started!");
        consoleConnector = new NapoliConnector("N/A");
        configService = mock(ConfigServiceHttpImpl.class);
        createDestination(true, true, true, true);
        consoleConnector.setConfigService(configService);
        consoleConnector.setInterval(2000);
        consoleConnector.setConnectionTimeout(1000 * 60 * 60);
        consoleConnector.init();

     

    }

    @After
    public void close() {
        log.info("test " + testName + " finished");
        consoleConnector.close();
        receiver.close();
        HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
      ConsoleConnector.closeAll();
    }

    private ClientDestination createDestination(boolean firstSendable, boolean firstReceivable, boolean secondSendable, 
                                                boolean secondReceivable) {
        NapoliConnector localConnector = new NapoliConnector();
        localConnector.setAddress(NapoliTestUtil.getAddress());
        localConnector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath") + "\\" + new Date().hashCode());
        localConnector.setPoolSize(5);

        localConnector.setIdlePeriod(1000);
        localConnector.init();

        
        ClientDestination dest = localConnector.getConfigService().fetchDestination(queueName);
        ClientQueue realQueue = null;
        Map<String, ClientDestination> destMap = new HashMap<String, ClientDestination>();

        if (dest instanceof ClientQueue) {
            realQueue = (ClientQueue) dest;
            if (((ClientQueue) dest).getMachineSet().size() < 2) {
                fail("Expected " + queueName + " has at least 2 physicial queues");
            }

        } else {
            fail("Expected " + queueName + " to be a queue, but it's not");
        }

        Set<ClientMachine> machineSet = new HashSet<ClientMachine>();
        Iterator<ClientMachine> realMachines = realQueue.getMachineSet().iterator();

        ClientQueue queueEntity = new ClientQueue();
        queueEntity.setName(queueName);
        ClientMachine machine1 = new ClientMachine();

        ClientMachine clientMachine1 = realMachines.next();
        ClientMachine clientMachine2 = realMachines.next();
        
        machine1.setIp(clientMachine1.getIp());
        machine1.setPort(clientMachine1.getPort());
        machine1.setMachineType(clientMachine1.getMachineType());
        machine1.setSendable(firstSendable);
        machine1.setReceiveable(firstReceivable);

        ClientMachine machine2 = new ClientMachine();
        machine2.setIp(clientMachine2.getIp());
        machine2.setPort(clientMachine2.getPort());
        machine2.setMachineType(clientMachine2.getMachineType());
        machine2.setSendable(secondSendable);
        machine2.setReceiveable(secondReceivable);

        machineSet.add(machine1);
        machineSet.add(machine2);
        queueEntity.setMachineSet(machineSet);

        queueEntity.setModified(System.currentTimeMillis());

        //make sure different test round triggers destination context refresh by force the modified increase.

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        destMap.put(queueName, queueEntity);

        when(configService.fetchDestination(queues)).thenReturn(destMap);
        log.error("yanny set mock configService fetchDestination for " + queueName);
        when(configService.fetchDestination(queueName)).thenReturn(destMap.get(queueName));           
    
        return queueEntity;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWorkerNull() {
        testName = "testSetWorkerNull";
        receiver = new DefaultAsyncReceiver();
        receiver.setWorker((AsyncWorker) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWorkerExist() {
        testName = "testSetWorkerExist";
        receiver = new DefaultAsyncReceiver();
        NapoliWorker worker = mock(AsyncWorker.class);
        receiver.setWorker((AsyncWorker) worker);

        receiver.setWorker((AsyncWorker) worker);

    }

    @Test
    public void testSetRedeliveryCallback() {
        testName = "testSetRedeliveryCallback";
        RedeliveryCallback callback = mock(RedeliveryCallback.class);
        receiver = new DefaultAsyncReceiver();
        receiver.setRedeliveryCallback(callback);

        RedeliveryStrategy redeliveryStrategy = receiver.getRedeliveryStrategy();
        assertTrue(redeliveryStrategy != null);
        assertTrue(redeliveryStrategy.getRedeliveryCallback().equals(callback));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRedeliveryCallbackNull() {
        testName = "testSetRedeliveryCallbackNull";
        receiver = new DefaultAsyncReceiver();
        receiver.setRedeliveryCallback(null);

    }

    @Test //todo: make this work
    public void testConfigLoadChangeState() throws Exception {

    	
        testName = "testConfigLoadChangeState";
        receiver = new DefaultAsyncReceiver();

        receiver.setConnector(consoleConnector);
        receiver.setName(queueName);
        receiver.setInstances(5);
        receiver.setStoreEnable(false);
        receiver.setPeriod(NapoliConstant.MIN_HEARTBEAT_PERIOD);

        receiver.setWorker(mock(AsyncWorker.class));
        // receiver.setTransportFactory(mockFactory);
        receiver.init();

        receiver.start();

        //Step 1, machines are Working, Working status.
        int instance1 = receiver.getInstances();
        int consumerCount1 = receiver.getDestinationContext().getConsumerMachineMap().size();

        log.info("yanny: 1. instance should be 6 and consumerCount should be 2 when 2 machines are available, instance is:"
                + instance1 + "; consumer is" + consumerCount1);

        assertTrue("instance should be 6 and consumerCount should be 2 when 2 machines are available, but instance is:"
                + instance1 + "; consumer is" + consumerCount1, instance1 == 6 && consumerCount1 == 2);

        //Step 2, machine1 is not sendable, but receiverable
        ClientDestination queueDest = createDestination(true, false, true, true);
        ClientMachine machine1 = ((ClientQueue) queueDest).getMachineSet().iterator().next();

        ConfigLoaderSchedule configLoaderSchedule = new ConfigLoaderSchedule(configService, consoleConnector);

        log.info("yanny kick of configLoad, when will heart beat finish?");

        configLoaderSchedule.run();

        //make sure heart beat happened.
        Thread.sleep(NapoliConstant.MIN_HEARTBEAT_PERIOD + 2000);
        int instance2 = receiver.getInstances();
        int consumerCount2 = receiver.getDestinationContext().getConsumerMachineMap().size();

        log.info("yanny: 2. instance should be 5 and consumerCount should be 1 when only 1 machines are available, instance is:"
                + instance2 + "; consumer is" + consumerCount2);

        assertTrue("instance should be 5 and consumerCount should be 1 when 1 machines are available, but instance is:"
                + instance2 + "; consumer is" + consumerCount2, instance2 == 5 && consumerCount2 == 1);

        //wait until the first machine's connection is closed by connectionCheckSchedule. then check it could recover the session.
        Thread.sleep(NapoliConstant.MAX_IDLE_TIME + 1000);
        assertTrue("the connection for machine " + machine1 + " should be closed due to idle",
                consoleConnector.getConsumerConnection(machine1) == null);

        //Step 3, machine are working, working status 
        queueDest = createDestination(true, true, true, true);
        configLoaderSchedule.run();

        //make sure heart beat happened.
        Thread.sleep(NapoliConstant.MIN_HEARTBEAT_PERIOD + 2000);
        int instance3 = receiver.getInstances();
        int consumerCount3 = receiver.getDestinationContext().getConsumerMachineMap().size();

        log.info("yanny: 3. instance should be 6 and consumerCount should be 2 when only 2 machines are available, instance is:"
                + instance3 + "; consumer is" + consumerCount3);

        assertTrue("instance should be 6 and consumerCount should be 2 when 2 machines are available, but instance is:"
                + instance3 + "; consumer is" + consumerCount3, instance3 == 6 && consumerCount3 == 2);

        //Step 4, machines are working, stop status
        //switch back two machine's status
        queueDest = createDestination(true, true, true, false);
        configLoaderSchedule.run();

        //make sure heart beat happened.
        Thread.sleep(NapoliConstant.MIN_HEARTBEAT_PERIOD + 2000);
        int instance4 = receiver.getInstances();
        int consumerCount4 = receiver.getDestinationContext().getConsumerMachineMap().size();

        log.info("yanny: 4. instance should be 5 and consumerCount should be 1 when only 1 machines are available, instance is:"
                + instance4 + "; consumer is" + consumerCount4);

        assertTrue("instance should be 5 and consumerCount should be 1 when 1 machines are available, but instance is:"
                + instance4 + "; consumer is" + consumerCount4, instance4 == 5 && consumerCount4 == 1);

        Thread.sleep(NapoliConstant.MAX_IDLE_TIME + 1000);
        Iterator it = ((ClientQueue) queueDest).getMachineSet().iterator();
        it.next();

        ClientMachine machine2 = (ClientMachine) it.next();

        assertTrue("the connection for machine " + machine2 + " should be closed due to idle",
                consoleConnector.getConsumerConnection(machine2) == null);

        //Step 5, machines are working working status
        //enable two machines again
        queueDest = createDestination(true, true, true, true);
        configLoaderSchedule.run();

        //make sure heart beat happened.
        Thread.sleep(NapoliConstant.MIN_HEARTBEAT_PERIOD + 2000);
        int instance5 = receiver.getInstances();
        int consumerCount5 = receiver.getDestinationContext().getConsumerMachineMap().size();

        log.info("yanny: 5. instance should be 6 and consumerCount should be 2 when only 2 machines are available, instance is:"
                + instance5 + "; consumer is" + consumerCount5);

        assertTrue("instance should be 6 and consumerCount should be 2 when 2 machines are available, but instance is:"
                + instance5 + "; consumer is" + consumerCount5, instance5 == 6 && consumerCount5 == 2);

        //Step 6, machines are stop, working status
        //switch back two machine's status
        queueDest = createDestination(true, false, true, true);
        configLoaderSchedule.run();

        //make sure heart beat happened.
        Thread.sleep(NapoliConstant.MIN_HEARTBEAT_PERIOD + 2000);
        int instance6 = receiver.getInstances();
        int consumerCount6 = receiver.getDestinationContext().getConsumerMachineMap().size();

        log.info("yanny: 6. instance should be 5 and consumerCount should be 1 when only 1 machines are available, instance is:"
                + instance6 + "; consumer is" + consumerCount6);

        assertTrue("instance should be 5 and consumerCount should be 1 when 1 machines are available, but instance is:"
                + instance6 + "; consumer is" + consumerCount6, instance6 == 5 && consumerCount6 == 1);
    }

    @Test
    //"Bug NP-215"    
    public void testConfigLoadChangeBeforeReceiverStart() throws Exception {
        testName = "testConfigLoadChangeBeforeReceiverStart";
        receiver = new DefaultAsyncReceiver();
        receiver.setConnector(consoleConnector);
        receiver.setName(queueName);
        receiver.setInstances(5);
        receiver.setStoreEnable(false);
        receiver.setPeriod(NapoliConstant.MIN_HEARTBEAT_PERIOD);

        receiver.setWorker(mock(AsyncWorker.class));
        // receiver.setTransportFactory(mockFactory);
        receiver.init();

        //consumer listener should not be started.

        int instance1 = receiver.getInstances();
        int consumerCount1 = receiver.getDestinationContext().getConsumerMachineMap().size();

        log.info("yanny: 1. instance should be 0 and consumerCount should be 0 when 2 machines are available but receiver is not started, instance is:"
                + instance1 + "; consumer is" + consumerCount1);

        assertTrue(
                "instance should be 0 and consumerCount should be 0 when 2 machines are available but receiver is not started, but instance is:"
                        + instance1 + "; consumer is" + consumerCount1, instance1 == 0 && consumerCount1 == 0);

        createDestination(false, true, true, true);

        ConfigLoaderSchedule configLoaderSchedule = new ConfigLoaderSchedule(configService, consoleConnector);

        configLoaderSchedule.run();

        //make sure heart beat happened.
        Thread.sleep(NapoliConstant.MIN_HEARTBEAT_PERIOD + 500);
        int instance2 = receiver.getInstances();
        int consumerCount2 = receiver.getDestinationContext().getConsumerMachineMap().size();

        //verify consumer listeners are still not started.

        log.info("yanny: 2. instance should be 0 and consumerCount should be 0 when only 1 machines are available after config load changed since receiver is not started, instance is:"
                + instance2 + "; consumer is" + consumerCount2);

        assertTrue(
                "instance should be 0 and consumerCount should be 0 when only 1 machines are available after config load changed since receiver is not started, but instance is:"
                        + instance2 + "; consumer is" + consumerCount2, instance2 == 0 && consumerCount2 == 0);
    }

}
