package com.taobao.diamond.client.impl;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.impl.DefaultDiamondManager;
import com.taobao.diamond.mockserver.MockServer;


public class MockManagerTest {
    private DiamondManager diamondManager = null;
    private TestManagerListener listener = new TestManagerListener();


    @Before
    public void setUp() {
        MockServer.setUpMockServer();
        Map<String, String> configInfos = new HashMap<String, String>();
        configInfos.put("Test_DataId", "Value:Test_DataId");
        MockServer.setConfigInfos(configInfos);
        MockServer.setConfigInfo("Test_DataId", "Group1", "Value:Test_DataId");
        DiamondConfigure config = new DiamondConfigure("diamond");
        config.setPollingIntervalTime(2);
        diamondManager = new DefaultDiamondManager.Builder("Test_DataId", listener).setDiamondConfigure(config).build();
    }


    @After
    public void tearDown() {
        diamondManager.close();
        DiamondClientFactory.getSingletonDiamondSubscriber("diamond").close();
        MockServer.tearDownMockServer();
    }


    @Test
    public void test1() throws Exception {
        while (!listener.getUpdateConfigInfo()) {
            Thread.sleep(100);
        }
        listener.setUpdateConfigInfo(false);
        Assert.assertEquals("Value:Test_DataId", listener.getConfigInfo());

        MockServer.setConfigInfo("Test_DataId", "Value:Test_DataId2");

        while (!listener.getUpdateConfigInfo()) {
            Thread.sleep(100);
        }
        Assert.assertEquals("Value:Test_DataId2", listener.getConfigInfo());
    }


    @Test
    public void test2() throws Exception {
        Assert.assertEquals("Value:Test_DataId", diamondManager.getConfigureInfomation(1000));
        MockServer.setConfigInfo("Test_DataId", "Value:Test_DataId2");
        Assert.assertEquals("Value:Test_DataId2", diamondManager.getConfigureInfomation(1000));
    }


    /**
     * 测试两个DiamondManager公用一个DataID，但是Group不同
     * 
     * @throws Exception
     */
    @Test
    public void test3() throws Exception {
        TestManagerListener another_listener = new TestManagerListener();
        DiamondManager another_diamondManager =
                new DefaultDiamondManager.Builder("Test_DataId", another_listener).setGroup("Group1").build();

        while (!another_listener.getUpdateConfigInfo()) {
            Thread.sleep(100);
        }
        another_listener.setUpdateConfigInfo(false);
        Assert.assertEquals("Value:Test_DataId", another_listener.getConfigInfo());

        MockServer.setConfigInfo("Test_DataId", "Group1", "Value:Test_DataId2");

        while (!another_listener.getUpdateConfigInfo()) {
            Thread.sleep(100);
        }
        Assert.assertEquals("Value:Test_DataId2", another_listener.getConfigInfo());
        another_diamondManager.close();
    }


    /**
     * 测试两个DiamondManager公用一个DataID和Group
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void test4() throws Exception {
        TestManagerListener another_listener = new TestManagerListener();
        DiamondManager another_diamondManager =
                new DefaultDiamondManager.Builder("Test_DataId", another_listener).build();
        while (!another_listener.getUpdateConfigInfo()) {
            Thread.sleep(100);
        }
        another_listener.setUpdateConfigInfo(false);
        Assert.assertEquals("Value:Test_DataId", another_listener.getConfigInfo());

        while (!listener.getUpdateConfigInfo()) {
            Thread.sleep(100);
        }
        listener.setUpdateConfigInfo(false);
        Assert.assertEquals("Value:Test_DataId", listener.getConfigInfo());

        MockServer.setConfigInfo("Test_DataId", "Value:Test_DataId2");

        while (!another_listener.getUpdateConfigInfo()) {
            Thread.sleep(100);
        }
        Assert.assertEquals("Value:Test_DataId2", another_listener.getConfigInfo());
        another_diamondManager.close();

        while (!listener.getUpdateConfigInfo()) {
            Thread.sleep(100);
        }
        Assert.assertEquals("Value:Test_DataId2", listener.getConfigInfo());
    }
}
