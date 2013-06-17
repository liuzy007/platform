package com.alibaba.napoli.base;

import com.alibaba.napoli.MqUtil;
import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;
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
 * User: heyman Date: 6/8/12 Time: 5:13 PM
 */
public class HornetqTest {
    private static final String address    = NapoliTestUtil.getAddress();
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
      ConsoleConnector.closeAll();
    }
    
    @Test
    public void testQueueSender() throws Exception {
        String queueName = "hornetqTest";
        long sendSum = 100L;
        HttpUtil.createQueueIfNotExist(address, queueName, "hornetqGroup");
        JmxUtil.deleteAllMessage(address, queueName);
        queueName = "HornetqTest";
        MqUtil.Result result = MqUtil.sendMessages(address,queueName, 10, sendSum);
        System.out.println(result);
        assertEquals("sendSuccess num is wrong!", sendSum, result.successCount.get());
        queueName = "hornetqTest";
        assertEquals(sendSum,  JmxUtil.getQueueSize(address,queueName));
        JmxUtil.deleteAllMessage(address,queueName);
    }

    @Test
    public void testQueueSendAndReceiver() throws Exception {
        String queueName = "hornetqTest";
        long sendSum = 100;
        int instances = 6;

        HttpUtil.createQueueIfNotExist(address, queueName, "hornetqGroup");
        JmxUtil.deleteAllMessage(address,queueName);
        MqUtil.Result result = MqUtil.sendMessages(address,queueName, 10, sendSum);
        System.out.println(result);
        assertEquals("sendSuccess num is wrong!", sendSum, result.successCount.get());
        assertEquals(sendSum, JmxUtil.getQueueSize(address,queueName));
        assertEquals(sendSum, MqUtil.receive(address,queueName, instances));
    }

    @Test
    public void testTopic() throws Exception {
        String vtopic = "hornetqTopic";
        long sendSum = 10;
        final int instances = 6;
        String[] queues = new String[] { vtopic + "-0", vtopic + "-1", vtopic + "-2", vtopic + "-3", vtopic + "-4",
                vtopic + "-5", vtopic + "-6", vtopic + "-7", vtopic + "-8", vtopic + "-9", vtopic + "-10",
                vtopic + "-11", vtopic + "-12"};
        for (String queueName : queues){
            HttpUtil.createQueueIfNotExist(address, queueName, "hornetqGroup");
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
    public void testFilter() throws Exception{
        String queueName = "hornetqTest2";
        HttpUtil.createQueueIfNotExist(address, queueName, "hornetqGroup");
        JmxUtil.deleteAllMessage(address, queueName);
        
        final AtomicLong receiveCount1 = new AtomicLong(0); 
        final AtomicLong receiveCount2 = new AtomicLong(0); 
        
        NapoliConnector connector = new NapoliConnector(address);
        connector.init();
        DefaultAsyncSender sender = DefaultAsyncSender.createSender(connector,queueName,false);
        sender.init();
        DefaultAsyncReceiver receiver1 = DefaultAsyncReceiver.createReceiver(connector, queueName, false, 6, new AsyncWorker() {
            public boolean doWork(Serializable message) {
                receiveCount1.incrementAndGet();
                return true;
            }
        });
        receiver1.setMessageSelector("type='1'");
        receiver1.init();
        receiver1.start();

        DefaultAsyncReceiver receiver2 = DefaultAsyncReceiver.createReceiver(connector, queueName, false, 6, new AsyncWorker() {
            public boolean doWork(Serializable message) {
                receiveCount2.incrementAndGet();
                return true;
            }
        });
        receiver2.setMessageSelector("type='2'");
        receiver2.init();
        receiver2.start();
       
        NapoliMessage napoliMessage = new NapoliMessage("hello world!");
        napoliMessage.setProperty("type","1");
        MqUtil.sendMessages(sender, 10, 100, napoliMessage);
        napoliMessage = new NapoliMessage("hello world2!");
        napoliMessage.setProperty("type","2");
        MqUtil.sendMessages(sender, 10, 1000, napoliMessage);
        sender.close();

        JmxUtil.waitTillQueueSizeZero(address, queueName);

        
        assertEquals(100, receiveCount1.get());
        assertEquals(1000, receiveCount2.get());
    }
}
