package com.taobao.diamond.client.processor;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.taobao.diamond.common.Constants;


public class LocalConfigInfoProcessorUnitTest {
    @Test
    @SuppressWarnings({ "unchecked" })
    public void isQualifiedTest() {
        Field localMapField = null;
        Map<String, Map<String, String>> localMap = null;
        String address = LocalConfigInfoProcessor.getHostAddress();
        LocalConfigInfoProcessor processor = new LocalConfigInfoProcessor();
        Assert.assertFalse(processor.containThisHostConfig());
        Map<String, String> dataIdGroupPairs = new HashMap<String, String>();
        dataIdGroupPairs.put("dataId1", "group1");
        dataIdGroupPairs.put("dataId2", "group2");
        try {
            localMapField = processor.getClass().getDeclaredField("localMap");
            localMapField.setAccessible(true);
            localMap = (Map<String, Map<String, String>>) localMapField.get(processor);
            if (null == localMap) {
                localMapField.set(processor, new HashMap<String, Map<String, String>>());
                localMap = (Map<String, Map<String, String>>) localMapField.get(processor);
            }
            localMap.put(address, dataIdGroupPairs);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(processor.containThisHostConfig());
        Assert.assertTrue(processor.isQualified("dataId1", "group1"));
        Assert.assertTrue(processor.isQualified("dataId2", "group2"));
        Assert.assertFalse(processor.isQualified("dataId3", "group2"));
        Assert.assertFalse(processor.isQualified("dataId1", "group2"));
    }


    @Test
    public void getFilePathTest() {
        LocalConfigInfoProcessor processor = new LocalConfigInfoProcessor();
        processor.start("rootPath");
        String filePath = processor.getFilePath("dataId", "group");
        StringBuilder filePathBuilder = new StringBuilder();
        filePathBuilder.append("rootPath").append("/").append(Constants.BASE_DIR).append("/").append("group")
            .append("/").append("dataId");
        Assert.assertEquals(new File(filePathBuilder.toString()).getAbsolutePath(), filePath);
    }


    @Test
    public void defaultGroupOrClientGroupTest() {
        LocalConfigInfoProcessor processor = new LocalConfigInfoProcessor();
        Assert.assertEquals(Constants.DEFAULT_GROUP, processor.defaultGroupOrClientGroup(null));
        Assert.assertEquals("group", processor.defaultGroupOrClientGroup("group"));
    }


    @Test
    public void getGroupByAddressTest() {
        LocalConfigInfoProcessor processor = new LocalConfigInfoProcessor();
        Field localMapField = null;
        try {
            localMapField = LocalConfigInfoProcessor.class.getDeclaredField("localMap");
        }
        catch (Exception e) {
            // 记录日志或打印输出
        }
        localMapField.setAccessible(true);
        Map<String, Map<String, String>> localMap = new HashMap<String, Map<String, String>>();
        Map<String, String> a = new HashMap<String, String>();
        a.put("dataId1", "group1");
        a.put("dataId2", "group2");
        Map<String, String> b = new HashMap<String, String>();
        b.put("dataId1", "group3");
        b.put("dataId2", "group4");
        Map<String, String> c = new HashMap<String, String>();
        c.put("dataId1", "group5");
        c.put("dataId2", "group6");
        localMap.put(LocalConfigInfoProcessor.getHostAddress(), a);
        localMap.put(Constants.DEFAULT_GROUP, b);
        localMap.put("special_group", c);
        try {
            localMapField.set(processor, localMap);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals("group1", processor.getGroupByAddress("dataId1", null));
        Assert.assertEquals("group2", processor.getGroupByAddress("dataId2", null));
        Assert.assertEquals(Constants.DEFAULT_GROUP, processor.getGroupByAddress("dataId3", null));
        Assert.assertEquals("special_group", processor.getGroupByAddress("dataId3", "special_group"));
        Assert.assertEquals("group1", processor.getGroupByAddress("dataId1", "special_group"));
        Assert.assertEquals("group2", processor.getGroupByAddress("dataId2", "special_group"));
        Assert.assertEquals("group1", processor.getGroupByAddress("dataId1", Constants.DEFAULT_GROUP));
        Assert.assertEquals("group2", processor.getGroupByAddress("dataId2", Constants.DEFAULT_GROUP));
        localMap.remove(LocalConfigInfoProcessor.getHostAddress());
        Assert.assertEquals("special_group", processor.getGroupByAddress("dataId1", "special_group"));
        Assert.assertEquals("special_group", processor.getGroupByAddress("dataId2", "special_group"));
        Assert.assertEquals(Constants.DEFAULT_GROUP, processor.getGroupByAddress("dataId1", Constants.DEFAULT_GROUP));
        Assert.assertEquals(Constants.DEFAULT_GROUP, processor.getGroupByAddress("dataId2", Constants.DEFAULT_GROUP));
    }


    @Test
    public void startStopTest() throws Exception {
        LocalConfigInfoProcessor processor = new LocalConfigInfoProcessor();
        String diamondHome = System.getProperty("user.home") + "/diamond";
        processor.start(diamondHome);
        Thread.sleep(1000);
        processor.stop();
        Thread.sleep(1000);
        processor.start(diamondHome);
        Thread.sleep(1000);
        processor.stop();
    }
}
