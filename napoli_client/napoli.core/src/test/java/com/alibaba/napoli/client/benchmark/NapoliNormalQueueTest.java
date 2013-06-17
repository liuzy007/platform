/**
 * Project: napoli.client
 *
 * File Created at 2011-9-23
 * $Id: NapoliNormalQueueTest.java 191497 2012-08-01 06:16:59Z yanny.wangyy $
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
import com.alibaba.dragoon.stat.napoli.NapoliReceiverStat;
import com.alibaba.dragoon.stat.napoli.NapoliSenderStat;
import com.alibaba.dragoon.stat.napoli.NapoliStatManager;
import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.model.MyWorker;
import com.alibaba.napoli.client.model.Person;
import com.alibaba.napoli.client.model.PersonStatus;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.common.util.JmxUtil;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.receiver.impl.ReceiverMonitorFilter;
import com.alibaba.napoli.sender.impl.SenderMonitorFilter;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test router module, RouterTest_Source is configed to router to
 * RouterTest_Target on another amq machine.
 * 
 * @author yanny.wangyy
 */
public class NapoliNormalQueueTest {
    private static final Log              log       = LogFactory.getLog("NapoliNormalQueueTest");

    protected static NapoliConnector      sendConnector;
    protected static ConsoleConnector     receiverConnector;
    protected static DefaultAsyncSender   qSender;
    protected static DefaultAsyncReceiver qReceiver;

    protected static MyWorker             qWorker   = new MyWorker();

    protected static NapoliStatManager    napoliStatManager;
    protected static NapoliSenderStat     napoliSenderStat;
    protected static NapoliReceiverStat   napoliReceiverStat;

    protected static String               initConsumeMessage;

    protected static String               queueName = "NapoliNormalQueueTest_" + System.currentTimeMillis();

    @BeforeClass
    public static void init() throws Exception {
        DragoonClient.setJdbcStatEnable(false);
        DragoonClient.setSpringStatEnable(false);
        DragoonClient.setUriStatEnable(false);
        DragoonClient.setLog4jStatEnable(true); //鐢ㄤ簬Exception鐩戞帶椤圭殑閲囬泦
        DragoonClient.start("NapoliNormalQueueTest"); // 鍙傛暟锛氬簲鐢ㄥ悕绉�

        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName,
                NapoliTestUtil.getProperty("napoli.func.mixGroupName"));
        log.info("NapoliNormalQueueTest with queueName=" + queueName);

        String storePath = NapoliTestUtil.getProperty("napoli.func.storePath") + System.currentTimeMillis();
        NapoliTestUtil.delFiles(storePath);

        sendConnector = new NapoliConnector();
        sendConnector.setAddress(NapoliTestUtil.getAddress());
        sendConnector.setStorePath(storePath);
        sendConnector.setJmsUserName(NapoliTestUtil.getUser());
        sendConnector.setJmsPassword(NapoliTestUtil.getPasswd());
        sendConnector.setInterval(6000);
        sendConnector.setPoolSize(25);
        sendConnector.setSendTimeout(1000);
        sendConnector.init();

        receiverConnector = new ConsoleConnector();
        receiverConnector.setAddress(NapoliTestUtil.getAddress());
        receiverConnector.setStorePath(storePath);
        receiverConnector.setJmsUserName(NapoliTestUtil.getUser());
        receiverConnector.setJmsPassword(NapoliTestUtil.getPasswd());
        receiverConnector.setInterval(6000);
        receiverConnector.setPoolSize(25);
        // sendConnector.setSendTimeout(1000);
        receiverConnector.init();

        qReceiver = new DefaultAsyncReceiver();
        qWorker.logEnabled = true;
        qReceiver.setConnector(sendConnector);
        qReceiver.setStoreEnable(true);
        qReceiver.setInstances(20);
        qReceiver.setName(queueName);
        qReceiver.setWorker(qWorker);
        qReceiver.init();
        qReceiver.start();

        log.info("console is " + NapoliTestUtil.getAddress());

        napoliStatManager = mock(NapoliStatManager.class);

