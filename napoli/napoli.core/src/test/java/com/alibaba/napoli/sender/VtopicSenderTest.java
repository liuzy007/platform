package com.alibaba.napoli.sender;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.common.util.JmxUtil;
import com.alibaba.napoli.connector.ConsoleConnector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: heyman Date: 3/14/12 Time: 9:48 上午
 */
public class VtopicSenderTest {
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
      ConsoleConnector.closeAll();
    }
    
    @Test
    public void testSender() throws Exception {
        String consoleAddress = NapoliTestUtil.getAddress();
        long timeStamp = System.currentTimeMillis();
        final String vtopicName = "VtopicSenderTest-" + timeStamp;
        final String[] queueNames = new String[] { "topicQueue1-" + timeStamp, "topicQueue2-" + timeStamp,
                "topicQueue3-" + timeStamp, "topicQueue4-" + timeStamp, "topicQueue5-" + timeStamp,
                "topicQueue6-" + timeStamp, "topicQueue7-" + timeStamp, "topicQueue8-" + timeStamp,
                "topicQueue9-" + timeStamp, "topicQueue10-" + timeStamp };
        for (String queuename : queueNames){
            HttpUtil.createQueue(consoleAddress,queuename,"mixGroup");
        }
        HttpUtil.createVtopic(consoleAddress, vtopicName, queueNames);
        NapoliConnector connector = new NapoliConnector();
        connector.setAddress(consoleAddress);
        connector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath") + "\\" + new Date().hashCode());
        connector.setIntervalForTest(200);
        connector.setDataBatchReadCount(100);
        final long oldMax = NapoliConstant.MAX_IDLE_TIME;
        NapoliConstant.MAX_IDLE_TIME = 1000;
        connector.setConnectionCheckPeriod(200);
        connector.init();
        DefaultAsyncSender sender = new DefaultAsyncSender();
        sender.setConnector(connector);
        sender.setStoreEnable(false);
        sender.setName(vtopicName);
        sender.init();
        for (int i = 0; i < 10; i++) {
            sender.send("hello world");
        }

        int sendMsgCount = 0;
        for (String queueName : queueNames) {
            sendMsgCount += JmxUtil.getQueueSize(consoleAddress, queueName);
        }
        assertEquals(100, sendMsgCount);

        HttpUtil.deleteQueueFromVtopic(consoleAddress,vtopicName,new String[]{"topicQueue9-" + timeStamp, "topicQueue10-" + timeStamp });
        Thread.sleep(400);
        
        for (int i = 0; i < 10; i++) {
            sender.send("hello world");
        }
        sendMsgCount = 0;
        for (String queueName : queueNames) {
            sendMsgCount += JmxUtil.getQueueSize(consoleAddress, queueName);
        }
        assertEquals(180,sendMsgCount);
        
        NapoliConstant.MAX_IDLE_TIME = oldMax;
        

        List<DefaultAsyncReceiver> receiverList = new ArrayList<DefaultAsyncReceiver>(10);
        final AtomicInteger receiveMsgCount = new AtomicInteger(0);
        for (String queueName : queueNames) {
            DefaultAsyncReceiver receiver = new DefaultAsyncReceiver();
            receiver.setConnector(connector);
            receiver.setName(queueName);
            receiver.setInstances(10);
            receiver.setStoreEnable(true);
            receiver.setWorker(new AsyncWorker() {
                public boolean doWork(Serializable message) {
                    receiveMsgCount.incrementAndGet();
                    return true;
                }
            });
            receiver.init();
            receiver.start();
            receiverList.add(receiver);
        }
        while (receiveMsgCount.get() < sendMsgCount){
            Thread.sleep(200);
        }

        sender.close();
        for (DefaultAsyncReceiver receiver : receiverList){
            receiver.close();
        }
        connector.close();
        HttpUtil.deleteVtopic(consoleAddress, vtopicName);
        for (String queueName : queueNames) {
            HttpUtil.deleteQueue(consoleAddress,queueName);
        }
    }
}
