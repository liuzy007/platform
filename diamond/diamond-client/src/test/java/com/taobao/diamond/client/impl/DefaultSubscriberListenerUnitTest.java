package com.taobao.diamond.client.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.junit.Assert;
import org.junit.Test;

import com.taobao.diamond.configinfo.ConfigureInfomation;
import com.taobao.diamond.manager.ManagerListener;


public class DefaultSubscriberListenerUnitTest {

    private static class TestManagerListener implements ManagerListener {

        private Map<Integer, Boolean> result;
        private int index;


        public TestManagerListener(int index, Map<Integer, Boolean> result) {
            this.index = index;
            this.result = result;
            result.put(index, false);
        }


        public Executor getExecutor() {
            return null;
        }


        public void receiveConfigInfo(String configInfo) {
            System.out.println("index:" + index + "," + configInfo);
            result.put(index, true);

        }
    }


    @Test
    public void testDefaultGroup() {
        Map<Integer, Boolean> result = new HashMap<Integer, Boolean>();

        DefaultSubscriberListener listener = new DefaultSubscriberListener();

        listener.addManagerListener("dataId1", null, "instanceId", new TestManagerListener(1, result));
        listener.addManagerListener("dataId1", null, "instanceId", new TestManagerListener(2, result));
        listener.addManagerListener("dataId1", null, "instanceId2", new TestManagerListener(3, result));
        listener.addManagerListener("dataId2", null, "instanceId", new TestManagerListener(4, result));
        listener.addManagerListener("dataId3", null, "instanceId", new TestManagerListener(5, result));

        ConfigureInfomation info = new ConfigureInfomation();
        info.setConfigureInfomation("test" + System.currentTimeMillis());
        info.setDataId("dataId1");

        listener.receiveConfigInfo(info);

        Assert.assertTrue(result.get(1));
        Assert.assertTrue(result.get(2));
        Assert.assertTrue(result.get(3));
        Assert.assertFalse(result.get(4));
        Assert.assertFalse(result.get(5));

    }


    @Test
    public void testDeferentGroup() {
        Map<Integer, Boolean> result = new HashMap<Integer, Boolean>();

        DefaultSubscriberListener listener = new DefaultSubscriberListener();

        listener.addManagerListener("dataId1", "g1", "instanceId", new TestManagerListener(1, result));
        listener.addManagerListener("dataId1", "g1", "instanceId", new TestManagerListener(2, result));
        listener.addManagerListener("dataId1", "g1", "instanceId2", new TestManagerListener(3, result));
        listener.addManagerListener("dataId2", "g1", "instanceId", new TestManagerListener(4, result));
        listener.addManagerListener("dataId2", "g2", "instanceId", new TestManagerListener(5, result));
        listener.addManagerListener("dataId3", null, "instanceId", new TestManagerListener(6, result));

        ConfigureInfomation info = new ConfigureInfomation();
        info.setConfigureInfomation("test" + System.currentTimeMillis());
        info.setDataId("dataId1");
        info.setGroup("g1");

        listener.receiveConfigInfo(info);

        Assert.assertTrue(result.get(1));
        Assert.assertTrue(result.get(2));
        Assert.assertTrue(result.get(3));
        Assert.assertFalse(result.get(4));
        Assert.assertFalse(result.get(5));
        Assert.assertFalse(result.get(6));

    }


    @Test
    public void testListenerList() {
        Map<Integer, Boolean> result = new HashMap<Integer, Boolean>();

        DefaultSubscriberListener listener = new DefaultSubscriberListener();

        List<ManagerListener> list = new ArrayList<ManagerListener>();
        list.add(new TestManagerListener(1, result));
        list.add(new TestManagerListener(2, result));
        listener.addManagerListeners("dataId1", "g1", "instanceId", list);

        list = new ArrayList<ManagerListener>();
        list.add(new TestManagerListener(3, result));
        list.add(new TestManagerListener(4, result));
        listener.addManagerListeners("dataId2", "g1", "instanceId", list);

        listener.addManagerListener("dataId1", "g2", "instanceId", new TestManagerListener(5, result));
        listener.addManagerListener("dataId3", null, "instanceId", new TestManagerListener(6, result));

        ConfigureInfomation info = new ConfigureInfomation();
        info.setConfigureInfomation("test" + System.currentTimeMillis());
        info.setDataId("dataId1");
        info.setGroup("g1");

        listener.receiveConfigInfo(info);

        Assert.assertTrue(result.get(1));
        Assert.assertTrue(result.get(2));
        Assert.assertFalse(result.get(3));
        Assert.assertFalse(result.get(4));
        Assert.assertFalse(result.get(5));
        Assert.assertFalse(result.get(6));

    }


    @Test
    public void testRemoveListener() {
        Map<Integer, Boolean> result = new HashMap<Integer, Boolean>();

        DefaultSubscriberListener listener = new DefaultSubscriberListener();

        listener.addManagerListener("dataId1", "g1", "instanceId", new TestManagerListener(1, result));
        listener.addManagerListener("dataId1", "g1", "instanceId", new TestManagerListener(2, result));
        listener.addManagerListener("dataId1", "g2", "instanceId", new TestManagerListener(3, result));
        listener.addManagerListener("dataId2", "g1", "instanceId", new TestManagerListener(4, result));
        listener.addManagerListener("dataId2", "g2", "instanceId", new TestManagerListener(5, result));
        listener.addManagerListener("dataId3", null, "instanceId", new TestManagerListener(6, result));
        listener.addManagerListener("dataId1", "g1", "instanceId2", new TestManagerListener(7, result));

        listener.removeManagerListeners("dataId1", "g1", "instanceId");

        ConfigureInfomation info = new ConfigureInfomation();
        info.setConfigureInfomation("test" + System.currentTimeMillis());
        info.setDataId("dataId1");
        info.setGroup("g1");

        listener.receiveConfigInfo(info);

        Assert.assertFalse(result.get(1));
        Assert.assertFalse(result.get(2));
        Assert.assertFalse(result.get(3));
        Assert.assertFalse(result.get(4));
        Assert.assertFalse(result.get(5));
        Assert.assertFalse(result.get(6));
        Assert.assertTrue(result.get(7));

    }


    @Test
    public void testGetListeners() {
        Map<Integer, Boolean> result = new HashMap<Integer, Boolean>();

        DefaultSubscriberListener listener = new DefaultSubscriberListener();

        listener.addManagerListener("dataId1", "g1", "instanceId", new TestManagerListener(1, result));
        listener.addManagerListener("dataId1", "g1", "instanceId", new TestManagerListener(2, result));
        listener.addManagerListener("dataId1", "g1", "instanceId2", new TestManagerListener(3, result));
        listener.addManagerListener("dataId2", "g1", "instanceId", new TestManagerListener(4, result));
        listener.addManagerListener("dataId2", "g2", "instanceId", new TestManagerListener(5, result));
        listener.addManagerListener("dataId3", null, "instanceId", new TestManagerListener(6, result));

        List<ManagerListener> list1 = listener.getManagerListenerList("dataId1", "g1", "instanceId");
        List<ManagerListener> list2 = listener.getManagerListenerList("dataId1", "g1", "instanceIdx");

        Assert.assertEquals(2, list1.size());
        Assert.assertNull(list2);

        list1.add(new TestManagerListener(7, result));

        list1 = listener.getManagerListenerList("dataId1", "g1", "instanceId");
        Assert.assertEquals(2, list1.size());
    }
}
