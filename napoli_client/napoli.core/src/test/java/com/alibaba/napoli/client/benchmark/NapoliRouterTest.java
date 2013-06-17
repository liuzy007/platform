/**
 * Project: napoli.client
 *
 * File Created at 2011-9-23
 * $Id: NapoliRouterTest.java 191497 2012-08-01 06:16:59Z yanny.wangyy $
 *
 * Copyright 1999-2100 Alibaba.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.napoli.client.benchmark;

import com.alibaba.dragoon.client.DragoonClient;
import com.alibaba.dragoon.stat.napoli.NapoliSenderStat;
import com.alibaba.dragoon.stat.napoli.NapoliStatManager;

import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.model.MyWorker;
import com.alibaba.napoli.client.model.Person;
import com.alibaba.napoli.client.model.PersonStatus;
import com.alibaba.napoli.common.util.JmxUtil;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;

import com.alibaba.napoli.domain.client.ClientDestination;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.domain.client.ClientVirtualTopic;
import com.alibaba.napoli.receiver.impl.ReceiverMonitorFilter;
import com.alibaba.napoli.sender.impl.SenderMonitorFilter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test router module, RouterTest_Source is configed to router to
 * RouterTest_Target on another amq machine.
 * 
 * @author yanny.wangyy
 */
public class NapoliRouterTest {
    protected static NapoliConnector    connector;
    protected static NapoliConnector    receiverConnector;

    protected static NapoliSenderStat   napoliSenderStat;

    protected static String             initConsumeMessage;

    protected static DefaultAsyncSender qSender;

    @BeforeClass
    public static void init() throws Exception {
        DragoonClient.setJdbcStatEnable(false);
        DragoonClient.setSpringStatEnable(false);
        DragoonClient.setUriStatEnable(false);
        DragoonClient.setLog4jStatEnable(true); // 用于Exception监控项的采集
        DragoonClient.start("NapoliRouterTest"); // 参数：应用名称

        String storePath = NapoliTestUtil.getProperty("napoli.func.storePath") + System.currentTimeMillis();
        NapoliTestUtil.delFiles(storePath);

        connector = new NapoliConnector();
        System.out.println("yanny connector address is " + NapoliTestUtil.getAddress());

        connector.setAddress(NapoliTestUtil.getAddress());
        connector.setStorePath(storePath);
        connector.setJmsUserName(NapoliTestUtil.getUser());
        connector.setJmsPassword(NapoliTestUtil.getPasswd());
        connector.setInterval(6000);
        connector.setPoolSize(25);
        connector.setSendTimeout(1000);
        connector.init();

        receiverConnector = new NapoliConnector();
        receiverConnector.setAddress(NapoliTestUtil.getAddress());
        receiverConnector.setStorePath(storePath);
        receiverConnector.setJmsUserName(NapoliTestUtil.getUser());
        receiverConnector.setJmsPassword(NapoliTestUtil.getPasswd());
        receiverConnector.setInterval(6000);
        receiverConnector.setPoolSize(25);
        // sendConnector.setSendTimeout(1000);
        receiverConnector.init();

        NapoliStatManager napoliStatManager = mock(NapoliStatManager.class);

        SenderMonitorFilter.setMonitor(napoliStatManager);
        ReceiverMonitorFilter.setMonitor(napoliStatManager);
        napoliSenderStat = mock(NapoliSenderStat.class);
        when(napoliStatManager.getSenderStat(anyString(), anyString())).thenReturn(napoliSenderStat);
    }
    
    @AfterClass
    public static void dispose() {
        System.out.println("yanny AfterClass is executed " + System.currentTimeMillis());
      
        if(qSender!=null && qSender.isStarted()){
        	qSender.close();
        }
        
        ConsoleConnector.closeAll();

    }

