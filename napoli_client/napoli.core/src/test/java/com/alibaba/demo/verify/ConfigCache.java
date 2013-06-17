package com.alibaba.demo.verify;

import com.alibaba.napoli.common.util.ConfigServiceHttpImpl;
import com.alibaba.napoli.common.util.NapoliMessageUtil;

import com.alibaba.napoli.domain.client.ClientDestination;
import java.util.concurrent.CountDownLatch;

/**
 * User: heyman
 * Date: 9/30/11
 * Time: 1:12 下午
 */
public class ConfigCache {
    public static void main(String[] args) {
        int threadnum = 1;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completeLatch = new CountDownLatch(threadnum);

        Runnable task = new Runnable() {
            public void run() {
                try {
                    startLatch.await();
                    ConfigServiceHttpImpl configService = NapoliMessageUtil.getConfigService("napoli.alibaba-inc.com");
                    for (int i = 0; i < 100; i++) {
                        ClientDestination destination = configService.fetchDestination("BUC_NOTIFY_AONE_QUEUE");
                        System.out.println(destination.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completeLatch.countDown();
                }
            }
        };

        for (int i = 0; i < threadnum; ++i) {
            Thread thread = new Thread(task);
            thread.setName("send-thread--" + i);
            thread.start();
        }

        long start = System.currentTimeMillis();
        startLatch.countDown();
        try {
            completeLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("cost:"+(System.currentTimeMillis() - start));
        /*Map<String,QueueEntity>  routers = configService.fetchQueue4Router();

        for (Map.Entry<String,QueueEntity> entry : routers.entrySet()){
            QueueEntity value = entry.getValue();
            StringBuilder sb = new StringBuilder();
            sb.append(value.getName()).append(":").append(value.getRouterTargetUrl()).append(":").append(value.getRouterTargetName()).append(";");
            System.out.println("routers:"+sb.toString());
        }

        Destination destinationContext = configService.fetchDestination("BING_PUBLISH_BUYOFFER_TOPIC");
        System.out.println(destinationContext);*/


        /*QueueEntity destinationContext = (QueueEntity)configService.fetchDestination("queuetest0");
        Map<Integer, PhysicalQueue> physicalQueueMap = destinationContext.getPhysicalQueueMap();
        for (Map.Entry<Integer, PhysicalQueue> entry : physicalQueueMap.entrySet()){
            PhysicalQueue physicalQueue = entry.getValue();
            System.out.println(physicalQueue.getState());
        }*/

        //System.out.println(destinationContext.getPhysicalQueueMap());
        /*long now = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            configService.fetchQueue4Router();
        }
        System.out.println("cost:"+(System.currentTimeMillis() -now));*/
    }
}
