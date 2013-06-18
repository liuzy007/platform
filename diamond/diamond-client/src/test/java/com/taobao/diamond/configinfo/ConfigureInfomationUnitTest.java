package com.taobao.diamond.configinfo;

import junit.framework.Assert;

import org.junit.Test;


public class ConfigureInfomationUnitTest {

    @Test
    public void test1() {
        ConfigureInfomation ci = new ConfigureInfomation();
        ci.setConfigureInfomation("configureInfomation");
        ci.setDataId("dataId");
        ci.setGroup("group");
        Assert.assertEquals("configureInfomation", ci.getConfigureInfomation());
        Assert.assertEquals("dataId", ci.getDataId());
        Assert.assertEquals("group", ci.getGroup());
    }


    @Test
    public void test2() {
        ConfigureInfomation ci1 = new ConfigureInfomation();
        ConfigureInfomation ci2 = new ConfigureInfomation();
        Assert.assertTrue(ci1.equals(ci1));
        Assert.assertTrue(ci1.equals(ci2));
        ci1.setDataId("dataId");
        ci1.hashCode();
        ci1.toString();
    }
}
