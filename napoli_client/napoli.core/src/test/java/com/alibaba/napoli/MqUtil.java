package com.alibaba.napoli;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.common.util.JmxUtil;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 6/8/12 Time: 5:05 PM
 */
public class MqUtil {
    private static final Log log = LogFactory.getLog(MqUtil.class);

    public final static class Result {
        public final AtomicLong successCount = new AtomicLong(0);
        public final AtomicLong failCount = new AtomicLong(0);
        public long costTime;

        @Override
        public String toString() {
            return "Result{" +
                    "successCount=" + successCount +
                    ", failCount=" + failCount +
                    ", costTime=" + costTime +
                    '}';
        }
    }

    public static Result sendMessages(String address, String destination, int threadNum, long msgSum) throws Exception {
        NapoliConnector connector = new NapoliConnector(address);
        connector.setPoolSize(threadNum);
        connector.init();
        return sendMessages(connector, destination, threadNum, msgSum, new NapoliMessage(new byte[1024]));
    }

    public static Result sendMessages(NapoliConnector connector, String destination, int threadNum, long msgSum, final NapoliMessage napoliMessage) throws Exception {
        final DefaultAsyncSender sender = DefaultAsyncSender.createSender(connector, destination, false);
        sender.init();
        Result result = sendMessages(sender, threadNum, msgSum, napoliMessage);
        sender.close();
        connector.close();
        return result;
    }

    public static Result sendMessages(final DefaultAsyncSender sender, int threadNum, long msgSum, final NapoliMessage message) throws Exception {
        final Result result = new Result();
        final AtomicLong msgSumCount = new AtomicLong(msgSum);
        final CountDownLatch endLatch = new CountDownLatch(threadNum);
        long begin = System.currentTimeMillis();
        for (int i = 0; i < threadNum; i++) {
            Thread thread = new Thread("destination=" + sender.getName() + " client=" + i) {
                public void run() {
                    try {
                        while (msgSumCount.decrementAndGet() >= 0) {
                            if (sender.send(message).success()) {
                                result.successCount.incrementAndGet();
                            } else {
                                result.failCount.incrementAndGet();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    endLatch.countDown();
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
        endLatch.await();
        log.info("yanny send message )" + msgSum + " finished with " + result.successCount.get() + " succeed");

        result.costTime = System.currentTimeMillis() - begin;
        return result;
    }

    public static long receive(String address, String queueName, int instances) throws Exception {
        NapoliConnector connector = new NapoliConnector(address);
        connector.init();

        final AtomicLong count = new AtomicLong();
        DefaultAsyncReceiver defaultReceiver = DefaultAsyncReceiver.createReceiver(connector, queueName, false, instances, new AsyncWorker() {
            @Override
            public boolean doWork(Serializable message) {
                count.incrementAndGet();
                return true;
            }
        }, "heymanchen");
        defaultReceiver.start();
        System.out.println("receiver start ok");

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
        connector.close();
        System.out.println("========================receive from "+queueName+" messages:"+count.get());
        return count.get();
    }
}
