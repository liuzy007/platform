package com.alibaba.demo;

import com.alibaba.napoli.MemoryTestUtil;
import com.alibaba.napoli.PerfCase;
import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.common.util.JmxUtil;
import com.alibaba.napoli.receiver.Receiver;
import com.alibaba.napoli.sender.Sender;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 1/20/12 Time: 1:18 下午
 */
public class HornetQSenderTest {
    private static final Log log = LogFactory.getLog(HornetQSenderTest.class);

    private String consoleAddress = "10.33.145.22";
    //private Integer          hqPort          = 1234;

    private String hqDest = "hqtest";
    private String amqDest = "amqtest";
    //private String           coreDestination = "jms.queue.hqtest";
    private NapoliConnector consoleConnector = NapoliConnector.createConnector(consoleAddress, 20, 5);

    public void setup() {
        consoleConnector.init();
        JmxUtil.deleteAllMessage(consoleAddress, hqDest);
        JmxUtil.deleteAllMessage(consoleAddress, amqDest);
    }

    public void close() {
        JmxUtil.deleteAllMessage(consoleAddress, hqDest);
        JmxUtil.deleteAllMessage(consoleAddress, amqDest);
        consoleConnector.close();
    }

    public void testamq() throws Exception {
        perfTest(hqDest);
    }

    public void testhornetq() throws Exception {
        perfTest(amqDest);
    }

    private void perfTest(final String target) throws Exception {
        final byte[] testTxt = new byte[1024];
        final AtomicLong sendCount = new AtomicLong(0);
        final AtomicLong recvCount = new AtomicLong(0);

        PerfCase perfCase = new PerfCase() {
            DefaultAsyncSender sender;

            public void setup() throws Exception {
                sender = DefaultAsyncSender.createSender(consoleConnector, target, false);
                sender.init();
            }

            public void excute() throws Exception {
                if (sender.sendMessage(testTxt)) {
                    sendCount.incrementAndGet();
                }
            }

            public void close() {
                sender.close();
            }
        };
        MemoryTestUtil.perf(20, 100000, perfCase);

        AsyncWorker worker = new AsyncWorker() {
            public boolean doWork(Serializable message) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                recvCount.incrementAndGet();
                return true;
            }
        };

        DefaultAsyncReceiver receiver = DefaultAsyncReceiver.createReceiver(consoleConnector, target, false, 10, worker);
        receiver.start();
        long now = System.currentTimeMillis();
        MemoryTestUtil.perf(20, 100000, perfCase);
        while (recvCount.get() < sendCount.get()) {
            Thread.sleep(100);
        }
        System.out.println("target[" + target + "] receiver cost:" + (now - System.currentTimeMillis()));
    }

    public static void main(String[] args) throws Exception {
        HornetQSenderTest hornetQSenderTest = new HornetQSenderTest();
        hornetQSenderTest.setup();
        hornetQSenderTest.testamq();
        hornetQSenderTest.testhornetq();
        hornetQSenderTest.close();
    }

}
