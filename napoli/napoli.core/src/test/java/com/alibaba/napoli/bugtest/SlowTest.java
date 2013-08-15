package com.alibaba.napoli.bugtest;

import com.alibaba.napoli.AbstractTestBase;
import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.PendingNotify;
import com.alibaba.napoli.client.async.RedeliveryCallback;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.model.MyWorker;
import com.alibaba.napoli.client.model.WorkerFailure;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.common.util.JmxUtil;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SlowTest extends AbstractTestBase {

    static int             redeliveryCallBackCalled = 0;
    private static boolean threadStop               = false;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        queueName = "";

        AbstractTestBase.setUpBeforeClass();
        connector.setConnectionCheckPeriod(5 * 60 * 1000);
        connector.init();
    }

    @Test
    public void testMessageReceiverConsumeHung() throws Exception {
        queueName = "NapoliAMQOnlyTest_" + System.currentTimeMillis();

        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName,
                NapoliTestUtil.getProperty("napoli.func.amqGroupName"));
        logger.info("yanny: test testMessageReceiverConsumeHung started with queueName=" + queueName);

        long beginQueueSize = 0;

        DefaultAsyncReceiver receiver = new DefaultAsyncReceiver();

        int fetchCount = 2;

        DefaultAsyncReceiver delayConsumer = new DefaultAsyncReceiver();

        long queueSize = 0;

        DefaultAsyncSender sender = new DefaultAsyncSender();

        try {
            sender.setConnector(connector);
            sender.setName(queueName);
            sender.setStoreEnable(false);
            sender.init();

            receiver.setConnector(connector);
            receiver.setName(queueName);

            receiver.setStoreEnable(true);
            MyWorker normalWorker = new MyWorker();
            normalWorker.workerName = "normal worker";
            normalWorker.logEnabled = true;

            receiver.setWorker(normalWorker);

            receiver.init();
            receiver.start();

            NapoliConnector slowConnector = new NapoliConnector();
            slowConnector.setAddress(NapoliTestUtil.getAddress());
            slowConnector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath") + "\\"
                    + new Date().hashCode());
            slowConnector.setPoolSize(5);

            slowConnector.setPrefetch(fetchCount);

            slowConnector.init();

            delayConsumer.setConnector(slowConnector);
            delayConsumer.setName(queueName);

            delayConsumer.setStoreEnable(true);
            logger.info((new Date()).toString() + " yanny delayWorker sleep 3 seconds for each message");

            delayConsumer.setPeriod(1000 * 60 * 60);
            MyWorker delayWorker = new MyWorker(3);

            delayWorker.workerName = "delay worker";
            delayWorker.logEnabled = true;

            delayConsumer.setWorker(delayWorker);

            delayConsumer.init();
            delayConsumer.start();

            AbstractTestBase.expectedConsumerSession = receiver.getExpectedInstances()
                    + receiver.getExpectedInstances();

            int i = 0;

            int expectedCount = delayConsumer.getExpectedInstances() * delayConsumer.getConnectionParam().getPrefetch();

            while (i < expectedCount * 4) {

                i++;
                sender.send(i);

                Thread.sleep(1);
            }

            logger.info("yanny " + (new Date()).toString()
                    + " start to wait for all messages to be consumed, total send " + expectedCount * 4);
            long begin = System.currentTimeMillis();

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

            long end = System.currentTimeMillis();

            // verify no message are left in the queue

            logger.info((new Date()).toString() + " yanny normal worker consume:");
            NapoliTestUtil.printArray(normalWorker.getReceivedIds());

            logger.info((new Date()).toString() + " yanny delay worker consume:");
            NapoliTestUtil.printArray(delayWorker.getReceivedIds());

            logger.info((new Date()).toString() + " yanny: consume all messages take " + ((end - begin) / 1000)
                    + " seconds");

            logger.info((new Date()).toString() + " Yanny: finally normal consume " + normalWorker.getAccessNum()
                    + "; delayQueueConsume " + delayWorker.getAccessNum());

            long endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);
            assertTrue((endQueueSize - beginQueueSize) + " messages stuck in the queue, should all be removed to DLQ",
                    endQueueSize == beginQueueSize);

            assertEquals(
                    "delay consumer should only consume prefetch(" + expectedCount
                            + ") messages, all others should be consumed by quick consumer, but it get "
                            + delayWorker.getAccessNum() + " messages", expectedCount, delayWorker.getAccessNum());

        } finally {
            if (!delayConsumer.isClosed()) {
                logger.info("yanny close receiver");
                delayConsumer.close();
            }

            if (!receiver.isClosed()) {
                logger.info("yanny close receiver");
                receiver.close();
            }

            if (!sender.isClosed()) {
                logger.info("yanny close sender");
                sender.close();
            }
        }
    }

    /*
     * when redeliverycall back is setup, store enable = true is disabled,
     * should have same result as testMessageRedeliveryCallBack
     */
    @Test
    public void testMessageRedeliveryCallBackWithStoreEnableAndRedeliveryExponential() throws Exception {

        queueName = "NapoliAMQOnlyTest_" + System.currentTimeMillis();

        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName,
                NapoliTestUtil.getProperty("napoli.func.amqGroupName"));
        logger.info("yanny: test testMessageRedeliveryCallBackWithStoreEnableAndRedeliveryExponential started with queueName="
                + queueName);

        long beginQueueSize = 0;

        DefaultAsyncSender sender =  DefaultAsyncSender.createSender(connector,queueName,false);
        sender.init();
        
        WorkerFailure worker = new WorkerFailure();
        worker.logEnabled = true;
        DefaultAsyncReceiver receiver = DefaultAsyncReceiver.createReceiver(connector,queueName,true,1,worker);
        try {
            receiver.setRedeliveryCallback(new RedeliveryCallback() {

                public void redeliveryFailed(Serializable msg, Throwable t) {
                    if (t!= null){
                        t.printStackTrace();
                    }
                    System.out.println("yanny a testMessageRedeliveryCallBackWithStoreEnable callback found." + t);
                    redeliveryCallBackCalled++;
                }
            });

            int redelivery = 7;
            int mutiplier = 1; //todo: mutiplier=2,test will be fail!
            receiver.setMaxRedeliveries(redelivery);
            receiver.setRedeliveryExponential(true);
            receiver.setRedeliveryMultiplier((short) mutiplier);
            receiver.setReprocessInterval(1000);
            receiver.start();
            
            NapoliTestUtil.setExpectedSessionCount(receiver);

            int i = 0;
            int count = 1;
            int successCount = 0;
            while (i < count) {
                String message = new Date().toString() + i;
                i++;
                logger.info("yanny start send message:" + message);
                if (sender.send(message)) {
                    successCount++;
                }
            }

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

            assertEquals(
                    "redeliveryCallBackCalled should be called since it has high priority than store eanble, but it's called "
                            + redeliveryCallBackCalled + " times", 1, redeliveryCallBackCalled);

            assertEquals("WorkerFailure.doWork(Message) is called " + worker.getAccessNum() + " times!=totalRequest*"
                    + (redelivery * mutiplier + 1), (redelivery * mutiplier + 1) * successCount, worker.getAccessNum());

            // when redeliveryCallback is set, store enable is disabled.

            // verify no message are left in the queue
            long endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);
            assertTrue((endQueueSize - beginQueueSize) + " messages stuck in the queue, should all be removed to DLQ",
                    endQueueSize == beginQueueSize);
        } finally {

            if (sender != null && !sender.isClosed()) {
                sender.close();
            }
            if (receiver != null && !receiver.isClosed()) {
                receiver.close();
            }

            HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName);
        }
    }

    static AtomicLong bizCallOrder = new AtomicLong(0);

    @Test
    public void testSenderSessionControl() {

        queueName = "BizSenderTest_" + System.currentTimeMillis();
        String queueName2 = "NapoliSenderSessionControlTest_" + System.currentTimeMillis();

        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName,
                NapoliTestUtil.getProperty("napoli.func.amqGroupName"));
        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName2,
                NapoliTestUtil.getProperty("napoli.func.amqGroupName"));

        logger.info("yanny: test testSenderSessionControl started with queueName=" + queueName + ", queueName2="
                + queueName2);

        final DefaultAsyncSender sender1 = new DefaultAsyncSender();
        final DefaultAsyncSender sender2 = new DefaultAsyncSender();
        int poolSize = connector.getPoolSize();
        logger.info("yanny connector pool size is " + poolSize);

        try {
            sender1.setConnector(connector);

            sender1.setPendingInterval(1000 * 10);
            logger.info("yanny sender before pendingNotify");
            sender1.setPendingNotifier(new PendingNotify() {

                public PendingNotifyStateEnum notify(NapoliMessage message) {
                    logger.info(new Date().toString() + " yanny: call pendingNotifiy for" + message);

                    try {
                        throw new Exception("yanny PendingNotifyStateEnum stacktrace");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return PendingNotifyStateEnum.COMMIT;
                }

            });

            sender1.setName(queueName);
            sender1.init();

            sender2.setConnector(connector);

            sender2.setPendingInterval(1000 * 10);
            logger.info("yanny sender before pendingNotify");
            sender2.setPendingNotifier(new PendingNotify() {

                public PendingNotifyStateEnum notify(NapoliMessage message) {
                    logger.info(new Date().toString() + " yanny: call pendingNotifiy for" + message);

                    try {
                        throw new Exception("yanny PendingNotifyStateEnum stacktrace");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return PendingNotifyStateEnum.COMMIT;
                }

            });

            sender2.setName(queueName2);
            sender2.init();

            logger.info("yanny connector pool size is " + poolSize);

            long startCount1 = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);
            long startCount2 = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName2);

            threadStop = false;

            bizCallOrder = new AtomicLong(0);

            final Runnable bizCall = new Runnable() {

                public void run() {

                    logger.info(new Date().toString() + " yanny callback succeed");
                    try {
                        Thread.sleep(490);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };
            final AtomicInteger requestCount = new AtomicInteger();

            int tc = 20;
            final Random rd = new Random(10);
            final AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 0; i < tc; i++) {
                Thread t = new Thread("sender1-thread--" + i) {
                    public void run() {
                        while (!threadStop) {
                            try {//      logger.info("hello");
                                int id = requestCount.incrementAndGet();

                                if (!sender1.isClosed()) {
                                    sender1.send("test1_sender1_" + id, bizCall);
                                    successCount.incrementAndGet();
                                    logger.info("test1 sender1 send message " + id);
                                }
                            } catch (Throwable t) {
                                //System.out.println("can't get session");
                                //t.printStackTrace();
                            }
                            NapoliTestUtil.sleep(rd.nextInt(10));
                        }

                    }
                };
                t.start();

                Thread t2 = new Thread("sender2-thread--" + i) {
                    public void run() {
                        while (!threadStop) {
                            try {
                                int id = requestCount.incrementAndGet();
                                if (!sender2.isClosed()) {
                                    sender2.send("test1_Sender2_" + id, bizCall);
                                    successCount.incrementAndGet();
                                    logger.info("test1 sender2 send message " + id);
                                }
                            } catch (Throwable t) {
                                //System.out.println("can't get session");
                                //t.printStackTrace();
                            }
                            NapoliTestUtil.sleep(rd.nextInt(10));
                        }
                    }
                };
                t2.start();
            }

            //sleep to make sure send happens so that all connections are created. 10 round of bizCall time.
            int times = 20;
            Thread.sleep(500 * times);
            threadStop = true;
            long endCount1 = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            long endCount2 = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName2);

            int totalMachineSize = 0;
            int retry = 0;
            do {
                Thread.sleep(100);
                totalMachineSize = connector.getSenderConnectionSize();
                retry++;
            } while (totalMachineSize == 0 && retry < 50);

            logger.info("connector.getSenderConnectionSize()=" + totalMachineSize);

            //give 1.1 rate because after threadStop, it may still takes some times to quit.
            int expectedMaxCount = (int) (poolSize * times * totalMachineSize * 1.1);

            int expectedCount = Math.min(expectedMaxCount, requestCount.get());

            int size1 = sender1.getDestinationContext().getSendQueueMachineList(sender1.getName()).size();
            int size2 = sender2.getDestinationContext().getSendQueueMachineList(sender2.getName()).size();

            logger.info("yanny size1 is " + size1 + " size2 " + size2 + " connector sender connection size "
                    + totalMachineSize);

            if ((size1 + size2) <= totalMachineSize) {
                //two senders (two queues) have no duplicate machines.
                fail("two queues should be on at least one same machine for this test, but it failed, please check. ");
            }

            int expectedCount1 = (int) (expectedCount * 0.2 * size1 / totalMachineSize);
            int expectedCount2 = (int) (expectedCount * 0.2 * size2 / totalMachineSize);

            logger.info(tc + " thread each for sender1 and sender2, wait for 10 times of Bizcalltime");
            logger.info("queue1 is on " + size1 + " machines, start1=" + startCount1 + ",end1=" + endCount1 + "(get "
                    + (endCount1 - startCount1) + "); ");
            logger.info("queue2 is on " + size2 + " machines, start2=" + startCount2 + ",end2=" + endCount2 + "(get "
                    + (endCount2 - startCount2) + ")");
            logger.info("total try send " + requestCount.get() + " total send succeed is "
                    + ((endCount1 - startCount1) + (endCount2 - startCount2)) + " expectedMaxcount is "
                    + expectedMaxCount);

            String errorMessage = "";

            /*if (((endCount1 - startCount1) + (endCount2 - startCount2)) > expectedMaxCount) {
                errorMessage += "; with connector pool size " + connector.getPoolSize() + " and wait for " + times
                        + " times of Bizcalltime should get no more than " + expectedMaxCount + " but get "
                        + ((endCount1 - startCount1) + (endCount2 - startCount2));
            }*/

            if (((endCount1 - startCount1) + (endCount2 - startCount2)) < (int) (expectedCount * 0.6)) {
                errorMessage += "; with connector pool size " + connector.getPoolSize() + " and wait for " + times
                        + " times of Bizcalltime should get at least 60% of expected (" + (int) (expectedCount * 0.6)
                        + ") but get " + ((endCount1 - startCount1) + (endCount2 - startCount2));
            }
            long successSendCount = (endCount1 - startCount1) + (endCount2 - startCount2);
            /*
             * 20% chance seems not stable most of the time. when two senders
             * share one connector, with session control enabled, one could get
             * all the send messages and the other get none. should use two
             * connectors instead of one. if ((endCount1 - startCount1) <
             * expectedCount1) { errorMessage +=
             * "; sender1 get less than 20% chance to send, sender1 send " +
             * (endCount1 - startCount1) + " expect at least " + expectedCount1;
             * } if ((endCount2 - startCount2) < expectedCount2) { errorMessage
             * += "; sender2 get less than 20% chance to send, sender2 send " +
             * (endCount2 - startCount2) + " expect at least " + expectedCount2;
             * }
             */
            assertTrue(errorMessage, NapoliTestUtil.isBlank(errorMessage));
            //assertEquals(10, successSendCount);
        } catch (Exception e) {
            logger.info("yanny exception happened");
            e.printStackTrace();

            Assert.fail("yanny exception happened " + e.getMessage());
        } finally {

            if (sender1 != null && !sender1.isClosed()) {
                sender1.close();
            }
            if (sender2 != null && !sender2.isClosed()) {
                sender2.close();
            }

            HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName);
            HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName2);
        }
    }

    @Test
    //Bug NP-237
    public void testSenderSessionControlFalse() {
        queueName = "BizSenderTest_" + System.currentTimeMillis();

        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName,
                NapoliTestUtil.getProperty("napoli.func.amqGroupName"));

        logger.info("yanny test testSenderSessionControlFalse starts with queueName=" + queueName);

        final DefaultAsyncSender sender1 = new DefaultAsyncSender();
        final DefaultAsyncSender sender2 = new DefaultAsyncSender();

        connector.close();

        NapoliConnector secondConnector = new NapoliConnector();
        secondConnector.setAddress(NapoliTestUtil.getAddress());
        secondConnector
                .setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath") + "\\" + new Date().hashCode());
        secondConnector.setSendSessionControl(false);
        secondConnector.init();
        int poolSize = secondConnector.getPoolSize();
        logger.info("yanny connector pool size is " + poolSize);

        try {
            sender1.setConnector(secondConnector);

            sender1.setPendingInterval(1000 * 10);
            logger.info("yanny sender before pendingNotify");
            sender1.setPendingNotifier(new PendingNotify() {

                public PendingNotifyStateEnum notify(NapoliMessage message) {
                    logger.info(new Date().toString() + " yanny: call pendingNotifiy for" + message);

                    return PendingNotifyStateEnum.COMMIT;
                }

            });

            sender1.setName(queueName);
            sender1.init();

            sender2.setConnector(secondConnector);

            sender2.setPendingInterval(1000 * 10);
            logger.info("yanny sender before pendingNotify");
            sender2.setPendingNotifier(new PendingNotify() {

                public PendingNotifyStateEnum notify(NapoliMessage message) {
                    logger.info(new Date().toString() + " yanny: call pendingNotifiy for" + message);

                    return PendingNotifyStateEnum.COMMIT;
                }

            });

            sender2.setName("NapoliSenderSessionControlTest");
            sender2.init();

            logger.info("yanny connector pool size is " + poolSize);

            long startCount1 = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);
            long startCount2 = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), sender2.getName());

            final Runnable bizCall = new Runnable() {

                public void run() {

                    logger.info(new Date().toString() + " yanny callback succeed");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };
            final AtomicInteger requestCount = new AtomicInteger();

            threadStop = false;
            int tc = 20;
            for (int i = 0; i < tc; i++) {
                Thread t = new Thread("sender1-thread--" + i) {
                    public void run() {
                        try {
                            while (threadStop == false) {
                                int id = requestCount.incrementAndGet();
                                if (!sender1.isClosed()) {
                                    sender1.send("test2_sender1_" + id, bizCall);
                                    logger.info("test2 sender1 send message " + id);
                                }
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                };
                t.start();

                Thread t2 = new Thread("sender2-thread--" + i) {
                    public void run() {
                        try {
                            while (threadStop == false) {
                                int id = requestCount.incrementAndGet();
                                if (!sender2.isClosed()) {
                                    sender2.send("test2_Sender2_" + id, bizCall);
                                    logger.info("test2 sender2 send message " + id);
                                }
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                };
                t2.start();
            }

            //sleep to make sure send happens so that all connections are created. 10 round of bizCall time.
            Thread.sleep(500 * 10);

            long endCount1 = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            long endCount2 = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), sender2.getName());

            int expectedCount = Math.min(10 * tc * 2, requestCount.get());

            int expectedCount1 = (int) (expectedCount * 0.3);
            int expectedCount2 = (int) (expectedCount * 0.3);

            logger.info(tc + " thread each for sender1 and sender2, wait for 10 times of Bizcalltime");
            logger.info("queue1 is start1=" + startCount1 + ",end1=" + endCount1 + "(get " + (endCount1 - startCount1)
                    + "); ");
            logger.info("queue2 is start2=" + startCount2 + ",end2=" + endCount2 + "(get " + (endCount2 - startCount2)
                    + ")");
            logger.info("total try send " + requestCount.get() + " total send succeed is "
                    + ((endCount1 - startCount1) + (endCount2 - startCount2)) + " expectedCount is " + expectedCount);

            String errorMessage = "";

            if (((endCount1 - startCount1) + (endCount2 - startCount2)) > expectedCount) {
                errorMessage += "; with " + tc
                        + " thread for each sender and wait for 10 times of Bizcalltime should get no more than "
                        + expectedCount + " but get " + ((endCount1 - startCount1) + (endCount2 - startCount2));
            }

            if (((endCount1 - startCount1) + (endCount2 - startCount2)) < (int) (expectedCount * 0.9)) {
                errorMessage += "; with "
                        + tc
                        + " thread for each sender and wait for 10 times of Bizcalltime should get at least 90% of expected ("
                        + (int) (expectedCount * 0.9) + ") but get "
                        + ((endCount1 - startCount1) + (endCount2 - startCount2));
            }

            if ((endCount1 - startCount1) < expectedCount1) {
                errorMessage += "; sender1 get less than 30% chance to send, sender1 send " + (endCount1 - startCount1)
                        + " expect at least " + expectedCount1;
            }

            if ((endCount2 - startCount2) < expectedCount2) {
                errorMessage += "; sender2 get less than 30% chance to send, sender2 send " + (endCount2 - startCount2)
                        + " expect at least " + expectedCount2;
            }

            assertTrue(errorMessage, NapoliTestUtil.isBlank(errorMessage));
        } catch (Exception e) {
            logger.info("yanny exception happened");
            e.printStackTrace();

            Assert.fail("yanny exception happened " + e.getMessage());
        } finally {
            sender1.close();
            sender2.close();
            secondConnector.close();

            HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName);
        }
    }

    @Test
    public void multiStopReceiver() {

        queueName = "NapoliSlowTest_" + System.currentTimeMillis();

        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName,
                NapoliTestUtil.getProperty("napoli.func.mixGroupName"));

        logger.info("yanny test multiStopReceiver starts with queueName=" + queueName);

        DefaultAsyncReceiver receiver = new DefaultAsyncReceiver();
        receiver.setConnector(connector);
        receiver.setName(queueName);

        receiver.setStoreEnable(true);
        MyWorker normalWorker = new MyWorker();
        normalWorker.workerName = "normal worker";
        normalWorker.logEnabled = true;
        receiver.setPeriod(1000);
        try {
            try {
                receiver.setWorker(normalWorker);
                receiver.init();
                receiver.start();
            } catch (Exception e) {
            }

            AbstractTestBase.expectedConsumerSession = receiver.getExpectedInstances();

            ThreadMXBean mBean = ManagementFactory.getThreadMXBean();

            int originalCount = mBean.getThreadCount();
            

            //List<JMXConnector> connectors = JmxUtil.getJMXConnectors(connector.getAddress(), queueName);

            for (int i = 0; i < 100; i++) {

                logger.info("loop " + i);
                try {
                    
                    NapoliTestUtil.sleep(10);
                    receiver.start();
                    logger.info("loop " + i + " stop, session size is "
                            + JmxUtil.getConsumerSessionCount(connector.getAddress(),  queueName));

                } catch (NapoliClientException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                receiver.stop();
            } catch (NapoliClientException e) {
                e.printStackTrace();
            }

            NapoliTestUtil.sleepSecond(10);
            

            int endCount = mBean.getThreadCount();

            String errorMessage = "";
            if (endCount > originalCount * 2) {
                errorMessage = "start and stop receiver should not increase thread too much, increased more than 100%, begin "
                        + originalCount + " end is:" + endCount;
            }

            int sessionCount = JmxUtil.getConsumerSessionCount(connector.getAddress(),  queueName);

            
            if (sessionCount != 0) {
                errorMessage += ";after consumer stop, the consumer session should all be closed, but has "
                        + sessionCount + " left";
            }

            if (!errorMessage.equals("")) {
                logger.error(errorMessage);
                fail(errorMessage);
            }

        } finally {
            if (receiver != null) {
                receiver.close();
            }

            HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName);
        }
    }

}
