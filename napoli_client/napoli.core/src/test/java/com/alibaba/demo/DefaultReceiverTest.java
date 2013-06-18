package com.alibaba.demo;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.model.MyWorker;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.util.JmxUtil;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

/**
 * 测试消息的工作者处理接口
 * 
 * @author munch.wangr
 */
public class DefaultReceiverTest {

    static NapoliConnector connector;
    static DefaultAsyncSender   sender;

    static DefaultAsyncSender   sender2;
    static DefaultAsyncReceiver queueCleanReceiver       = new DefaultAsyncReceiver();
    protected static MyWorker   qWorker                  = new MyWorker();

    static int                  redeliveryCallBackCalled = 0;

    static String               queueName                = "TempTest";

    static String               queueName2               = "happytest";

    //"DefaultAsyncReceiverMessageTest";

    static int                  fetchCount               = 1;

    public static void setUpBeforeClass() throws Exception {

        System.out.println("yanny prefetch = " + System.getProperty("prefetch"));
        fetchCount = Integer.parseInt(System.getProperty("prefetch", "100"));

        connector = new NapoliConnector();
        connector.setAddress(NapoliTestUtil.getAddress());
        connector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath") + "\\" + new Date().hashCode());
        connector.setPoolSize(5);
        connector.setPrefetch(100);
        connector.init();

        sender = new DefaultAsyncSender();
        sender.setConnector(connector);
        sender.setName(queueName);
        sender.setStoreEnable(false);
        //   sender.init();    

        sender2 = new DefaultAsyncSender();
        sender2.setConnector(connector);
        sender2.setName(queueName);
        sender2.setStoreEnable(false);
        //   sender2.init();  
    }

    public static void tearDownAfterClass() throws Exception {
    }

    public void testMessageReceiver() throws Exception {

        DefaultAsyncReceiver receiver = new DefaultAsyncReceiver();

        sender.init();
        sender2.init();

        int i = 0;
        /*
         * while (i < 1) { Boolean result = sender.send("test prefetch " + i);
         * if (result) { i++; } else {
         * System.out.println("yanny: send test prefetch " + i + " failed"); } }
         */

        try {

            queueCleanReceiver.setConnector(connector);
            queueCleanReceiver.setName(queueName2);

            queueCleanReceiver.init();
            queueCleanReceiver.setWorker(new MyWorker());
            queueCleanReceiver.start();

            receiver.setConnector(connector);
            receiver.setName(queueName);
            receiver.setInstances(3);
            //   receiver.setMessageNotHandle(true);
            receiver.setStoreEnable(true);
            //  WorkerFailure worker = new WorkerFailure();
            MyWorker worker = new MyWorker();
            worker.logEnabled = true;
            receiver.setWorker(worker);

            receiver.init();

            receiver.start();

            long queueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);
            assertTrue(queueSize + " messages stuck in the queue, should all be consumed", queueSize <= 0);

            //TODO: should also verify the messages are in DLQ queues. 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!receiver.isClosed()) {
                System.out.println("yanny close receiver");
                receiver.stop();
            }
        }
    }

    static class WorkerFailure implements AsyncWorker {
        private AtomicInteger accessNum = new AtomicInteger();

        public boolean doWork(Serializable message) {
            System.out.println(new Date().toString() + " WorkerFailure doWork(Serializable) yanny message is "
                    + message);
            System.out.println("=========================" + message + "==============================");
            accessNum.incrementAndGet();

            //  throw new RuntimeException("yanny doWork throw exception " + message);

            return false;
        }

        public int getAccessNum() {
            return accessNum.get();
        }

        public void resetAccessNum() {
            accessNum.lazySet(0);
        }
    }

    public static void main(String[] args) throws Exception{
        DefaultReceiverTest defaultReceiverTest = new DefaultReceiverTest();
        DefaultReceiverTest.setUpBeforeClass();
        defaultReceiverTest.testMessageReceiver();
        DefaultReceiverTest.tearDownAfterClass();
        
    }

}
