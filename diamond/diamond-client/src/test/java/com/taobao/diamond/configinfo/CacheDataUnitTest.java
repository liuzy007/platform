package com.taobao.diamond.configinfo;

import junit.framework.Assert;

import org.junit.Test;


public class CacheDataUnitTest {

    @Test
    public void test1() {
        CacheData cacheData = new CacheData("dataId", "group");
        Assert.assertEquals("dataId", cacheData.getDataId());
        Assert.assertEquals("group", cacheData.getGroup());

        cacheData.setDataId("IdData");
        cacheData.setGroup("puorg");
        cacheData.setDomainNamePos(100);
        cacheData.setLastModifiedHeader("lastModifiedHeader");
        cacheData.setLocalConfigInfoFile("localConfigureInfomation");
        cacheData.setLocalConfigInfoVersion(1000L);
        cacheData.setMd5("md5");

        Assert.assertEquals("IdData", cacheData.getDataId());
        Assert.assertEquals("puorg", cacheData.getGroup());
        Assert.assertEquals(100, cacheData.getDomainNamePos().get());
        Assert.assertEquals("lastModifiedHeader", cacheData.getLastModifiedHeader());
        Assert.assertEquals("localConfigureInfomation", cacheData.getLocalConfigInfoFile());
        Assert.assertEquals("md5", cacheData.getMd5());
        Assert.assertEquals(1000L, cacheData.getLocalConfigInfoVersion());
    }
}