    @Test
    public void sendMessageToRouterQueueTest() throws Exception {

        System.out.println("start to execute sendMessageToRouterQueueTest");
        
        String routerSource = "RouterTest_Source";
        String routerTarget = "RouterTest_Target";

    //    JmxUtil.deleteAllMessage(connector.getAddress(), routerSource);
    //    JmxUtil.deleteAllMessage(connector.getAddress(), routerTarget);

        qSender = new DefaultAsyncSender();
        qSender.setConnector(connector);
        qSender.setName(routerSource);

        qSender.setStoreEnable(true);
        qSender.setReprocessInterval(10000 * 1000 * 1000);

        qSender.init();

        DefaultAsyncReceiver qReceiver = new DefaultAsyncReceiver();
        try {
            MyWorker qWorker = new MyWorker();
            qWorker.logEnabled = true;
            
            qReceiver.setConnector(connector);
            qReceiver.setStoreEnable(true);
            qReceiver.setInstances(20);
            qReceiver.setName(routerTarget);
            qReceiver.setWorker(qWorker);
            qReceiver.init();
            qReceiver.start();
            
            long beginQueueSizeSource = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), routerSource);

            long beginQueueSizeTarget = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), routerTarget);

            int tc = 15;
            System.out.println("yanny requestcount = " + System.getProperty("NapoliRouterTest.requestCount"));
            final int tp = Integer.parseInt(System.getProperty("NapoliRouterTest.requestCount", "20"));
            final Semaphore semaphore = new Semaphore(tc);
            final AtomicInteger sumCount = new AtomicInteger();

            final AtomicInteger requestCount = new AtomicInteger();
            long startTime = System.currentTimeMillis();
            System.out.println("Yanny start send request " + startTime);

            long startCountSource = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), routerSource);
            long startCountTarget = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), routerTarget);

            for (int i = 0; i < tc; i++) {
                Thread t = new Thread("thread--" + i) {
                    public void run() {
                        try {
                            // 构造消息，消息可以是对象、String、Map等，消息对象必须实现Serializable接口
                            semaphore.acquire();
                            Person person = new Person();

                            person.setLoginName("superman");
                            person.setEmail("sm@1.com");
                            person.setPenName("pname");
                            person.setStatus(PersonStatus.ENABLED);

                            for (int j = 0; j < tp; j++) {
                                // System.out.println("hello");
                                int id = requestCount.incrementAndGet();
                                person.setPersonId("" + id);

                                // 建议处理返回值, 发送成功返回true，否则返回false
                                boolean result = qSender.send(person);
                                if (!result) {
                                    System.out.println("----------------send to queue " + "result is false. personid="
                                            + j);
                                } else {
                                    sumCount.incrementAndGet();
                                }
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        } finally {
                            semaphore.release();
                        }
                    }
                };
                t.start();
            }

            while (semaphore.availablePermits() != tc) {
                Thread.sleep(100);
            }
            int totalRequest = tc * tp;

            long endTime = System.currentTimeMillis();
            System.out.println("yanny: send " + totalRequest + " message, take " + (endTime - startTime)
                    + " milseconds");

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), routerSource, beginQueueSizeSource);
            endTime = System.currentTimeMillis();
            System.out.println("yanny: router finished  send " + totalRequest + " message, succeed  " + sumCount.get()
                    + " message, take " + (endTime - startTime) + " milseconds");

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), routerTarget, beginQueueSizeTarget);

            long endCountSource = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), routerSource);
            long endCountTarget = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), routerTarget);

            endTime = System.currentTimeMillis();
            System.out.println("yanny: consume finished  " + (endCountSource - startCountSource) + " message, take "
                    + (endTime - startTime) + " milseconds");

            String errorMessage = "";

            if (sumCount.get() < totalRequest) {
                errorMessage += ";send success less than total " + totalRequest + ", succeed " + sumCount.get()
                        + ", with store enable, all send should return true";
            }

            if ((endCountTarget - startCountTarget) != (endCountSource - startCountSource)) {
                errorMessage += ";RouterSource received total request " + (endCountSource - startCountSource)
                        + "!=RouterTarget received total request " + (endCountTarget - startCountTarget);
            }

            long qBdbCount = 0;

            // get store size.
            qBdbCount = connector.getSenderKVStore(qSender.getName()).getStoreSize();

            if (qBdbCount > totalRequest * 0.01) {
                errorMessage += ";more than 1% send failed (stored locally) " + qBdbCount + "/" + totalRequest;
            }

            System.out.println("yanny totalRequest " + totalRequest + " send queue success " + sumCount);

            System.out.println(initConsumeMessage);

            System.out.println("yanny " + routerTarget + "'s success=" + qWorker.getAccessNum() + " bdb's size="
                    + qBdbCount);

            if (totalRequest * 1.01 < qWorker.getAccessNum()) {
                errorMessage += ";queue should not have success messages more than " + totalRequest + "*1.01 (success "
                        + qWorker.getAccessNum() + ")";

            }

            if ((sumCount.get() - qBdbCount) > qWorker.getAccessNum()) {
                errorMessage += ";queue received message (" + qWorker.getAccessNum()
                        + ") less than send succeed(" + (sumCount.get() - qBdbCount) + "), message lost";
            }

            int allowedDiff = (int) (sumCount.get() * 0.001);

            if (((qWorker.getAccessNum() + qBdbCount) - sumCount.get()) > allowedDiff) {
                errorMessage += "queue received message should not have more than totalRequest+ " + allowedDiff
                        + " than allowed (0.1%), gap " + ((qWorker.getAccessNum() + qBdbCount) - sumCount.get());
            }

            assertTrue(errorMessage, errorMessage.equals(""));

            verify(napoliSenderStat, atMost(qWorker.getAccessNum())).sendSuccess(anyLong(), anyLong());
            verify(napoliSenderStat, atLeast((int) (sumCount.get() - qBdbCount))).sendSuccess(anyLong(), anyLong());
            verify(napoliSenderStat, times((int) qBdbCount)).sendFailure(anyLong(), anyLong());
        } finally {
            qSender.close();

            qReceiver.close();
        }
    }

    @Test
    public void sendMessageToRouterTopicTest() throws Exception {

        System.out.println("start to execute sendMessageToRouterTopicTest");
        
        String routerSource = "PerfRouteToTopic";
        String routerTarget = "PerfVirtualTopic";

        ClientDestination dest = connector.getConfigService().fetchDestination(routerTarget);
        ClientVirtualTopic topic = null;
        Map<String, Long> beginSizeCount = new HashMap<String, Long>();

        if (dest instanceof ClientVirtualTopic) {
            topic = (ClientVirtualTopic) dest;
            if (topic.getClientQueueList().size() == 0) {
                fail("expect routerTarget " + routerTarget + " be a topic with queues, but it's empty");
            }
        } else {
            fail("expect routerTarget " + routerTarget + " be a topic, but it's not.");
        }

        // clear router target's queues' message to avoid test impact.
        for (ClientQueue queue : topic.getClientQueueList()) {
            String name = queue.getName();
            JmxUtil.deleteAllMessage(connector.getAddress(), name);
            beginSizeCount.put(name, JmxUtil.getEnQueue(connector.getAddress(), name));
        }

        JmxUtil.deleteAllMessage(connector.getAddress(), routerSource);

        qSender = new DefaultAsyncSender();
        qSender.setConnector(connector);
        qSender.setName(routerSource);

        qSender.setStoreEnable(true);
        qSender.setReprocessInterval(10000 * 1000 * 1000);

        qSender.init();

        DefaultAsyncReceiver qReceiver = new DefaultAsyncReceiver();
        try {
            MyWorker qWorker = new MyWorker();
            qReceiver.setConnector(connector);
            qReceiver.setStoreEnable(true);
            qReceiver.setInstances(20);
            qReceiver.setName(topic.getClientQueueList().get(0).getName());
            qReceiver.setWorker(qWorker);
            qReceiver.init();
            qReceiver.start();

            long beginQueueSizeSource = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), routerSource);

            long beginQueueSizeTargetQueue0 = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), qReceiver.getName());

            int tc = 15;
            System.out.println("yanny requestcount = " + System.getProperty("NapoliRouterTest.requestCount"));
            final int tp = Integer.parseInt(System.getProperty("NapoliRouterTest.requestCount", "20"));
            final Semaphore semaphore = new Semaphore(tc);
            final AtomicInteger sumCount = new AtomicInteger();

            final AtomicInteger requestCount = new AtomicInteger();
            long startTime = System.currentTimeMillis();
            System.out.println("Yanny start send request " + startTime);

            long startCountSource = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), routerSource);

            for (int i = 0; i < tc; i++) {
                Thread t = new Thread("thread--" + i) {
                    public void run() {
                        try {
                            // 构造消息，消息可以是对象、String、Map等，消息对象必须实现Serializable接口
                            semaphore.acquire();
                            Person person = new Person();

                            person.setLoginName("superman");
                            person.setEmail("sm@1.com");
                            person.setPenName("pname");
                            person.setStatus(PersonStatus.ENABLED);

                            for (int j = 0; j < tp; j++) {
                                // System.out.println("hello");
                                int id = requestCount.incrementAndGet();
                                person.setPersonId("" + id);

                                // 建议处理返回值, 发送成功返回true，否则返回false
                                boolean result = qSender.send(person);
                                if (!result) {
                                    System.out.println("----------------send to queue " + "result is false. personid="
                                            + j);
                                } else {
                                    sumCount.incrementAndGet();
                                }
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        } finally {
                            semaphore.release();
                        }
                    }
                };
                t.start();
            }

            while (semaphore.availablePermits() != tc) {
                Thread.sleep(100);
            }
            int totalRequest = tc * tp;

            long endTime = System.currentTimeMillis();
            System.out.println("yanny: send " + totalRequest + " message, take " + (endTime - startTime)
                    + " milseconds");

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), routerSource, beginQueueSizeSource);
            endTime = System.currentTimeMillis();
            System.out.println("yanny: router finished  send " + totalRequest + " message, succeed  " + sumCount.get()
                    + " message, take " + (endTime - startTime) + " milseconds");

            long endCountSource = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), routerSource);

            String errorMessage = "";

            for (ClientQueue queue : topic.getClientQueueList()) {
                String name = queue.getName();
                long endCount = JmxUtil.getEnQueue(connector.getAddress(), name);
                if ((endCount - beginSizeCount.get(name)) > (endCountSource - startCountSource) * 1.001) {
                    // 路由topic的每个队列收到地消息理论上应该部队与路由source收到的消息,考虑路由转发时,可能发送返回失败,但是实际成功,给0.1%的buffer
                    errorMessage += ";queue " + name + " recieved message " + (endCount - beginSizeCount.get(name))
                            + " larger than router source received message " + (endCountSource - startCountSource)
                            + "*1.001, message duplicate too much";
                } else if ((endCount - beginSizeCount.get(name)) < (endCountSource - startCountSource)) {
                    // 路由topic的每个队列收到地消息理论上应该部队与路由source收到的消息,考虑路由转发时,可能发送返回失败,但是实际成功,给0.1%的buffer
                    errorMessage += ";queue " + name + " recieved message " + (endCount - beginSizeCount.get(name))
                            + " less than router source received message " + (endCountSource - startCountSource)
                            + ", message lost";
                }
            }

            assertTrue(errorMessage, errorMessage.equals(""));

        } finally {

            qSender.close();
            qReceiver.close();
            for (ClientQueue queue : topic.getClientQueueList()) {
                String name = queue.getName();
                JmxUtil.deleteAllMessage(connector.getAddress(), name);
            }
        }
    }

    public static void main(String[] args) {
        try {

            NapoliRouterTest test = new NapoliRouterTest();
            NapoliRouterTest.init();

            test.sendMessageToRouterQueueTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
