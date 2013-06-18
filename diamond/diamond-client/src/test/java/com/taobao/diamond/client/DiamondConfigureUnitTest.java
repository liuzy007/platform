package com.taobao.diamond.client;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.taobao.diamond.common.Constants;


public class DiamondConfigureUnitTest {

    @Test
    public void test1() {
        DiamondConfigure config = new DiamondConfigure("diamond");
        Assert.assertEquals(System.getProperty("user.home") + "/diamond/diamond", config.getFilePath());
        config.setConnectionStaleCheckingEnabled(true);
        Assert.assertTrue(config.isConnectionStaleCheckingEnabled());
        config.setConnectionStaleCheckingEnabled(false);
        Assert.assertFalse(config.isConnectionStaleCheckingEnabled());

        config.addDomainName("A");
        config.addDomainName("B");
        List<String> list1 = config.getDomainNameList();
        Assert.assertEquals(2, list1.size());
        Assert.assertTrue(list1.contains("A"));
        Assert.assertTrue(list1.contains("B"));
        List<String> list2 = new LinkedList<String>();
        list2.add("C");
        list2.add("D");
        config.addDomainNames(list2);
        List<String> list3 = config.getDomainNameList();
        Assert.assertEquals(4, list3.size());
        Assert.assertTrue(list3.contains("A"));
        Assert.assertTrue(list3.contains("B"));
        Assert.assertTrue(list3.contains("C"));
        Assert.assertTrue(list3.contains("D"));
        
        config.addPushitDomainName("PA");
        config.addPushitDomainName("PB");
        List<String> pList1 = config.getPushitDomainNameList();
        Assert.assertEquals(2, pList1.size());
        Assert.assertTrue(pList1.contains("PA"));
        Assert.assertTrue(pList1.contains("PB"));
        List<String> pList2 = new LinkedList<String>();
        pList2.add("PC");
        pList2.add("PD");
        config.addPushitDomainNames(pList2);
        List<String> pList3 = config.getPushitDomainNameList();
        Assert.assertEquals(4, pList3.size());
        Assert.assertTrue(pList3.contains("PA"));
        Assert.assertTrue(pList3.contains("PB"));
        Assert.assertTrue(pList3.contains("PC"));
        Assert.assertTrue(pList3.contains("PD"));
        
        config.setConnectionTimeout(111);
        config.setFilePath("/home/admin");
        config.setMaxHostConnections(66);
        config.setMaxTotalConnections(77);
        config.setOnceTimeout(88);
        config.setPollingIntervalTime(99);
        config.setPort(9777);
        config.setReceiveWaitTime(2000);
        config.setScheduledThreadPoolSize(110);
        config.setRetrieveDataRetryTimes(10);

        Assert.assertEquals(111, config.getConnectionTimeout());
        Assert.assertEquals("/home/admin", config.getFilePath());
        Assert.assertEquals(66, config.getMaxHostConnections());
        Assert.assertEquals(77, config.getMaxTotalConnections());
        Assert.assertEquals(88, config.getOnceTimeout());
        Assert.assertEquals(99, config.getPollingIntervalTime());
        Assert.assertEquals(9777, config.getPort());
        Assert.assertEquals(2000, config.getReceiveWaitTime());
        Assert.assertEquals(110, config.getScheduledThreadPoolSize());
        Assert.assertEquals(10, config.getRetrieveDataRetryTimes());
    }


    @Test
    public void testSetPollingTime() {
        DiamondConfigure config = new DiamondConfigure("diamond");

        Assert.assertEquals(Constants.POLLING_INTERVAL_TIME, config.getPollingIntervalTime());

        config.setPollingIntervalTime(10);

        Assert.assertEquals(Constants.POLLING_INTERVAL_TIME, config.getPollingIntervalTime());

        config.setPollingIntervalTime(100);

        Assert.assertEquals(100, config.getPollingIntervalTime());
    }
    
}
