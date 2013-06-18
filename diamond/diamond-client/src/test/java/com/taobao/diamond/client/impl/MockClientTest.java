package com.taobao.diamond.client.impl;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.client.DiamondSubscriber;
import com.taobao.diamond.client.impl.DefaultDiamondSubscriber.Builder;
import com.taobao.diamond.mockserver.MockServer;


public class MockClientTest {
    private DiamondSubscriber diamondSubscriber = null;
    private final TestSubscriberListener listener = new TestSubscriberListener();


    @Before
    public void setUp() {
        MockServer.setUpMockServer();
        MockServer.setConfigInfo("Test_DataId", "Value:Test_DataId");
        DiamondConfigure config = new DiamondConfigure("diamond");
        config.setPollingIntervalTime(1);
        diamondSubscriber = new Builder(listener).addDataId("Test_DataId").setDiamondConfigure(config).build();
        diamondSubscriber.start();
    }


    @After
    public void tearDown() {
        diamondSubscriber.close();
        MockServer.tearDownMockServer();
    }


    @Test
    public void test1() throws Exception {
        while (!listener.getUpdateConfigInfo()) {
            Thread.sleep(100);
        }
        listener.setUpdateConfigInfo(false);
        Assert.assertEquals("Value:Test_DataId", listener.getConfigureInfomation());
        MockServer.setConfigInfo("Test_DataId", "Value:Test_DataId2");
        while (!listener.getUpdateConfigInfo()) {
            Thread.sleep(100);
        }
        Assert.assertEquals("Value:Test_DataId2", listener.getConfigureInfomation());
    }


    @Test
    public void test2() throws Exception {
        Assert.assertEquals("Value:Test_DataId", diamondSubscriber.getConfigureInfomation("Test_DataId", null, 1000));
        MockServer.setConfigInfo("Test_DataId", "Value:Test_DataId2");
        Assert.assertEquals("Value:Test_DataId2", diamondSubscriber.getConfigureInfomation("Test_DataId", 1000));
        MockServer.setConfigInfo("Test_DataId", "Value:Test_DataId3");
        Assert.assertEquals("Value:Test_DataId3", diamondSubscriber.getConfigureInfomation("Test_DataId", 1000));
        MockServer.setConfigInfo("Test_DataId", "Value:Test_DataId4");
        Assert.assertEquals("Value:Test_DataId4", diamondSubscriber.getConfigureInfomation("Test_DataId", 1000));
    }


    public void test3() throws Exception {
        while (true) {
            diamondSubscriber.getConfigureInfomation("Test_DataId", 1000);
        }
    }
}
