/**
 * Project: napoli.client File Created at 2009-6-17 $Id: NapoliConnectorTest.java 17586 2009-07-29 05:34:41Z
 * guolin.zhuanggl $ Copyright 2008 Alibaba.com Croporation Limited. All rights reserved. This software is the
 * confidential and proprietary information of Alibaba Company. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.napoli.connector;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.FileSystemUtils;

/**测试NapoliConnector(server地址，storePath路径)
 * @author guolin.zhuanggl
 */
public class NapoliConnectorConcurrentTest {

    private static final Log log = LogFactory.getLog(NapoliConnectorConcurrentTest.class);

    private NapoliConnector  connector;

    @Before
    public void setUp() throws Exception {
        connector = new NapoliConnector();
        connector.setAddress("10.20.153.62:8080");
        connector.setStorePath(new File("./NapoliConnectorConcurrentTest").getAbsolutePath());
        connector.init();
        log.info("setUp finished!");
    }

    @After
    public void tearDown() throws Exception {
        log.info("tearDown finished!");
        File file = new File("./NapoliConnectorConcurrentTest");
        FileSystemUtils.deleteRecursively(file);
    }
    
    @Test
    public void test() {
        
    }

}
