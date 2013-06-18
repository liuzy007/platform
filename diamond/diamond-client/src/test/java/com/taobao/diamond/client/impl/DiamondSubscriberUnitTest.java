package com.taobao.diamond.client.impl;

import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.client.DiamondSubscriber;
import com.taobao.diamond.mockserver.MockServer;


public class DiamondSubscriberUnitTest {
    DiamondSubscriber diamondSubscriber = null;
    TestSubscriberListener defaultListener = new TestSubscriberListener();


    @Before
    public void setUp() {
        MockServer.setUpMockServer();
        diamondSubscriber = new DefaultDiamondSubscriber(defaultListener);
    }


    @After
    public void tearDown() {
        diamondSubscriber.close();
        MockServer.tearDownMockServer();
    }


    @Test
    public void diamondConfigureTest() {
        DiamondConfigure configure = new DiamondConfigure("diamond");
        diamondSubscriber.setDiamondConfigure(configure);
        Assert.assertEquals(diamondSubscriber.getDiamondConfigure(), configure);
    }


    @Test
    public void subscriberListenerTest() {
        TestSubscriberListener listener = new TestSubscriberListener();
        diamondSubscriber.setSubscriberListener(listener);
        Assert.assertEquals(diamondSubscriber.getSubscriberListener(), listener);
        Assert.assertNotSame(diamondSubscriber.getSubscriberListener(), defaultListener);
    }


    @Test
    public void dataIdTest1() {
        diamondSubscriber.addDataId("dataId1", null);
        diamondSubscriber.addDataId("dataId2", null);
        diamondSubscriber.addDataId("dataId3", null);
        diamondSubscriber.addDataId("dataId1", null);
        Set<String> dataIds1 = diamondSubscriber.getDataIds();
        Assert.assertEquals(3, dataIds1.size());
        Assert.assertTrue(dataIds1.contains("dataId1"));
        Assert.assertTrue(dataIds1.contains("dataId2"));
        Assert.assertTrue(dataIds1.contains("dataId3"));
        diamondSubscriber.clearAllDataIds();
        Set<String> dataIds2 = diamondSubscriber.getDataIds();
        Assert.assertEquals(0, dataIds2.size());
    }


    @Test
    public void dataIdTest2() {
        diamondSubscriber.addDataId("dataId1", null);
        diamondSubscriber.addDataId("dataId2", null);
        diamondSubscriber.addDataId("dataId3", null);
        diamondSubscriber.addDataId("dataId1", null);
        Set<String> dataIds1 = diamondSubscriber.getDataIds();
        Assert.assertEquals(3, dataIds1.size());
        Assert.assertTrue(dataIds1.contains("dataId1"));
        Assert.assertTrue(dataIds1.contains("dataId2"));
        Assert.assertTrue(dataIds1.contains("dataId3"));

        diamondSubscriber.removeDataId("dataId1");
        Set<String> dataIds2 = diamondSubscriber.getDataIds();
        Assert.assertEquals(3, dataIds2.size());
        Assert.assertTrue(dataIds2.contains("dataId1"));
        Assert.assertTrue(dataIds2.contains("dataId2"));
        Assert.assertTrue(dataIds2.contains("dataId3"));

        diamondSubscriber.clearAllDataIds();
        Set<String> dataIds4 = diamondSubscriber.getDataIds();
        Assert.assertEquals(0, dataIds4.size());
    }
}
