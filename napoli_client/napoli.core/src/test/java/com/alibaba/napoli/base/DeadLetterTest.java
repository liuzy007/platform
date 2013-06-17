package com.alibaba.napoli.base;

import com.alibaba.napoli.MqUtil;
import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.RedeliveryCallback;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.common.util.JmxUtil;
import com.alibaba.napoli.connector.ConsoleConnector;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: heyman
 * Date: 8/13/12
 * Time: 3:04 PM
 */
public class DeadLetterTest {
    private static final String address    = NapoliTestUtil.getAddress();
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        ConsoleConnector.closeAll();
    }
    @Test
    public void testHornetqDlq() throws Exception{
        String queueName = "hornetqDLQ2";
        HttpUtil.deleteQueue(address,queueName);
        HttpUtil.createQueue(address, queueName, "hornetqGroup");
        JmxUtil.deleteAllMessage(address, queueName);
        
        MqUtil.Result result = MqUtil.sendMessages(address,queueName, 10, 100);
        assertEquals("sendSuccess num is wrong!", 100, JmxUtil.getQueueSize(address,queueName));

        NapoliConnector connector = new NapoliConnector(address);
        connector.init();
        final AtomicLong rec_count = new AtomicLong();
        DefaultAsyncReceiver defaultReceiver = DefaultAsyncReceiver.createReceiver(connector, queueName, false,6,new AsyncWorker() {
            @Override
            public boolean doWork(Serializable message) {
                //System.out.println(message);
                rec_count.incrementAndGet();
                throw new RuntimeException("error happened!");
                //return false;
            }
        });
        final AtomicLong callback_count = new AtomicLong();
        RedeliveryCallback callback = new RedeliveryCallback() {
            @Override
            public void redeliveryFailed(Serializable msg, Throwable t) {
                //System.out.println(msg);
                if (t instanceof RuntimeException){
                callback_count.incrementAndGet();
                }
            }
        };
        
        defaultReceiver.setRedeliveryCallback(callback);
        defaultReceiver.start();

        int i = 0;
        while (JmxUtil.getQueueSize(address, queueName) > 0) {
            Thread.sleep(1000);
            i++;
            if (i > 30) {
                defaultReceiver.close();
                throw new RuntimeException("receive time out!");
            }
        }
        assertEquals(700,rec_count.get());
        assertEquals(100,callback_count.get());
        assertEquals(100,JmxUtil.getDLQQueueSize(address,queueName));
        
        
        defaultReceiver.close();
        connector.close();
    }

    @Test
    public void testAmqDlq() throws Exception{
        String queueName = "amqDLQ2";
        HttpUtil.deleteQueue(address,queueName);
        HttpUtil.createQueue(address, queueName, "amqGroup");
        JmxUtil.deleteAllMessage(address, queueName);

        MqUtil.Result result = MqUtil.sendMessages(address,queueName, 10, 100);
        assertEquals("sendSuccess num is wrong!", 100, JmxUtil.getQueueSize(address,queueName));

        NapoliConnector connector = new NapoliConnector(address);
        connector.init();
        final AtomicLong rec_count = new AtomicLong();
        DefaultAsyncReceiver defaultReceiver = DefaultAsyncReceiver.createReceiver(connector, queueName, false,6,new AsyncWorker() {
            @Override
            public boolean doWork(Serializable message) {
                //System.out.println(message);
                System.out.println("receive==="+rec_count.incrementAndGet());
                throw new RuntimeException("error happened!");
                //return false;
            }
        });
        final AtomicLong callback_count = new AtomicLong();
        /*RedeliveryCallback callback = new RedeliveryCallback() {
            @Override
            public void redeliveryFailed(Serializable msg, Throwable t) {
                //System.out.println(msg);
                if (t instanceof RuntimeException){
                    callback_count.incrementAndGet();
                }
            }
        };

        defaultReceiver.setRedeliveryCallback(callback);*/
        defaultReceiver.start();

        int i = 0;
        while (JmxUtil.getQueueSize(address, queueName) > 0) {
            Thread.sleep(1000);
            i++;
            if (i > 200) {
                defaultReceiver.close();
                throw new RuntimeException("receive time out!");
            }
        }
        assertEquals(700,rec_count.get());
        //assertEquals(100,callback_count.get());
        assertEquals(100,JmxUtil.getDLQQueueSize(address,queueName));


        defaultReceiver.close();
        connector.close();
    }
}
