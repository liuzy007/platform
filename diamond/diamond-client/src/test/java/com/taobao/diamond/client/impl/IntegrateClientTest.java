package com.taobao.diamond.client.impl;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.client.DiamondSubscriber;
import com.taobao.diamond.client.impl.DefaultDiamondSubscriber.Builder;


@Ignore
public class IntegrateClientTest {
    private static final Log log = LogFactory.getLog(IntegrateClientTest.class);
    private DiamondSubscriber diamondSubscriber = null;
    private TestSubscriberListener listener = new TestSubscriberListener();


    @Before
    public void setUp() {
        log.info("setUp IntegrateClientTest");
        DiamondConfigure diamondConfigure = new DiamondConfigure("diamond");
        diamondConfigure.setFilePath("C:\\filepath");
        DiamondConfigure config = new DiamondConfigure("diamond");
        config.setPollingIntervalTime(2);
        config.addDomainName("192.168.207.102");
        config.setConfigServerAddress("192.168.207.102");
        diamondSubscriber =
                new Builder(listener).setDiamondConfigure(diamondConfigure).addDataId("dataId1", null)
                    .setDiamondConfigure(config).build();
        diamondSubscriber.start();
    }


    @After
    public void tearDown() {
        log.info("tearDown IntegrateClientTest");
        diamondSubscriber.close();
    }


    @Test
    public void test1() throws Exception {
        while (!listener.getUpdateConfigInfo()) {
            Thread.sleep(100);
        }
        listener.setUpdateConfigInfo(false);
        Assert.assertNotNull(listener.getConfigureInfomation());
        System.err.println("获取了配置信息");
        Thread.sleep(50000);
    }
}
