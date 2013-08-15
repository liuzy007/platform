package com.alibaba.demo;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.PendingNotify;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: heyman Date: 5/4/12 Time: 11:00 上午
 */
public class SenderWithTransaction {
    private static final String        address      = "10.33.145.22";
    private static final String        queue        = "chhtest";
    private static final int           threadNum    = 10;
    private static final AtomicInteger count        = new AtomicInteger(0);
    private static final AtomicInteger failcount    = new AtomicInteger(0);
    private static final AtomicInteger pendingCount = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        NapoliConnector connector = new NapoliConnector();
        connector.setAddress(address);
        connector.setSendTimeout(1000);
        connector.setPoolSize(threadNum);
        connector.init();

        final DefaultAsyncSender defaultSender = new DefaultAsyncSender();
        defaultSender.setConnector(connector);
        defaultSender.setName(queue);

        PendingNotify pendingNotify = new PendingNotify() {
            @Override
            public PendingNotifyStateEnum notify(NapoliMessage message) {
                System.out.println("pending notify be called " + pendingCount.incrementAndGet());
                return PendingNotifyStateEnum.COMMIT;
            }
        };
        defaultSender.setPendingNotifier(pendingNotify);
        defaultSender.setPendingInterval(30000);
        defaultSender.init();

        final Runnable bizCall = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //throw new RuntimeException("biz error");
            }
        };

        //final DefaultAsyncSender sender = connector.createSender(queue, false);

        final CountDownLatch endLatch = new CountDownLatch(threadNum);
        for (int i = 0; i < threadNum; i++) {
            Thread thread = new Thread("queue=" + queue + " client=" + i) {
                public void run() {
                    for (int j = 0; j < 10; j++) {
                        try {
                            defaultSender.send(new byte[1024], bizCall);
                            System.out.println("success:" + count.incrementAndGet());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    endLatch.countDown();
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
        endLatch.await();
        System.out.println("send ok!!!");
        Thread.sleep(20000000);
    }
}