        SenderMonitorFilter.setMonitor(napoliStatManager);
        ReceiverMonitorFilter.setMonitor(napoliStatManager);
    }

    @Before
    public void setup() {

        napoliSenderStat = mock(NapoliSenderStat.class);
        napoliReceiverStat = mock(NapoliReceiverStat.class);
        when(napoliStatManager.getSenderStat(anyString(), anyString())).thenReturn(napoliSenderStat);

        when(napoliStatManager.getReceiverStat(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(napoliReceiverStat);

        JmxUtil.deleteAllMessage(NapoliTestUtil.getAddress(), queueName);
        qWorker.reset();

        qSender = new DefaultAsyncSender();
        qSender.setConnector(sendConnector);
        qSender.setName(queueName);

        qSender.setStoreEnable(true);
        try {
            qSender.init();
        } catch (NapoliClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        KVStore store = sendConnector.getSenderKVStore(queueName);
        if (store != null) {
            store.clear();
        }

        qSender.close();
    }

    @AfterClass
    public static void dispose() {
        log.info("yanny AfterClass is executed " + System.currentTimeMillis());
        if (qReceiver != null && qReceiver.isStarted()) {
            qReceiver.close();
        }

        if (qSender != null && qSender.isStarted()) {
            qSender.close();
        }

        ConsoleConnector.closeAll();

        HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName);
    }

    @Test
    public void sendMessageWithSenderStoreEnableTest() throws Exception {

        log.info("start to execute sendMessageWithSenderStoreEnableTest");
        long beginQueueSize = JmxUtil.getQueueSize(sendConnector.getAddress(), queueName);

        qSender = new DefaultAsyncSender();
        qSender.setConnector(sendConnector);
        qSender.setName(queueName);

        qSender.setStoreEnable(true);
        qSender.setReprocessInterval(10000 * 1000 * 1000);

        qSender.init();

        int tc = 10;

        log.info("yanny requestcount = " + System.getProperty("requestCount") + ", begin queue size is "
                + beginQueueSize);
        final int tp = Integer.parseInt(System.getProperty("requestCount", "20"));
        final Semaphore semaphore = new Semaphore(tc);
        final AtomicInteger sumCount = new AtomicInteger();

        final AtomicInteger requestCount = new AtomicInteger();
        long startTime = System.currentTimeMillis();
        log.info("Yanny start send request " + startTime);

        for (int i = 0; i < tc; i++) {
            Thread t = new Thread("thread--" + i) {
                public void run() {
                    try {
                        //鏋勯�娑堟伅锛屾秷鎭彲浠ユ槸瀵硅薄銆丼tring銆丮ap绛夛紝娑堟伅瀵硅薄蹇呴』瀹炵幇Serializable鎺ュ彛
                        semaphore.acquire();
                        Person person = new Person();

                        person.setLoginName("superman");
                        person.setEmail("sm@1.com");
                        person.setPenName("pname");
                        person.setStatus(PersonStatus.ENABLED);

                        for (int j = 0; j < tp; j++) {
                            //      log.info("hello");
                            int id = requestCount.incrementAndGet();
                            person.setPersonId("" + id);

                            //寤鸿澶勭悊杩斿洖鍊� 鍙戦�鎴愬姛杩斿洖true锛屽惁鍒欒繑鍥瀎alse                        
                            boolean result = qSender.send(person);
                            if (!result) {
                                log.info("----------------send to queue " + "result is false. personid=" + j);
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
        log.info("yanny: send " + totalRequest + " message, take " + (endTime - startTime) + " milseconds");

        JmxUtil.waitTillQueueSizeAsTarget(sendConnector.getAddress(), queueName, beginQueueSize);

        endTime = System.currentTimeMillis();

        String errorMessage = "";

        long qBdbCount = NapoliTestUtil.getStoreSize(sendConnector.getSenderKVStore(qSender.getName()));

        log.info("yanny totalRequest " + totalRequest + " send queue success " + sumCount + " local store count:"
                + qBdbCount + " queue received " + qWorker.getAccessNum() + " take " + (endTime - startTime)
                + " milseconds");

        log.info(initConsumeMessage);

        log.info("NapoliNormalQueueTest's success=" + qWorker.getAccessNum() + " bdb's size=" + qBdbCount);

        //with store enabled, all send should succeed.
        if (qSender.getStoreEnable()) {
            if (sumCount.get() != totalRequest) {
                errorMessage += ";with store enabled, all send should return success, but not equal now. send succeed "
                        + sumCount.get() + "; total request:" + totalRequest;
            }
        } else {
            if (sumCount.get() < totalRequest * 0.95) {
                errorMessage += ";with store disabled, expected more than 95% message send succeed, total request:"
                        + totalRequest + "; send succeed " + sumCount.get();
            }
        }

        if (sumCount.get() < qWorker.getAccessNum()) {
            errorMessage += ";queue should not have success messages more than send succeed" + sumCount.get()
                    + " (success " + qWorker.getAccessNum() + ")";
        }

        if ((sumCount.get() - qBdbCount) > qWorker.getAccessNum()) {
            errorMessage += ";queue received message (" + qWorker.getAccessNum()
                    + ") less than send succeed - local stored message, message lost " + (sumCount.get() - qBdbCount);
        }

        int allowedDiff = (int) Math.round(sumCount.get() * 0.001);

        if (((qWorker.getAccessNum() + qBdbCount) - sumCount.get()) > allowedDiff) {
            errorMessage += "queue received message should not have more than send succeed + " + allowedDiff
                    + " than allowed (0.1%), gap " + ((qWorker.getAccessNum() + qBdbCount) - sumCount.get());
        }

        assertTrue(errorMessage, errorMessage.equals(""));

        verify(napoliSenderStat, atMost(qWorker.getAccessNum())).sendSuccess(anyLong(), anyLong());
        verify(napoliSenderStat, atLeast((int) (sumCount.get() - qBdbCount))).sendSuccess(anyLong(), anyLong());
        verify(napoliSenderStat, times((int) qBdbCount)).sendFailure(anyLong(), anyLong());

        verify(napoliReceiverStat, times((int) qWorker.getAccessNum())).receiveSuccess(anyLong(), anyLong());
    }

    @Test
    public void sendMessageWithSenderStoreDisabledTest() throws Exception {

        log.info("start to execute sendMessageWithSenderStoreDisabledTest");

        long beginQueueSize = JmxUtil.getQueueSize(sendConnector.getAddress(), queueName);

        qSender = new DefaultAsyncSender();
        qSender.setConnector(sendConnector);
        qSender.setName(queueName);

        qSender.setStoreEnable(false);
        qSender.setReprocessInterval(10000 * 1000 * 1000);

        qSender.init();

        int tc = 10;
        log.info("yanny requestcount = " + System.getProperty("requestCount"));
        final int tp = Integer.parseInt(System.getProperty("requestCount", "20"));
        final Semaphore semaphore = new Semaphore(tc);
        final AtomicInteger sumCount = new AtomicInteger();

        final AtomicInteger requestCount = new AtomicInteger();
        long startTime = System.currentTimeMillis();
        log.info("Yanny start send request " + startTime);

        for (int i = 0; i < tc; i++) {
            Thread t = new Thread("thread--" + i) {
                public void run() {
                    try {
                        //鏋勯�娑堟伅锛屾秷鎭彲浠ユ槸瀵硅薄銆丼tring銆丮ap绛夛紝娑堟伅瀵硅薄蹇呴』瀹炵幇Serializable鎺ュ彛
                        semaphore.acquire();
                        Person person = new Person();

                        person.setLoginName("superman");
                        person.setEmail("sm@1.com");
                        person.setPenName("pname");
                        person.setStatus(PersonStatus.ENABLED);

                        for (int j = 0; j < tp; j++) {
                            //      log.info("hello");
                            int id = requestCount.incrementAndGet();
                            person.setPersonId("" + id);

                            //寤鸿澶勭悊杩斿洖鍊� 鍙戦�鎴愬姛杩斿洖true锛屽惁鍒欒繑鍥瀎alse                        
                            boolean result = qSender.send(person);
                            if (!result) {
                                log.info("----------------send to queue " + "result is false. personid=" + j);
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
        log.info("yanny: send " + totalRequest + " message, take " + (endTime - startTime) + " milseconds");

        JmxUtil.waitTillQueueSizeAsTarget(sendConnector.getAddress(), queueName, beginQueueSize);

        endTime = System.currentTimeMillis();

        String errorMessage = "";

        long qBdbCount = NapoliTestUtil.getStoreSize(sendConnector.getSenderKVStore(qSender.getName()));
        log.info("yanny totalRequest " + totalRequest + " send queue success " + sumCount + " local store count:"
                + qBdbCount + " queue received " + qWorker.getAccessNum() + " take " + (endTime - startTime)
                + " milseconds");

        log.info(initConsumeMessage);

        log.info("NapoliNormalQueueTest's success=" + qWorker.getAccessNum() + " bdb's size=" + qBdbCount);

        if (qBdbCount > 0) {
            errorMessage += ";with store disabled, local store count should be empty, but is " + qBdbCount;
        }

        //with store enabled, all send should succeed.
        if (qSender.getStoreEnable()) {
            if (sumCount.get() != totalRequest) {
                errorMessage += ";with store enabled, all send should return success, but not equal now. send succeed "
                        + sumCount.get() + "; total request:" + totalRequest;
            }
        } else {
            if (sumCount.get() < totalRequest * 0.95) {
                errorMessage += ";with store disabled, expected more than 95% message send succeed, total request:"
                        + totalRequest + "; send succeed " + sumCount.get();
            }
        }

        //鍙戦�杩斿洖澶辫触锛屼絾鏄疄闄呭彲鑳芥垚鍔�鎵�互娑堣垂搴旇涓嶅浜巘otalRequest,浣嗘槸鍙兘>=sum
        if (totalRequest < qWorker.getAccessNum()) {
            errorMessage += ";queue should not have success messages more than send succeed" + sumCount.get()
                    + " (success " + qWorker.getAccessNum() + ")";
        }

        //姝ゆ椂qBdbCount搴旇涓�銆�
        if ((sumCount.get() - qBdbCount) > qWorker.getAccessNum()) {
            errorMessage += ";queue received message (" + qWorker.getAccessNum()
                    + ") less than send succeed - local stored message, message lost " + (sumCount.get() - qBdbCount);
        }

        int allowedDiff = (int) Math.round(sumCount.get() * 0.001);

        if (((qWorker.getAccessNum() + qBdbCount) - sumCount.get()) > allowedDiff) {
            errorMessage += "queue received message should not have more than send succeed + " + allowedDiff
                    + " than allowed (0.1%), gap " + ((qWorker.getAccessNum() + qBdbCount) - sumCount.get());
        }

        assertTrue(errorMessage, errorMessage.equals(""));

        verify(napoliSenderStat, atMost(qWorker.getAccessNum())).sendSuccess(anyLong(), anyLong());
        verify(napoliSenderStat, atLeast((int) (sumCount.get() - qBdbCount))).sendSuccess(anyLong(), anyLong());
        verify(napoliSenderStat, times(totalRequest - sumCount.get())).sendFalse(anyLong(), anyLong());

        verify(napoliSenderStat, times((int) qBdbCount)).sendFailure(anyLong(), anyLong());

        verify(napoliReceiverStat, times((int) qWorker.getAccessNum())).receiveSuccess(anyLong(), anyLong());
    }

    public static void main(String[] args) {
        try {
            NapoliNormalQueueTest test = new NapoliNormalQueueTest();
            NapoliNormalQueueTest.init();

            try {
                test.setup();
                test.sendMessageWithSenderStoreEnableTest();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                test.setup();
                test.sendMessageWithSenderStoreDisabledTest();
            } catch (Exception e) {
                e.printStackTrace();
            }

            NapoliNormalQueueTest.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
