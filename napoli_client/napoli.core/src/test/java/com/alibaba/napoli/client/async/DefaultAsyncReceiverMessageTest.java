package com.alibaba.napoli.client.async;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.dragoon.stat.napoli.NapoliReceiverStat;
import com.alibaba.dragoon.stat.napoli.NapoliSenderStat;
import com.alibaba.dragoon.stat.napoli.NapoliStatManager;
import com.alibaba.napoli.AbstractTestBase;

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.ext.AsyncWorkerEx;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;

import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;

import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.common.util.JmxUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.model.MyWorker;
import com.alibaba.napoli.client.model.WorkerFailure;
import com.alibaba.napoli.client.model.WorkerRetrySuccess;
import com.alibaba.napoli.client.util.NapoliTestUtil;

import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.persistencestore.KVStore;

import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.receiver.filter.Context;
import com.alibaba.napoli.receiver.filter.Filter;
import com.alibaba.napoli.receiver.filter.FilterFinder;
import com.alibaba.napoli.receiver.impl.ReceiverMonitorFilter;
import com.alibaba.napoli.receiver.impl.ReprocessSchedule;
import com.alibaba.napoli.sender.impl.SenderMonitorFilter;

/**
 * 测试消息的工作者处理接口
 * 
 * @author munch.wangr
 */
public class DefaultAsyncReceiverMessageTest extends AbstractTestBase {

    static DefaultAsyncSender             sender;
  
    static int                            redeliveryCallBackCalled = 0;

    static int                            fetchCount               = 5;

    protected static NapoliStatManager    napoliStatManager        = mock(NapoliStatManager.class);
    protected static NapoliReceiverStat   napoliReceiverStat;
    protected static DefaultAsyncReceiver receiver;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        NapoliConstant.CONNECTION_CHECK_PERIOD = 1000 * 2;
        NapoliConstant.MAX_IDLE_TIME = 1000 * 3;

        NapoliConstant.MIN_HEARTBEAT_PERIOD = 1000 * 1;
        
