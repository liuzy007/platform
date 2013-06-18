package com.taobao.diamond.utils;

import org.junit.Assert;
import org.junit.Test;


public class SimpleCacheUnitTest {

    @Test
    public void testNormal() {
        SimpleCache<String> cache = new SimpleCache<String>();
        cache.put("key1", "value1");

        Assert.assertEquals("value1", cache.get("key1"));
        Assert.assertNull(cache.get("key2"));
    }
    
    @Test
    public void testTimeout() throws Exception {
        SimpleCache<String> cache = new SimpleCache<String>(1000L);
        cache.put("key1", "value1");
        
        Assert.assertEquals("value1", cache.get("key1"));
        
        Thread.sleep(2000L);
        Assert.assertNull(cache.get("key1"));
    }
}
