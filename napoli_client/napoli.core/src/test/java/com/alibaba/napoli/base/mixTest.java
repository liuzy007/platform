package com.alibaba.napoli.base;

import com.alibaba.napoli.MqUtil;
import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.common.persistencestore.StoreDiscard;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.common.util.JmxUtil;
import com.alibaba.napoli.connector.ConsoleConnector;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: heyman
 * Date: 6/12/12
 * Time: 2:55 PM
 */
public class mixTest {
    private static final String address    = NapoliTestUtil.getAddress();

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
      ConsoleConnector.closeAll();
    }
    
    @Test
    public void testQueueSender() throws Exception {
        String queueName = "mixTest";
        long sendSum = 1000;
        HttpUtil.createQueueIfNotExist(address, queueName, "mixGroup");
        JmxUtil.deleteAllMessage(address, queueName);
        MqUtil.Result result = MqUtil.sendMessages(address,queueName, 10, sendSum);
        System.out.println(result);
        assertEquals("sendSuccess num is wrong!", sendSum, result.successCount.get());
        assertEquals(sendSum, JmxUtil.getQueueSize(address,queueName));
        JmxUtil.deleteAllMessage(address,queueName);
    }

    @Test
    public void testQueueSendAndReceiver() throws Exception {
        String queueName = "mixTest";
        long sendSum = 1000;
        int instances = 6;

        HttpUtil.createQueueIfNotExist(address, queueName, "mixGroup");
        JmxUtil.deleteAllMessage(address, queueName);
        MqUtil.Result result = MqUtil.sendMessages(address,queueName, 10, sendSum);
        System.out.println(result);
        assertEquals("sendSuccess num is wrong!"+result, sendSum, result.successCount.get());
        assertEquals(sendSum, JmxUtil.getQueueSize(address,queueName));
        assertEquals(sendSum, MqUtil.receive(address,queueName, instances));
    }

    @Test
    public void testTopic() throws Exception {
        String vtopic = "mixTopic";
        long sendSum = 100;
        final int instances = 6;
        String[] queues = new String[] { vtopic + "-0", vtopic + "-1", vtopic + "-2", vtopic + "-3", vtopic + "-4",
                vtopic + "-5", vtopic + "-6", vtopic + "-7", vtopic + "-8", vtopic + "-9", vtopic + "-10",
                vtopic + "-11", vtopic + "-12"};
        for (String queueName : queues){
            HttpUtil.createQueueIfNotExist(address, queueName, "mixGroup");
        }
        HttpUtil.deleteVtopic(address,vtopic);
        HttpUtil.createVtopic(address, vtopic, queues);
        JmxUtil.deleteAllMessage(address,vtopic);

        MqUtil.Result result = MqUtil.sendMessages(address,vtopic, 10, sendSum);
        System.out.println(result);
        assertEquals(sendSum,result.successCount.get());

        final AtomicLong receiveCount = new AtomicLong(0);
        final CountDownLatch endLatch = new CountDownLatch(queues.length);
        for (final String queueName : queues){
            Thread receiveThread = new Thread(){
                @Override
                public void run() {
                    try {
                        receiveCount.addAndGet(MqUtil.receive(address,queueName,instances));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    endLatch.countDown();
                }
            };
            receiveThread.setDaemon(true);
            receiveThread.start();
        }
        endLatch.await();
        assertEquals((Long)(sendSum*queues.length),(Long)receiveCount.get());
    }
    
    @Test
    public void testResend() throws Exception{
        String queueName = "mixTest3";
        long sendSum = 100;
        int instances = 6;

        HttpUtil.createQueueIfNotExist(address, queueName, "mixGroup");
        JmxUtil.deleteAllMessage(address, queueName);
        MqUtil.Result result = MqUtil.sendMessages(address,queueName, 10, sendSum);
        System.out.println(result);

        NapoliConnector connector = new NapoliConnector(address);
        connector.init();
        KVStore kvStore = ConsoleConnector.getorCreateBdbKvStore(connector.getStorePath(), queueName, NapoliConstant.CLIENT_TYPE_RECEIVER);
        kvStore.clear();
        
        DefaultAsyncReceiver defaultReceiver = DefaultAsyncReceiver.createReceiver(connector,queueName,true,instances, new AsyncWorker() {
            public boolean doWork(Serializable message) {
                return false;
            }
        });
        defaultReceiver.setReprocessInterval(Integer.MAX_VALUE);
        defaultReceiver.start();
        
        int i = 0;
        while (JmxUtil.getQueueSize(address, queueName) > 0) {
            Thread.sleep(1000);
            i++;
            if (i > 25) {
                defaultReceiver.close();
                throw new RuntimeException("receive time out!");
            }
        }
        defaultReceiver.close();
        System.out.println("receive storeSize="+kvStore.getStoreSize());
        assertEquals(sendSum, kvStore.getStoreSize());

        final AtomicLong count2 = new AtomicLong(0);
        DefaultAsyncReceiver defaultReceiver2 = DefaultAsyncReceiver.createReceiver(connector,queueName,true,instances, new AsyncWorker() {
            public boolean doWork(Serializable message) {
                count2.incrementAndGet();
                System.out.println();
                return true;
            }
        });
        defaultReceiver2.setReprocessInterval(1000);
        defaultReceiver2.start();
        i=0;
        while (count2.get() < sendSum){
            Thread.sleep(500);
            i++;
            if (i > 10) {
                defaultReceiver2.close();
                throw new RuntimeException("receive time out!");
            }
            System.out.println("reprocess count="+count2.get()+" localstore="+kvStore.getStoreSize());
        }
        assertEquals(sendSum,count2.get());
    }
    
    @Test
    public void testStoreDiscard() throws Exception{
        String queueName = "mixTest";
        long sendSum = 100;
        int instances = 6;

        HttpUtil.createQueueIfNotExist(address, queueName, "mixGroup");
        JmxUtil.deleteAllMessage(address, queueName);
        MqUtil.Result result = MqUtil.sendMessages(address,queueName, 10, sendSum);
        System.out.println(result);

        NapoliConnector connector = new NapoliConnector(address);
        connector.init();
        KVStore kvStore = ConsoleConnector.getorCreateBdbKvStore(connector.getStorePath(), queueName, NapoliConstant.CLIENT_TYPE_RECEIVER);
        kvStore.clear();

        DefaultAsyncReceiver defaultReceiver = DefaultAsyncReceiver.createReceiver(connector,queueName,true,instances, new AsyncWorker() {
            public boolean doWork(Serializable message) {
                return false;
            }
        });
        defaultReceiver.setReprocessInterval(200);
        defaultReceiver.setReprocessNum(2);
        defaultReceiver.setStoreDiscard(new StoreDiscard() {
            @Override
            public void storeDiscard(NapoliMessage napoliMessage) {
                System.out.println(napoliMessage.getProps());
                System.out.println(napoliMessage.getContent());
            }
        });
        defaultReceiver.start();
        int i = 0;
        while (kvStore.getStoreSize() > 0){
            i++;
            Thread.sleep(1000);
            System.out.println("kvstore size="+kvStore.getStoreSize());
            if (i>15){
                break;
            }
        }
        assertEquals(0,kvStore.getStoreSize());
    }
}
