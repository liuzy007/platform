package com.alibaba.napoli;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;

import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.common.util.JmxUtil;

import com.alibaba.napoli.common.util.NamedThreadFactory;

import javax.management.remote.JMXConnector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class AbstractTestBase {
    public static final Log          logger                        = LogFactory.getLog("AbstractTestBase");

    public static NapoliConnector    connector;
    public static String             queueName                     = "";

    public static List<JMXConnector> jmxConnectors                 = new ArrayList<JMXConnector>();
    public static Future<?>          getQueueConsumerSessionThread = null;
    private static ExecutorService   executorService               = null;
    private static Future<?>         future                        = null;
    public static int                expectedConsumerSession       = -1;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        connector = new NapoliConnector();
        connector.setAddress(NapoliTestUtil.getAddress());
        connector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath") + "\\" + new Date().hashCode());
        checkQueueConsumerSessionCountAsExpected(false, 1);
    }

    public static void getQueueConsumerSessionCount() {
        //getQueueConsumerSessionCount(true, 5, 2);
    }

    public static void checkQueueConsumerSessionCountAsExpected(final boolean isLog, final double periodSecond) {

        if (!NapoliTestUtil.isBlank(queueName)) {
            //jmxConnectors = JmxUtil.getJMXConnectors(NapoliTestUtil.getAddress(), queueName);

            if (executorService != null) {
                future.cancel(true);
                executorService.shutdown();
            }

            executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory(
                    "napoli--test--checkQueueConsumerSessionCountAsExpected--" + queueName, true));

            future = executorService.submit(new Runnable() {
                public void run() {
                    try {
                        while (true) {
                            Thread.sleep((int) (1000 * periodSecond));
                            if (expectedConsumerSession > 0) {
                                int count = JmxUtil.getConsumerSessionCount(connector.getAddress(), queueName);
                                if (count <= expectedConsumerSession) {
                                    if (isLog) {

                                        logger.info("-----queue:" + queueName + " has " + count + " sessions");
                                    }
                                } else {
                                    logger.error("-----queue:" + queueName + " has more than expectedConsumerSession("
                                            + expectedConsumerSession + ") session, total " + count
                                            + " sessions, something wrong, please check");
                                }
                            }
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });
        }
    }

    public static void checkQueueConsumerSessionCountAsThreshold(final boolean isLog, final int threshold,
                                                                 final double periodSecond) {

        //jmxConnectors = JmxUtil.getJMXConnectors(NapoliTestUtil.getAddress(), queueName);

        if (executorService != null) {
            future.cancel(true);
            executorService.shutdown();
        }

        executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory(
                "napoli--test--checkQueueConsumerSessionCountAsThreshold--" + queueName, true));

        future = executorService.submit(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        Thread.sleep((int) (1000 * periodSecond));
                        int count = JmxUtil.getConsumerSessionCount(connector.getAddress(),  queueName);
                        if (count <= threshold) {
                            if (isLog) {

                                logger.info("-----queue:" + queueName + " has " + count + " sessions");
                            }
                        } else {
                            logger.error("-----queue:" + queueName + " has more than threshold(" + threshold
                                    + ") session, total " + count + " sessions, something wrong, please check");
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });

    }
    
    @After
    public void stop() throws NapoliClientException {
        
        expectedConsumerSession = -1;
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {

        logger.info("yanny afterClass is executed.");

        if (connector != null) {
            connector.close();
        }

        if (executorService != null) {
            future.cancel(true);
            executorService.shutdown();
        }

        NapoliTestUtil.sleepSecond(1);

        for (JMXConnector connector : jmxConnectors) {
            if (connector != null) {
                connector.close();
            }
        }

        try {
            HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
