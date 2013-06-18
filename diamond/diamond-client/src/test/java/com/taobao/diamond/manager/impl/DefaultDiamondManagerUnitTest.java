package com.taobao.diamond.manager.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import junit.framework.Assert;

import org.apache.log4j.Category;
import org.junit.Test;

import com.taobao.diamond.client.impl.DiamondClientFactory;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.configinfo.ConfigureInfomation;
import com.taobao.diamond.manager.ManagerListener;


public class DefaultDiamondManagerUnitTest {

    @Test
    public void testGetManagerListener() {
        ManagerListener listener1 = new ManagerListener() {

            public Executor getExecutor() {
                return null;
            }


            public void receiveConfigInfo(String configInfo) {
            }
        };
        DefaultDiamondManager manager1 = new DefaultDiamondManager("dataId", listener1);
        ManagerListener listener2 = new ManagerListener() {

            public Executor getExecutor() {
                return null;
            }


            public void receiveConfigInfo(String configInfo) {
            }
        };
        DefaultDiamondManager manager2 = new DefaultDiamondManager("dataId", listener2);

        ManagerListener listener3 = new ManagerListener() {

            public Executor getExecutor() {
                return null;
            }


            public void receiveConfigInfo(String configInfo) {
            }
        };
        DefaultDiamondManager manager3 = new DefaultDiamondManager("dataId", listener3);

        Assert.assertEquals(listener1, manager1.getManagerListeners().get(0));
        Assert.assertEquals(listener2, manager2.getManagerListeners().get(0));
        Assert.assertEquals(listener3, manager3.getManagerListeners().get(0));

    }


    @Test
    public void testCallAllListener() {
        final Map<Integer, String> result = new ConcurrentHashMap<Integer, String>();
        ManagerListener listener1 = new ManagerListener() {

            public Executor getExecutor() {
                return null;
            }


            public void receiveConfigInfo(String configInfo) {
                result.put(1, configInfo);
            }
        };
        new DefaultDiamondManager("dataId", listener1);
        ManagerListener listener2 = new ManagerListener() {

            public Executor getExecutor() {
                return null;
            }


            public void receiveConfigInfo(String configInfo) {
                result.put(2, configInfo);
            }
        };
        new DefaultDiamondManager("dataId", listener2);

        ManagerListener listener3 = new ManagerListener() {

            public Executor getExecutor() {
                return null;
            }


            public void receiveConfigInfo(String configInfo) {
                result.put(3, configInfo);
            }
        };
        new DefaultDiamondManager("dataId", listener3);

        ConfigureInfomation info = new ConfigureInfomation();
        info.setDataId("dataId");
        info.setGroup(Constants.DEFAULT_GROUP);
        info.setConfigureInfomation("config info");

        DiamondClientFactory.getSingletonDiamondSubscriber("diamond").getSubscriberListener().receiveConfigInfo(info);

        Assert.assertEquals(3, result.size());
        Assert.assertEquals("config info", result.get(1));
        Assert.assertEquals("config info", result.get(2));
        Assert.assertEquals("config info", result.get(3));

    }
    
    public static void main(String[] args)throws Exception {
    	System.out.println(Category.getRoot().getLevel());
    	ManagerListener listener3 = new ManagerListener() {

            public Executor getExecutor() {
                return null;
            }


            public void receiveConfigInfo(String configInfo) {
               System.out.println(configInfo);
            }
        };
        String dataId = "xxx.dataId.do.not.exist.tttttXXXxxx";
        String group = "DEFAULT_GROUP";
        
        String dataId2 = "com.taobao.uic.cacheConfig";
        String group2 = "1.0.0.daily";
        String dataId3 = "com.taobao.session.config.xml";
        String group3 = "daily_session";
        
        String dataId4 = "xxx.Routing.Rule";
        String group4 = "TEST_GROUP";
        
        String dataId5 = "2.0.4.v.test.3";
        String DEFAULT_GROUP = "DEFAULT_GROUP";
        String dataId6 = "2.0.4.v.test.4";
        
        String dataid7 = "2.0.4.v.test.5";
        
        
        new DefaultDiamondManager(group,dataId, listener3);
        
        new DefaultDiamondManager(group2,dataId2, listener3);
        
        new DefaultDiamondManager(group3,dataId3, listener3);
        new DefaultDiamondManager(group4,dataId4, listener3);
        
        new DefaultDiamondManager(DEFAULT_GROUP,dataId5, listener3);
        new DefaultDiamondManager(DEFAULT_GROUP,dataId6, listener3);
        new DefaultDiamondManager(DEFAULT_GROUP,dataid7, listener3);

       for(;;){
    	   Thread.sleep(1000);
       }
	}
}
