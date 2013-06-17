package com.alibaba.demo;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.alibaba.napoli.client.connector.NapoliConnector;
import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;

import com.alibaba.napoli.common.util.JmxUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.client.model.MyWorker;
import com.alibaba.napoli.client.model.Person;

import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.constants.NapoliConstant;

import com.alibaba.napoli.sender.impl.DefaultSenderImpl;

/**
 * DefaultAsyncReceiver unit test
 * 
 * @author munch.wangr
 */
public class DebugHornet {
    private static final Log log         = LogFactory.getLog("DefaultAsyncReceiverTest");

    private NapoliConnector consoleConnector;

    static String            testName    = "";

    String                   queueName   = "2";
    DefaultAsyncReceiver     receiver;

    private static String    msg_body_1k = "message body,FD 和 Type 列的含义最为模糊，它们提供了关于文件如何使用的更多信息。"
                                                 + "FD 列表示文件描述符，应用程序通过文件描述符识别该文件。Type 列提供了关于文件格"
                                                 + "式的更多描述。我们来具体研究一下文件描述符列，清单 1 中出现了三种不同的值。cwd 值表示"
                                                 + "应用程序的当前工作目录，这是该应用程序启动的目录，除非它本身对这个目录进行更改。txt 类型的文件是程序代码"
                                                 + "，如应用程序二进制文件本身或共享库，再比如本示例的列表中显示的 init 程序。最后，数值表示应用程序的文件描述符，"
                                                 + "这是打开该文件时返回的一个整数。在清单 1 输出的最后一行中，您可以看到用户正在使用 vi "
                                                 + "编辑 /var/tmp/ExXDaO7d，其文件描述符为 3。u 表示该文件被打开并处于读取/写入模式，而不是只读 (r) "
                                                 + "或只写 (w) 模式。有一点不是很重要但却很有帮助，初始打开每个应用程序时，都具有三个文件描述符，从 0 到 2，";

    public static void init() throws Exception {
        NapoliConstant.CONNECTION_CHECK_PERIOD = 1000 * 5;
        NapoliConstant.MAX_IDLE_TIME = 1000 * 1200;

    }

