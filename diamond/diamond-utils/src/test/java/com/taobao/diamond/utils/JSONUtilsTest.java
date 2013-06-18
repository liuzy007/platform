package com.taobao.diamond.utils;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;


public class JSONUtilsTest {

    @Test
    @SuppressWarnings( { "unchecked" })
    public void test1() throws Exception {
        Map<String, Map<String, String>> o = new HashMap<String, Map<String, String>>();
        String jsonString = JSONUtils.serializeObject(o);
        Map<String, Map<String, String>> n =
                (Map<String, Map<String, String>>) JSONUtils.deserializeObject(jsonString, o.getClass());
        Assert.assertEquals(0, n.size());
    }


    @Test
    @SuppressWarnings( { "unchecked" })
    public void test2() throws Exception {
        Map<String, Map<String, String>> o = new HashMap<String, Map<String, String>>();
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("innerKey1", "value1");
        map1.put("innerKey2", "value2");
        map1.put("innerKey3", "value3");
        o.put("key1", map1);
        String jsonString = JSONUtils.serializeObject(o);
        Map<String, Map<String, String>> n =
                (Map<String, Map<String, String>>) JSONUtils.deserializeObject(jsonString, o.getClass());
        Assert.assertEquals(1, n.size());
        Assert.assertEquals(3, n.get("key1").size());
        Assert.assertEquals("value1", n.get("key1").get("innerKey1"));
        Assert.assertEquals("value2", n.get("key1").get("innerKey2"));
        Assert.assertEquals("value3", n.get("key1").get("innerKey3"));
    }


    @Test
    @SuppressWarnings( { "unchecked" })
    public void test3() throws Exception {
        Map<String, Map<String, String>> o = new HashMap<String, Map<String, String>>();
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("innerKey1", "value1");
        map1.put("innerKey2", "value2");
        map1.put("innerKey3", "value3");
        o.put("key1", map1);
        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("innerKey1", "value1");
        map2.put("innerKey2", "value2");
        map2.put("innerKey3", "value3");
        o.put("key2", map2);
        String jsonString = JSONUtils.serializeObject(o);
        Map<String, Map<String, String>> n =
                (Map<String, Map<String, String>>) JSONUtils.deserializeObject(jsonString, o.getClass());
        Assert.assertEquals(2, n.size());

        Assert.assertEquals(3, n.get("key1").size());
        Assert.assertEquals("value1", n.get("key1").get("innerKey1"));
        Assert.assertEquals("value2", n.get("key1").get("innerKey2"));
        Assert.assertEquals("value3", n.get("key1").get("innerKey3"));

        Assert.assertEquals(3, n.get("key2").size());
        Assert.assertEquals("value1", n.get("key2").get("innerKey1"));
        Assert.assertEquals("value2", n.get("key2").get("innerKey2"));
        Assert.assertEquals("value3", n.get("key2").get("innerKey3"));
    }
}
