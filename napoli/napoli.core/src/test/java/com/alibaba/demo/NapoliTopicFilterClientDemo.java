/**
 * Project: napoli.client-1.4.0
 * 
 * File Created at 2010-8-3
 * $Id: NapoliTopicFilterClientDemo.java 154477 2012-03-13 03:57:55Z haihua.chenhh $
 * 
 * Copyright 2008 Alibaba.com Croporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.demo;

import com.alibaba.napoli.client.async.NapoliTextFilter;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;

/**
 * TODO 测试filter。消息选择路由到topic对应的哪些queue上
 * @author dacy
 *
 */
public class NapoliTopicFilterClientDemo {
//    private static final Log log = LogFactory.getLog(NapoliTopicFilterClientDemo.class);

    
    public static final void main(String args[]) throws Exception{

        NapoliConnector connector;
        DefaultAsyncSender vtProducer;
        
        connector = new NapoliConnector();
        connector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath"));
        connector.setAddress("10.20.189.62:8080");// 设置napoli服务器地址
        connector.init();

        vtProducer = new DefaultAsyncSender();
        vtProducer.setDestinationFilter(new NapoliTextFilter());
        vtProducer.setName("testForFilter");
        vtProducer.setConnector(connector);
        vtProducer.setInstances(5);
        vtProducer.init();
        vtProducer.send("b");
        vtProducer.close();
        connector.close();
    }
    
 
}