        queueName = "DefaultAsyncReceiverMessageTest_" + System.currentTimeMillis();
    
        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName, NapoliTestUtil.getProperty("napoli.func.mixGroupName"));
        
        logger.info("yanny: DefaultAsyncReceiverMessageTest started with queueName=" + queueName);
        AbstractTestBase.setUpBeforeClass();

        //change heart beat min period to reduce test time.
        NapoliConstant.MIN_HEARTBEAT_PERIOD = 1000 * 2;

        logger.info("yanny prefetch = " + System.getProperty("prefetch"));

        SenderMonitorFilter.setMonitor(napoliStatManager);
        ReceiverMonitorFilter.setMonitor(napoliStatManager);

        // need to set when here so that queueCleanReceiver can handle
        // successfully.
        setMockStat();

        fetchCount = Integer.parseInt(System.getProperty("prefetch", fetchCount + ""));

        connector.setPoolSize(5);
        connector.setInterval(1000);

        connector.init();

        sender = new DefaultAsyncSender();
        sender.setConnector(connector);
        sender.setName(queueName);
        sender.setStoreEnable(false);
        sender.init();

        AbstractTestBase.getQueueConsumerSessionCount();
    }

    @Before
    public void setUp() throws Exception {
        JmxUtil.deleteAllMessage(NapoliTestUtil.getAddress(), queueName);

        JmxUtil.waitTillConsumerSessionCountZero(connector.getAddress(), queueName);

        redeliveryCallBackCalled = 0;
        setMockStat();
    }

    public static void setMockStat() {
        // since NapoliStatManager is NapoliClient level, set getSenderStat to
        // avoid null point exception.
        NapoliSenderStat napoliSenderStat = mock(NapoliSenderStat.class);
        when(napoliStatManager.getSenderStat(anyString(), anyString())).thenReturn(napoliSenderStat);
        napoliReceiverStat = null;

        napoliReceiverStat = mock(NapoliReceiverStat.class);
        when(napoliStatManager.getReceiverStat(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(napoliReceiverStat);

        NapoliReceiverStat test = napoliStatManager.getReceiverStat("", "", "", "", "");

        assertEquals("getREceiverStat doesn't work", napoliReceiverStat, test);

        logger.info("reset napoliReceiverStat mock");
        verify(napoliReceiverStat, times(0)).rereceiveFailure(anyLong());

    }

    @After
    public void stop() throws NapoliClientException {
        logger.info("Yanny test finished");
        // delete receiver persistent to avoid test impact each other.       
        KVStore store = ConsoleConnector.getorCreateBdbKvStore(connector.getStorePath(), queueName,
                NapoliConstant.CLIENT_TYPE_RECEIVER);

        if (store != null) {
            store.clear();

            assertEquals("clear should delete all local files, but " + store.getStoreSize() + " messages remain", 0,
                    store.getStoreSize());

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (receiver != null && !receiver.isClosed()) {
            receiver.close();
        }

        super.stop();
    }  
    
    @Test
    public void testConsumeWithWorkFailureAndStoreEnable() throws Exception {
        logger.info("yanny: test testConsumeWithWorkFailureAndStoreEnablestarted");

        receiver = new DefaultAsyncReceiver();

        long queueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);

        JmxUtil.deleteAllMessage(connector.getAddress(), queueName);

        long beginQueueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);
        logger.info("queue " + queueName + " has " + queueSize + " messages after delete queueSize is "
                + beginQueueSize);

        receiver.setConnector(connector);
        receiver.setName(queueName);
        receiver.setInstances(1);

        receiver.setStoreEnable(true);
        receiver.setReprocessInterval(1000);
        WorkerFailure worker = new WorkerFailure();
        worker.logEnabled = true;
        receiver.setWorker(worker);

        receiver.init();

        receiver.start();

        //  NapoliTestUtil.setExpectedSessionCount(receiver);

        int i = 0;
        int count = 5;
        int successCount = 0;
        while (i < count || successCount == 0) {
            i++;
            String message = new Date().toString() + i;
            logger.info("yanny start send message:" + message);

            Boolean result = sender.send(message);
            if (result) {
                successCount++;
            }
        }

        logger.info("send " + i + " succeed " + successCount);
        JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

        NapoliTestUtil.sleepSecond(1);
        assertTrue("WorkerFailure.doWork(Message)(called" + worker.getAccessNum()
                + " should be called more than totalSendSuccess*2 (" + 2 * successCount,
                worker.getAccessNum() >= 2 * successCount);

        // when store enable, if the message is saved locally success, send
        // is considered as success, but receiveFailure will be increased.
        verify(napoliReceiverStat, times(worker.getMessageCount())).receiveFailure(anyLong());

        // the first consume fail is added to receiveFailure.
        verify(napoliReceiverStat, times(worker.getAccessNum() - worker.getMessageCount())).rereceiveFailure(anyLong());

        // verify no message are left in the queue

        long endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);
        assertTrue((endQueueSize - beginQueueSize)
                + " messages stuck in the queue, should all be consumed and saved locally",
                endQueueSize <= beginQueueSize);

    }

    @Test
    public void testConsumeWithWorkFailureAndStoreDisabled() throws Exception {

        logger.info("yanny: test testConsumeWithWorkFailureAndStoreDisabled started");

        logger.info("============================================================================");
        long queueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);

        JmxUtil.deleteAllMessage(connector.getAddress(), queueName);

        long beginQueueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);
        logger.info("queue " + queueName + " has " + queueSize + " messages after delete queueSize is "
                + beginQueueSize);
        receiver = new DefaultAsyncReceiver();

        receiver.setConnector(connector);
        receiver.setName(queueName);
        receiver.setInstances(1);

        receiver.setStoreEnable(false);
        receiver.setReprocessInterval(1000);
        WorkerFailure worker = new WorkerFailure();
        receiver.setWorker(worker);

        receiver.init();

        receiver.start();

        NapoliTestUtil.setExpectedSessionCount(receiver);

        int i = 0;
        int count = 5;
        int successCount = 0;
        while (i < count || successCount == 0) {
            String message = new Date().toString() + i;
            i++;
            logger.info("yanny start send message:" + message);

            Boolean result = sender.send(message);
            if (result) {
                successCount++;
            }
        }

        JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize, 13000);

        // redelivery频率不能控制，

        assertTrue("WorkerFailure.doWork(Message)(called" + worker.getAccessNum()
                + " should be called more than totalSendSuccess*1 (" + 1 * successCount,
                worker.getAccessNum() >= 1 * successCount);

        // when store enable, if the message is saved locally success, send is considered as success

        verify(napoliReceiverStat, times(worker.getAccessNum())).receiveFalse(anyLong());
        verify(napoliReceiverStat, times(0)).rereceiveFailure(anyLong());

        // 消费失败，不存本地，没有redelivery callback,消息会一直留在队列，重复调用

        long endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);
        assertTrue(" messages should stuck in the queue, begin " + beginQueueSize + ", end is " + endQueueSize,
                endQueueSize > beginQueueSize);

    }

    @Test
    public void testConsumeWithWorkRetryOnceSuccessAndStoreEnable() throws Exception {
        logger.info("yanny: test testConsumeWithWorkRetryOnceSuccessAndStoreEnable started");

        receiver = new DefaultAsyncReceiver();

        int retryCount = 1;
        long queueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);

        JmxUtil.deleteAllMessage(connector.getAddress(), queueName);

        long beginQueueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);
        logger.info("queue " + queueName + " has " + queueSize + " messages after delete queueSize is "
                + beginQueueSize);
        receiver.setConnector(connector);
        receiver.setName(queueName);
        receiver.setInstances(1);

        receiver.setStoreEnable(true);
        receiver.setReprocessInterval(1000);
        receiver.setDataBatchReadCount(500);

        WorkerRetrySuccess worker = new WorkerRetrySuccess(retryCount);

        worker.logEnabled = true;
        receiver.setWorker(worker);

        receiver.init();

        receiver.start();

        NapoliTestUtil.setExpectedSessionCount(receiver);

        int i = 0;
        int count = 5;
        int successCount = 0;
        while (i < count || successCount == 0) {
            String message = new Date().toString() + i;

            i++;
            logger.info("yanny start send message:" + message);

            Boolean result = sender.send(message);
            if (result) {
                successCount++;
            }
        }
        logger.info("send total " + i + "return success " + successCount);

        JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

        NapoliTestUtil.waitUntilLocalStoreEmpty(connector, queueName, NapoliConstant.CLIENT_TYPE_RECEIVER);

        logger.info("local messages all consumed");

        // since result is false but actual send may be succeed.
        /*
         * assertTrue("WorkerRetrySuccess.doWork(Message)(called" +
         * worker.getAccessNum() +
         * " should be called >= totalSendSuccess*2 times(" + 2 * successCount,
         * worker.getAccessNum() >= 2 * successCount);
         */

        long messageCount = i + beginQueueSize;

        //initial delete may failure and the message then get consumed.
        assertTrue("WorkerRetrySuccess.doWork(Message)(called" + worker.getAccessNum()
                + " should be called <= totalSend *" + (retryCount + 1) + " times(" + (retryCount + 1) * messageCount,
                worker.getAccessNum() <= (retryCount + 1) * messageCount);

        // when store enabled, if saved local succesfully, treat it as
        // receiveSuccess, since result is false but actual send may be
        // succeed.
        verify(napoliReceiverStat, times(0)).receiveSuccess(anyLong(), anyLong());

        verify(napoliReceiverStat, atLeast(successCount * (retryCount - 1))).rereceiveFailure(anyLong());
        verify(napoliReceiverStat, atMost((int) messageCount * retryCount)).rereceiveFailure(anyLong());

        verify(napoliReceiverStat, atLeast(successCount)).rereceiveSuccess(anyLong(), anyLong());
        verify(napoliReceiverStat, atMost((int) messageCount)).rereceiveSuccess(anyLong(), anyLong());

        // verify no message are left in the queue
        long endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);
        assertTrue((endQueueSize - beginQueueSize) + " messages stuck in the queue, should all be removed to DLQ",
                endQueueSize <= beginQueueSize);

    }

    @Test
    public void testConsumeWithWorkRetrySuccessAndStoreEnable() throws Exception {

        logger.info("yanny: test testConsumeWithWorkRetrySuccessAndStoreEnable started");

        receiver = new DefaultAsyncReceiver();

        int retryCount = 3;
        long queueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);

        JmxUtil.deleteAllMessage(connector.getAddress(), queueName);

        long beginQueueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);
        logger.info("queue " + queueName + " has " + queueSize + " messages after delete queueSize is "
                + beginQueueSize);
        receiver.setConnector(connector);
        receiver.setName(queueName);
        receiver.setInstances(1);

        receiver.setStoreEnable(true);
        receiver.setReprocessInterval(1000);
        receiver.setDataBatchReadCount(100);

        ReprocessSchedule.setSecondDataCountBase(10);

        WorkerRetrySuccess worker = new WorkerRetrySuccess(retryCount, 5);

        receiver.setWorker(worker);

        receiver.init();

        receiver.start();

        NapoliTestUtil.setExpectedSessionCount(receiver);

        int i = 0;
        int count = 5;
        int successCount = 0;
        while (i < count || successCount == 0) {
            String message = new Date().toString() + i;
            i++;

            Boolean result = sender.send(message);
            if (result) {
                successCount++;
            }
        }

        logger.info("send total " + i + "return success " + successCount);
        JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

        NapoliTestUtil.waitUntilLocalStoreEmpty(connector, queueName, NapoliConstant.CLIENT_TYPE_RECEIVER);

        // since result is false but actual send may be succeed.
        /*
         * assertTrue("WorkerRetrySuccess.doWork(Message)(called" +
         * worker.getAccessNum() +
         * " should be called >= totalSendSuccess*2 times(" + 2 * successCount,
         * worker.getAccessNum() >= 2 * successCount);
         */
        assertTrue("WorkerRetrySuccess.doWork(Message)(called" + worker.getAccessNum()
                + " should be called <= totalSend *" + (retryCount + 1) + " times(" + (retryCount + 1) * i,
                worker.getAccessNum() <= (retryCount + 1) * i);

        verify(napoliReceiverStat, times(0)).receiveSuccess(anyLong(), anyLong());

        verify(napoliReceiverStat, atLeast(successCount * (retryCount - 1))).rereceiveFailure(anyLong());
        verify(napoliReceiverStat, atMost(i * (retryCount - 1))).rereceiveFailure(anyLong());

        verify(napoliReceiverStat, atLeast(successCount)).rereceiveSuccess(anyLong(), anyLong());
        verify(napoliReceiverStat, atMost(i)).rereceiveSuccess(anyLong(), anyLong());

        // verify no message are left in the queue

        long endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);
        assertTrue((endQueueSize - beginQueueSize) + " messages stuck in the queue, should all be removed to DLQ",
                endQueueSize == beginQueueSize);

    }

    @Test
    public void testMessageRedeliveryCallBack() throws Exception {
        logger.info("yanny: test testMessageRedeliveryCallBack started");

        receiver = new DefaultAsyncReceiver();
        long queueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);

        JmxUtil.deleteAllMessage(connector.getAddress(), queueName);

        long beginQueueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);
        logger.info("queue " + queueName + " has " + queueSize + " messages after delete queueSize is "
                + beginQueueSize);
        receiver.setConnector(connector);
        receiver.setName(queueName);
        receiver.setInstances(1);

        receiver.setStoreEnable(false);
        WorkerFailure worker = new WorkerFailure();
        worker.logEnabled = true;
        receiver.setWorker(worker);

        receiver.setRedeliveryCallback(new RedeliveryCallback() {

            public void redeliveryFailed(Serializable msg, Throwable t) {
                logger.info("yanny a redeliveryCallback found." + t);
                redeliveryCallBackCalled++;

            }
        });

        receiver.setReprocessInterval(1000);
        receiver.init();

        receiver.start();

        NapoliTestUtil.setExpectedSessionCount(receiver);

        int i = 0;
        int count = 4;
        int successCount = 0;
        while (i < count || successCount == 0) {
            String message = new Date().toString() + i;
            i++;
            logger.info("yanny start send message:" + message);

            Boolean result = sender.send(message);
            if (result) {
                successCount++;
            }
        }

        JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

        assertTrue("redeliveryCallBackCalled is called " + redeliveryCallBackCalled
                + " times should be at least sendSuccess count" + successCount,
                redeliveryCallBackCalled >= successCount);

        assertTrue("redeliveryCallBackCalled is called " + redeliveryCallBackCalled
                + " times should be at most total send request count" + i, redeliveryCallBackCalled <= i);

        assertEquals("WorkerFailure.doWork(Message) is called " + worker.getAccessNum() + " times!=totalRequest*"
                + (receiver.getRedeliveryStrategy().getMaxRedeliveries() + 1), (receiver.getRedeliveryStrategy()
                .getMaxRedeliveries() + 1) * successCount, worker.getAccessNum());

        // when redeliveryCallback is set, store enable is disabled.
        verify(napoliReceiverStat, times(worker.getAccessNum())).receiveFalse(anyLong());
        verify(napoliReceiverStat, times(0)).rereceiveFailure(anyLong());

        // verify no message are left in the queue
        long endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);
        assertTrue((endQueueSize - beginQueueSize) + " messages stuck in the queue, should all be removed to DLQ",
                endQueueSize == beginQueueSize);

        // TODO: should also verify the messages are in DLQ queues.
    }

    @Test
    public void testMessageReceiver() throws Exception {
        logger.info("yanny: test testMessageReceiver started");
        receiver = new DefaultAsyncReceiver();

        int sumCount = 0;
        long queueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);

        JmxUtil.deleteAllMessage(connector.getAddress(), queueName);

        long beginQueueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);
        logger.info("queue " + queueName + " has " + queueSize + " messages after delete queueSize is "
                + beginQueueSize);
     
        receiver.setConnector(connector);
        receiver.setName(queueName);
        receiver.setInstances(5);

        receiver.setStoreEnable(true);
        MyWorker worker = new MyWorker();
        receiver.setWorker(worker);

        receiver.init();
        receiver.start();

        NapoliTestUtil.setExpectedSessionCount(receiver);

        int i = 0;
        int count = 5;
        while (i < count || sumCount == 0) {
            String message = new Date().toString() + i;
            i++;
            logger.info("yanny start send message:" + message);

            boolean result = sender.send(message);

            if (result) {
                sumCount++;
            }
        }

        JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

        assertTrue("Worker.doWork(Message) (" + worker.getAccessNum()
                + ") times should not be called more than totalRequest*" + i, worker.getAccessNum() <= i);

        assertTrue("Worker.doWork(Message) (" + worker.getAccessNum()
                + ") times should be called no less than send success count*" + sumCount,
                worker.getAccessNum() >= sumCount);

        verify(napoliReceiverStat, times(worker.getAccessNum())).receiveSuccess(anyLong(), anyLong());
        verify(napoliReceiverStat, times(0)).rereceiveFailure(anyLong());

        // verify no message are left in the queue
        long endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);
        assertTrue(queueSize + " messages stuck in the queue, should all be consumed", endQueueSize <= 0);

        // TODO: should also verify the messages are in DLQ queues.

    }   

    @Test
    public void testMQServerRestartCanConsume() throws Exception {
        logger.info("yanny: test testMQServerRestartCanConsume started");
        receiver = new DefaultAsyncReceiver();

        int sumCount = 0;
        long queueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);

        JmxUtil.deleteAllMessage(connector.getAddress(), queueName);

        long beginQueueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);
        logger.info("queue " + queueName + " has " + queueSize + " messages after delete queueSize is "
                + beginQueueSize);
     
        receiver.setConnector(connector);
        receiver.setName(queueName);
        receiver.setInstances(5);

        receiver.setStoreEnable(true);
        MyWorker worker = new MyWorker(1.4);
        worker.logEnabled = true;
        receiver.setWorker(worker);

        receiver.setPeriod(NapoliConstant.MIN_HEARTBEAT_PERIOD);

        receiver.init();
        receiver.start();

        ClientQueue dest = (ClientQueue)connector.getConfigService().fetchDestination(queueName);
        
        
        
        //int machineCount = jmxAddresses.length;
        int instance1 = receiver.getInstances();
        int consumerCount1 = receiver.getDestinationContext().getConsumerMachineMap().size();

        logger.info("yanny: consumer instance is " + instance1 + " consumerCount is " + consumerCount1);
        
        int i = 0;
        int count = 50;
        while (i < count || sumCount == 0) {
            String message = new Date().toString() + i;
            i++;
            logger.info("yanny start send message:" + message);

            boolean result = sender.send(message);

            if (result) {
                sumCount++;
            }
        }

        //my worker sleep 1.4 seconds, not all messages are consumed.
        NapoliTestUtil.sleepSecond(3);
        
        long endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);
        
        logger.info("after 3 seconds, still have " + endQueueSize + " messages in queue, kill all MQ servers");
        
        for(ClientMachine machine : dest.getMachineSet()){
            JmxUtil.shutdownMachine(JmxUtil.getJmxAddress(machine), machine.getMachineType());
        }
        
        NapoliTestUtil.sleepSecond(3);
        
        logger.info("after 3 seconds, restart MQ servers");
        for(ClientMachine machine : dest.getMachineSet()){
            JmxUtil.startMachine(JmxUtil.getJmxAddress(machine), machine.getMachineType());
        }
        
        logger.info("restart MQ servers finished.");
        
        JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

     
        //due to connection restart, could have multi consume because the acknowledge is not sent or handle succesfully.
        //assertTrue("Worker.doWork(Message) (" + worker.getAccessNum() + ") times should not be called more than totalRequest*" + i, worker.getAccessNum() <= i);

        assertTrue("Worker.doWork(Message) (" + worker.getAccessNum()
                + ") times should be called no less than send success count*" + sumCount,
                worker.getAccessNum() >= sumCount);

        // verify no message are left in the queue
        endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);
        assertTrue(queueSize + " messages stuck in the queue, should all be consumed", endQueueSize <= 0);
        int instance2 = receiver.getInstances();
        int consumerCount2 = receiver.getDestinationContext().getConsumerMachineMap().size();

        logger.info("after restart yanny: consumer instance is " + instance2 + " consumerCount is " + consumerCount2);
     
        assertTrue("after restart, instance and consumerCount should equal but instance1(" + instance1 + ")!=instance2(" + instance2 + ") or consumerCount1(" + consumerCount1 + ")!=consumerCount2(" + consumerCount2+")", instance1==instance2 && consumerCount1==consumerCount2);
    }
    
    @Test
    public void testAsyncExReceiver() {
        logger.info("yanny: test testAsyncExReceiver started");
        receiver = new DefaultAsyncReceiver();
        long queueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);

        JmxUtil.deleteAllMessage(connector.getAddress(), queueName);

        long beginQueueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);
        logger.info("queue " + queueName + " has " + queueSize + " messages after delete queueSize is "
                + beginQueueSize);
        long beginEnQueueSize = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

        int sumCount = 0;
        try {
            receiver.setConnector(connector);
            receiver.setName(queueName);
            receiver.setInstances(1);

            receiver.setStoreEnable(true);
            MyWorkerEX worker = new MyWorkerEX();
            receiver.setExWorker(worker);

            receiver.init();
            receiver.start();

            NapoliTestUtil.setExpectedSessionCount(receiver);

            int i = 0;
            int count = 5;
            while (i < count || sumCount == 0) {
                String message = new Date().toString() + i;
                i++;
                logger.info("yanny start send message:" + message);

                boolean result = sender.send(message);

                if (result) {
                    sumCount++;
                }
            }

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

            long endEnQueueSize = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            assertEquals("all received message should be consumed once, send success "
                    + (endEnQueueSize - beginEnQueueSize) + ";consumed " + worker.getAccessNum(),
                    (int) (endEnQueueSize - beginEnQueueSize), worker.getAccessNum());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testReceiverMultiSetWorker() {
        logger.info("yanny: test testReceiverMultiSetWorker started");
        receiver = new DefaultAsyncReceiver();

        receiver.setConnector(connector);
        receiver.setName(queueName);
        receiver.setInstances(1);

        receiver.setStoreEnable(true);
        MyWorkerEX worker = new MyWorkerEX();
        receiver.setExWorker(worker);

        try {
            receiver.setWorker(new MyWorker());
            fail("set worker twice should throw exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("should throw IllegalArgumentException exception:setWorker method only support call once.",
                    "setWorker method only support call once.", ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail("throw unexpected exception " + e.getMessage());
        }
    }

    @Test
    public void testReceiverHeartbeat() {
        logger.info("yanny: test testReceiverHeartbeat started");

        NapoliConnector localConnector = new NapoliConnector();
        localConnector.setAddress(NapoliTestUtil.getAddress());
        localConnector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath") + "\\" + new Date().hashCode());
        localConnector.setPoolSize(5);

        localConnector.setIdlePeriod(1000);
        localConnector.init();

        receiver = new DefaultAsyncReceiver();

        receiver.setConnector(localConnector);
        receiver.setName(queueName);
        receiver.setInstances(5);

        receiver.setPeriod(NapoliConstant.MIN_HEARTBEAT_PERIOD);
        receiver.setStoreEnable(true);
        MyWorkerEX worker = new MyWorkerEX();
        receiver.setExWorker(worker);

        try {
            receiver.init();
            receiver.start();

            NapoliTestUtil.setExpectedSessionCount(receiver);
            Thread.sleep(NapoliConstant.MIN_HEARTBEAT_PERIOD + 100);

            int expectedInstance = receiver.getExpectedInstances();

            int count = 0;
            int beginInstance = 0;

            do {
                NapoliTestUtil.sleep(100);
                beginInstance = receiver.getInstances();
                count++;
            } while (count < 5 && beginInstance < expectedInstance);

            if (beginInstance < expectedInstance) {
                logger.error("receiver expected to have " + expectedInstance + " sessions, but only get "
                        + beginInstance);
                fail("receiver expected to have " + expectedInstance + " sessions, but only get " + beginInstance);
            }

            Thread.sleep(NapoliConstant.MIN_HEARTBEAT_PERIOD + 100);

            int endInstance = receiver.getInstances();

            logger.info("instance number, init is " + beginInstance + ";end instance is " + endInstance);

            assertEquals("instance number should not changed after heartbeat even no consume happened, init is "
                    + beginInstance + ";end instance is " + endInstance, beginInstance, endInstance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("throw unexpected exception " + e.getMessage());
        }
    }

    @Test
    public void testStopReceiver() {

        logger.info("yanny: test testStopReceiver started");
        receiver = new DefaultAsyncReceiver();
        receiver.setConnector(connector);
        receiver.setName(queueName);

        receiver.setStoreEnable(true);
        MyWorker normalWorker = new MyWorker();
        normalWorker.workerName = "normal worker";
        normalWorker.logEnabled = true;
        receiver.setPeriod(1000);
        AbstractTestBase.expectedConsumerSession = 10;

        try {
            receiver.setWorker(normalWorker);
            receiver.init();
        } catch (Exception e) {
        }

        ThreadMXBean mBean = ManagementFactory.getThreadMXBean();

        int originalCount = mBean.getThreadCount();

        //List<JMXConnector> connectors = JmxUtil.getJMXConnectors(connector.getAddress(), queueName);

        try {
            receiver.start();
            logger.info(" call stop, session size is "
                    + JmxUtil.getConsumerSessionCount(connector.getAddress(),  queueName));
            NapoliTestUtil.sleep(2);
            receiver.stop();
            logger.info("yanny stop finished");

        } catch (NapoliClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        NapoliTestUtil.sleepSecond(3);

        int endCount = mBean.getThreadCount();

        String errorMessage = "";
        if (endCount > originalCount * 2) {
            errorMessage = "start and stop receiver should not increase thread too much, increased more than 100%, begin "
                    + originalCount + " end is:" + endCount;
        }

        int sessionCount = JmxUtil.getConsumerSessionCount(connector.getAddress(),  queueName);

        
        if (sessionCount != 0) {
            errorMessage += ";after consumer stop, the consumer session should all be closed, but has " + sessionCount
                    + " left";
        }

        if (!errorMessage.equals("")) {
            logger.error(errorMessage);
            fail(errorMessage);
        }
    }

    @Test
    //test for bug NP-268
    public void testMessageFilter() throws Exception {
        logger.info("yanny: test testMessageFilter started");

        long queueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);

        JmxUtil.deleteAllMessage(connector.getAddress(), queueName);

        long beginQueueSize = JmxUtil.getQueueSize(connector.getAddress(), queueName);
        long beginEnQueueSize = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);
        logger.info("queue " + queueName + " has " + queueSize + " messages after delete queueSize is "
                + beginQueueSize + " at enQueueSize " + beginEnQueueSize);

        receiver = new DefaultAsyncReceiver();

        int greenSumCount = 0;

        receiver.setConnector(connector);
        receiver.setName(queueName);
        receiver.setInstances(5);

        receiver.setStoreEnable(true);
        MyWorker worker = new MyWorker();
        worker.logEnabled = true;
        receiver.setWorker(worker);

        ArrayList<Filter> filterList = new ArrayList<Filter>();

        filterList.add(new FilterExample("red"));
        receiver.setFilterList(filterList);
        receiver.init();
        receiver.start();

        int i = 0;
        int count = 10;
        while (i < count || greenSumCount == 0) {
            String message = new Date().toString() + i + "green";
            i++;
            logger.info("yanny start send message:" + message);

            boolean result = sender.send(message);

            if (result) {
                greenSumCount++;
            }
        }

        int redSumCount = 0;
        while (i < 2 * count || redSumCount == 0) {
            String message = new Date().toString() + i + "red";
            i++;
            logger.info("yanny start send message:" + message);

            boolean result = sender.send(message);

            if (result) {
                redSumCount++;
            }
        }
    
        JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

        // verify no message are left in the queue
        long endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);

        logger.info("yanny after consume, " + endQueueSize + " left in queue, consumer worker get "
                + worker.getAccessNum());

        assertTrue(queueSize + " messages stuck in the queue, should all be consumed", endQueueSize <= 0);

        assertTrue("qWorker should only consume messages that contain 'red', no more than half messages(" + count
                + "), but get " + worker.getAccessNum(), worker.getAccessNum() <= count);
        
        assertTrue("qWorker should consume all messages that contain 'red', no less than red messages (" + redSumCount
                + "), but get " + worker.getAccessNum(), worker.getAccessNum() >= redSumCount);
    }

    class MyWorkerEX implements AsyncWorkerEx {

        private AtomicInteger accessNum = new AtomicInteger();

        public boolean doWork(NapoliMessage message) {
            System.out.println("AsyncWorkerEx process message " + message.getContent());

            accessNum.incrementAndGet();
            return true;

        }

        public int getAccessNum() {
            return accessNum.get();
        }

        public void resetAccessNum() {
            accessNum.lazySet(0);
        }

        public void reset() {
            this.resetAccessNum();
        }
    }

    class FilterExample implements Filter {

        String filter;

        FilterExample(String filter) {
            this.filter = filter;
        }

        public void destroy() {

        }

        public void filter(Context context, FilterFinder next) {
            Object obj = context.getOutputObject();
            if (obj instanceof String) {
                if (((String) obj).contains(this.filter)) {
                    Filter nextFilter = next.nextFilter(this);
                    if (nextFilter != null) {
                        nextFilter.filter(context, next);
                    }
                } else {
                    context.setOutputObject(null);
                }
            } else {
                context.setOutputObject(null);
            }
        }

        public void init() {

        }
    }
}
