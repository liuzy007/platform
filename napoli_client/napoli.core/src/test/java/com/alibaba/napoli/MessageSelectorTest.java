package com.alibaba.napoli;

import com.alibaba.napoli.common.util.JmxUtil;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Test;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.connector.ConsoleConnector;

/**
 * User: heyman Date: 5/2/12 Time: 1:16 下午
 */
public class MessageSelectorTest {
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
      ConsoleConnector.closeAll();
    }
    @Test
    public void testHornetq() throws Exception {
        String consoleAddress = NapoliTestUtil.getAddress();
        String destination = "messageSelectorTest";
        HttpUtil.createQueueIfNotExist(consoleAddress, destination,
                NapoliTestUtil.getProperty("napoli.func.hornetQGroupName"));
        JmxUtil.deleteAllMessage(consoleAddress, destination);

        NapoliConnector connector = new NapoliConnector();
        connector.setAddress(consoleAddress);
        connector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath") + "\\" + new Date().hashCode());
        connector.init();

        final AtomicInteger offerCount = new AtomicInteger(0);
        DefaultAsyncReceiver offerReceiver = new DefaultAsyncReceiver();
        offerReceiver.setConnector(connector);
        offerReceiver.setName(destination);
        offerReceiver.setStoreEnable(false);
        offerReceiver.setInstances(5);
        offerReceiver.setWorkerPri(new AsyncWorker() {
            public boolean doWork(Serializable message) {
                System.out.println("type='offer' receiver sum:" + offerCount.incrementAndGet());
                return true;
            }
        });
        offerReceiver.setMessageSelector("type='offer'");
        offerReceiver.init();
        offerReceiver.start();

        final AtomicInteger memberCount = new AtomicInteger(0);
        DefaultAsyncReceiver memberReceiver = new DefaultAsyncReceiver();
        memberReceiver.setConnector(connector);
        memberReceiver.setName(destination);
        memberReceiver.setStoreEnable(false);
        memberReceiver.setInstances(5);
        memberReceiver.setWorkerPri(new AsyncWorker() {
            public boolean doWork(Serializable message) {
                System.out.println("type='member' receiver sum:" + memberCount.incrementAndGet());
                return true;
            }
        });
        memberReceiver.setMessageSelector("type='member'");
        memberReceiver.init();
        memberReceiver.start();

        DefaultAsyncSender sender = DefaultAsyncSender.createSender(connector, destination, false);
        sender.init();
        for (int i = 0; i < 5; i++) {
            NapoliMessage napoliMessage = new NapoliMessage("type=offer");
            napoliMessage.setProperty("type", "offer");
            sender.send(napoliMessage);
        }

        for (int i = 0; i < 6; i++) {
            NapoliMessage napoliMessage2 = new NapoliMessage("type=member");
            napoliMessage2.setProperty("type", "member");
            sender.send(napoliMessage2);
        }

        JmxUtil.waitTillQueueSizeZero(consoleAddress, destination);
        sender.close();
        offerReceiver.close();
        memberReceiver.close();
        connector.close();
    }
}
