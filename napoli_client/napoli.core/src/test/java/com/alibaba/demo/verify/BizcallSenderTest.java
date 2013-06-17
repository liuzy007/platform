package com.alibaba.demo.verify;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.common.util.JmxUtil;
import com.alibaba.napoli.receiver.impl.DefaultReceiverImpl;
import com.alibaba.napoli.sender.impl.AbstractSenderTest;
import com.alibaba.napoli.sender.impl.DefaultSenderImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: heyman
 * Date: 12/28/11
 * Time: 1:59 下午
 */
public class BizcallSenderTest extends AbstractSenderTest {
    private static final Log log = LogFactory.getLog(BizcallSenderTest.class);
    @Test
    public void sendWithBizcallNoPending() throws Exception {
        final AtomicBoolean called = new AtomicBoolean(false);
        final AtomicInteger messageCount = new AtomicInteger(0);
        String address = "10.33.145.22";
        String queueName = "BizSenderTest";
        JmxUtil.deleteAllMessage(address, queueName);


        consoleConnector = new NapoliConnector(address);
        consoleConnector.init();
        DefaultSenderImpl sender = new DefaultSenderImpl();
        sender.setConnector(consoleConnector);
        sender.setName(queueName);
        sender.setStoreEnable(false);
        sender.init();

        DefaultReceiverImpl receiver = new DefaultReceiverImpl();
        receiver.setName(queueName);
        receiver.setConnector(consoleConnector);
        receiver.setInstances(5);
        receiver.setWorker(new AsyncWorker() {
            public boolean doWork(Serializable message) {
                log.info("handle message " + message + " at " + messageCount.get());
                messageCount.incrementAndGet();
                log.info("one bizcall message consumed");
                return true;
            }
        });
        receiver.init();
        receiver.start();
        Runnable bizCall = new Runnable() {
            public void run() {
                called.compareAndSet(false, true);
            }
        };
        
        System.out.println("yanny test");

        for (int i = 0; i < 10; i++) {
            sender.sendMessage("message", bizCall);
        }
        log.info("send all complete");
        assertTrue(called.get());
        int i = 300;
        while (messageCount.get() != 10) {
            Thread.sleep(100);
            i--;
            if (i < 1){
                break;
            }
        }
        assertEquals(10,messageCount.get());

        sender.close();
        //receiver.close();
        consoleConnector.close();
    }


}
