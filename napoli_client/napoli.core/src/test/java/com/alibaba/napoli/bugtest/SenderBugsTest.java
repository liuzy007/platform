/**
 * Project: napoli.client.refactor
 * 
 * File Created at 2011-12-16
 * $Id: SenderBugsTest.java 191497 2012-08-01 06:16:59Z yanny.wangyy $
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
package com.alibaba.napoli.bugtest;

import static org.junit.Assert.*;

import com.alibaba.napoli.client.connector.NapoliConnector;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.connector.ConsoleConnector;

/**
 * TODO Comment of SenderBugs
 * 
 * @author yanny.wangyy
 */
public class SenderBugsTest {
    private static final Log log = LogFactory.getLog("SenderBugsTest");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
      ConsoleConnector.closeAll();
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void stop() throws NapoliClientException {
    }

    @Test
    public void testBugNP193() throws Exception {

        String queueName = "NapoliEmptyQueue_" + System.currentTimeMillis();
        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName, "");
        log.info("testBugNP193 with queueName=" + queueName);

        NapoliConnector connector;
        DefaultAsyncSender sender;

        DefaultAsyncSender sender2;

        connector = new NapoliConnector();
        connector.setAddress(NapoliTestUtil.getAddress());
        connector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath") + "\\" + new Date().hashCode());
        connector.setPoolSize(5);
        connector.setPrefetch(100);
        connector.init();

        sender = new DefaultAsyncSender();
        sender.setConnector(connector);
        sender.setName(queueName);
        sender.setStoreEnable(true);

        sender2 = new DefaultAsyncSender();
        sender2.setConnector(connector);
        sender2.setName(queueName);
        sender2.setStoreEnable(true);

        try {

            //need to verify the queue is not associated with any machine. otherwise, test is meaningless.
           /* ArrayList<PhysicalQueue> pQueues = NapoliMessageUtil.getPhysicalQueues(NapoliTestUtil.getAddress(),
                    queueName);
*/
            /*if (pQueues.size() > 0) {
                throw new Exception("Queue " + queueName + " has physical queues, make test meaningless");
                //TODO:
                //update test code to remove all physical queues and wait for 1 minutes instead of throw exception.                
            }*/

            sender.init();
            sender2.init();

            if (!sender.send("Message 1 from sender")) {
                fail("send message failed");
            }

            if (!sender2.send("Message 2 from sender2")) {
                fail("sender2 send message failed");
            }

            sender.close();

            boolean result = sender2.send("Message 3 from sender2");
            assertEquals("send message 3 from sender2 should succeed even sender1 is closed, but it return false",
                    true, result);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test testBugNP193 got exception " + e.getMessage());
        } finally {            
           
            if (!sender.isClosed()) {
                sender.close();
            }
            if (!sender2.isClosed()) {
                sender2.close();
            }
           
            connector.close();
            
            HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName);            
        }
    }
}