    public void setup() {

        log.info("test started!");
        consoleConnector = new NapoliConnector("10.33.145.22");
        consoleConnector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath") + "\\"
                + new Date().hashCode());
        consoleConnector.setPoolSize(100);
        consoleConnector.setIdlePeriod(1000 * 60 * 2);
        consoleConnector.init();

    }

    public void close() {
        log.info("test " + testName + " finished");
        consoleConnector.close();
        receiver.close();
    }

    public void testSendAndReceive() throws Exception {

        testName = "testSendAndReceive";
        Date time = new Date();

        String suffix = time.toString();
        log.info("test started at " + suffix);
        long messageCount = JmxUtil.getQueueSize(consoleConnector.getAddress(), queueName);
        long beginEnQueue = JmxUtil.getEnQueue(consoleConnector.getAddress(), queueName);

        JmxUtil.deleteAllMessage(consoleConnector.getAddress(), queueName);

        long endQueueSize = JmxUtil.getQueueSize(consoleConnector.getAddress(), queueName);
        log.info("queue " + queueName + " has " + messageCount + " messages at enQueue " + beginEnQueue
                + " after, delete, queueSize is " + endQueueSize);


        DefaultSenderImpl sender = new DefaultSenderImpl();
        sender.setConnector(consoleConnector);
        sender.setName(queueName);
        sender.setStoreEnable(false);
        sender.setReprocessInterval(3000 * 1000);
        sender.init();

        receiver = new DefaultAsyncReceiver();

        receiver.setConnector(consoleConnector);
        receiver.setName(queueName);
        receiver.setInstances(1);
        receiver.setStoreEnable(false);
        receiver.setPeriod(NapoliConstant.MIN_HEARTBEAT_PERIOD);

        MyWorker worker = new MyWorker();
        worker.logEnabled = true;

        receiver.setWorker(worker);
        receiver.init();
        receiver.start();

        int sendSucceed = 0;
        int count = 2;
        long beginTime = System.currentTimeMillis();

        Thread t = new Thread("thread--get queue size:" + queueName) {
            public void run() {

                while (true) {
                    NapoliTestUtil.sleepSecond(20);
                    long messageEnQueueCount = JmxUtil.getEnQueue(consoleConnector.getAddress(), queueName);
                    long messageCount = JmxUtil.getQueueSize(consoleConnector.getAddress(), queueName);

                    long consumerCount = JmxUtil.getConsumerSessionCount(consoleConnector.getAddress(), 
                            queueName);
                    log.info("yanny message count:" + messageCount + " EnQueue:" + messageEnQueueCount
                            + " consumer Count is " + consumerCount);
                }
            }

        };
        //  t.start();

        for (int i = 0; i < count; i++) {
            boolean result = sender.sendMessage("中文消息message " + i + "_" + suffix);
            if (!result) {
                System.out.println("send message " + i + " failed.");
            } else {
                sendSucceed++;
            }
        }

        long receiverBeginTime = System.currentTimeMillis();

        log.info("yanny send " + count + " succeed " + sendSucceed + " takes " + (receiverBeginTime - beginTime));

        for (int i = 0; i < count; i++) {
            boolean result = sender.sendMessage(new Person("Person_" + (count + i) + "_" + suffix));
            if (!result) {
                System.out.println("send object message " + (count + i) + " failed.");
            } else {
                sendSucceed++;
            }
        }

        log.info("yanny send object message " + count + " total send succeed " + sendSucceed + " takes "
                + (receiverBeginTime - beginTime));
      
        JmxUtil.waitTillQueueSizeAsTarget(consoleConnector.getAddress(), queueName, endQueueSize);
        log.info("yanny send " + 2 * count + " messages, succeed " + sendSucceed + ", worker received "
                + worker.getAccessNum());

        assertTrue("should have some send succeed, but all failed", sendSucceed > 0);
        assertTrue(" worker should only get " + sendSucceed + " messages, but get " + worker.getAccessNum(),
                worker.getAccessNum() == sendSucceed);

    }

    static AtomicInteger sendSucceed = new AtomicInteger();

    public void testSendAndReceivePerf() throws Exception {

        testName = "testSendAndReceive";
        Date time = new Date();

        final String suffix = time.toString();

        sendSucceed = new AtomicInteger();
        try {

            log.info("test started at " + suffix);
         
            long messageCount = JmxUtil.getQueueSize(consoleConnector.getAddress(), queueName);
            long beginEnQueue = JmxUtil.getEnQueue(consoleConnector.getAddress(), queueName);

        //    JmxUtil.deleteAllMessage(consoleConnector.getAddress(), queueName);

            long endQueueSize = JmxUtil.getQueueSize(consoleConnector.getAddress(), queueName);
            log.info("queue " + queueName + " has " + messageCount + " messages at enQueue " + beginEnQueue
                    + " after, delete, queueSize is " + endQueueSize);

            //final List<JMXConnector> connectors2 = JmxUtil.getJMXConnectors(consoleConnector.getAddress(), queueName);

            receiver = new DefaultAsyncReceiver();

            receiver.setConnector(consoleConnector);
            receiver.setName(queueName);
            receiver.setInstances(25);
            receiver.setStoreEnable(false);
            receiver.setPeriod(NapoliConstant.MIN_HEARTBEAT_PERIOD);

            final MyWorker worker = new MyWorker(0.01, 10000);

            worker.isPerf = true;

            receiver.setWorker(worker);
            receiver.init();

            final int count = 100;
            long startTime = System.currentTimeMillis();

            final AtomicInteger totalSend = new AtomicInteger();

            Thread t = new Thread("thread--get queue size:" + queueName) {
                public void run() {

                    while (true) {

                        NapoliTestUtil.sleepSecond(60);
                        long messageEnQueueCount = JmxUtil.getEnQueue(consoleConnector.getAddress(), queueName);
                        long messageCount = JmxUtil.getQueueSize(consoleConnector.getAddress(), queueName);

                        long consumerCount = JmxUtil.getConsumerSessionCount(consoleConnector.getAddress(),
                                 queueName);
                        log.info("yanny message count:" + messageCount + " EnQueue:" + messageEnQueueCount
                                + " consumer Count is " + consumerCount);

                    }
                }

            };
            t.start();

            Thread t2 = new Thread("thread--get receiver count QPS:" + queueName) {
                public void run() {

                    long messageEnQueueCount = 0;
                    while (true) {

                        NapoliTestUtil.sleepSecond(30);
                        long newEnQueueCount = worker.getAccessNum();

                        if (newEnQueueCount > messageEnQueueCount) {
                            log.info("yanny 1 second receive message is " + (newEnQueueCount - messageEnQueueCount)
                                    / 30 + " receiver instance: " + receiver.getInstances());
                            messageEnQueueCount = newEnQueueCount;
                        }
                    }
                }

            };
            t2.start();

            Thread t3 = new Thread("thread--get sender count QPS:" + queueName) {
                public void run() {

                    long messageEnQueueCount = 0;
                    while (true) {

                        NapoliTestUtil.sleepSecond(30);
                        long newEnQueueCount = sendSucceed.get();

                        if (newEnQueueCount > messageEnQueueCount) {
                            log.info("yanny 1 second send success message is "
                                    + (newEnQueueCount - messageEnQueueCount) / 30 + " totalSend:" + totalSend.get()
                                    + " totalSucceed:" + newEnQueueCount);
                            messageEnQueueCount = newEnQueueCount;
                        }
                    }
                }

            };
            t3.start();

            final DefaultSenderImpl sender = new DefaultSenderImpl();
            sender.setConnector(consoleConnector);
            sender.setName(queueName);
            sender.setStoreEnable(false);
            sender.setReprocessInterval(3000 * 1000);
            try {
                sender.init();
            } catch (NapoliClientException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            int tc = 0;
            final Semaphore semaphore = new Semaphore(tc);

            for (int i = 0; i < tc; i++) {
                Thread st = new Thread("thread--sender " + queueName + " " + i) {
                    public void run() {

                        try {
                            semaphore.acquire();

                            for (int i = 0; i < count; i++) {
                                totalSend.incrementAndGet();
                                //boolean result = sender.sendMessage(new Person("Person_" + (count + i) + "_" + suffix));
                                boolean result = sender.sendMessage("message_" + (count + i) + "_" + suffix);// + msg_body_1k);
                                if (!result) {
                                    System.out.println("send message " + i + " failed.");
                                } else {
                                    sendSucceed.incrementAndGet();
                                }

                                /*
                                 * totalSend.incrementAndGet(); i++; result =
                                 * sender.sendMessage(new Person("Person_" +
                                 * (count + i) + "_" + suffix + msg_body_1k));
                                 * if (!result) {
                                 * System.out.println("send message " + i +
                                 * " failed."); } else {
                                 * sendSucceed.incrementAndGet(); }
                                 */
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        finally {
                            semaphore.release();
                        }

                    }
                };

                st.start();
            }

            receiver.start();

            while (semaphore.availablePermits() != tc) {
                Thread.sleep(4000);
            }

            long endTime = System.currentTimeMillis();
            log.info("yanny: send " + count * tc + " message, succeed " + sendSucceed + ", take "
                    + (endTime - startTime) + " milseconds");

            sender.close();

            JmxUtil.waitTillQueueSizeAsTarget(consoleConnector.getAddress(), queueName, endQueueSize);          

            long receiverEndTime = System.currentTimeMillis();

            log.info("yanny send " + count + " messages, succeed " + sendSucceed + ", worker received "
                    + worker.getAccessNum() + "  takes " + (receiverEndTime - endTime) + " milseconds");

            assertTrue("should have some send succeed, but all failed", sendSucceed.incrementAndGet() > 0);
            assertTrue(" worker should only get " + sendSucceed + " messages, but get " + worker.getAccessNum(),
                    worker.getAccessNum() == sendSucceed.incrementAndGet());
        } finally {
            receiver.close();

            consoleConnector.close();
        }
    }

    public static void main(String[] args) throws Exception{
        DebugHornet debugHornet = new DebugHornet();
        DebugHornet.init();
        debugHornet.setup();
        debugHornet.testSendAndReceive();
        debugHornet.testSendAndReceivePerf();
        debugHornet.close();
    }

}
