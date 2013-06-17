package com.alibaba.napoli.client.benchmark;

import com.alibaba.dragoon.client.DragoonClient;
import com.alibaba.dragoon.stat.napoli.NapoliReceiverStat;
import com.alibaba.dragoon.stat.napoli.NapoliSenderStat;
import com.alibaba.dragoon.stat.napoli.NapoliStatManager;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.model.MyWorker;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.receiver.impl.ReceiverMonitorFilter;
import com.alibaba.napoli.sender.impl.SenderMonitorFilter;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 测试AbstractNapoliClient User: heyman Date: 6/17/11 Time: 5:14 PM
 */
public abstract class AbstractNapoliClientTest {
  
    protected static NapoliConnector      sendConnector;
    protected static NapoliConnector      recConnector;

    protected static DefaultAsyncSender   vSender;
    protected static DefaultAsyncSender   qSender;

    protected static DefaultAsyncReceiver q1Receiver;
    protected static DefaultAsyncReceiver q2Receiver;
    protected static DefaultAsyncReceiver heymanReceiver;

    protected static MyWorker             q1Worker          = new MyWorker();
    protected static MyWorker             q2Worker          = new MyWorker();
    protected static MyWorker             q3Worker          = new MyWorker();

    protected static NapoliStatManager    napoliStatManager = mock(NapoliStatManager.class);
    protected static NapoliSenderStat     napoliSenderStat;
    protected static NapoliReceiverStat   napoliReceiverStat;
    protected static String               initConsumeMessage;

    protected static final Log            log               = LogFactory.getLog("NapoliClientTest");
    static String suffix = "_" + System.currentTimeMillis();
    //static String suffix = "";
    protected static String topicName = "NapoliClientTest" + suffix;
    protected static String queueName1 = "NapoliClientTest_q1" + suffix;
    protected static String queueName2 = "NapoliClientTest_q2" + suffix;
    protected static String queueName3 = "NapoliClientTest_q3" + suffix;
    

    @BeforeClass
    public static void init() throws Exception {
        DragoonClient.setJdbcStatEnable(false);
        DragoonClient.setSpringStatEnable(false);
        DragoonClient.setUriStatEnable(false);
        DragoonClient.setLog4jStatEnable(true); // 用于Exception监控项的采集
        DragoonClient.start("AbstractNapoliClientTest"); // 参数：应用名称

        NapoliTestUtil.delFiles(NapoliTestUtil.getProperty("napoli.func.storePath"));

        sendConnector = new NapoliConnector();
        sendConnector.setAddress(NapoliTestUtil.getAddress());
        sendConnector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath") + "\\" + new Date().hashCode());
        sendConnector.setJmsUserName(NapoliTestUtil.getUser());
        sendConnector.setJmsPassword(NapoliTestUtil.getPasswd());
        sendConnector.setInterval(6000);
        sendConnector.setPoolSize(25);
        sendConnector.setSendTimeout(1000);
        sendConnector.init();
       
        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName1, NapoliTestUtil.getProperty("napoli.func.mixGroupName"));
        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName2, NapoliTestUtil.getProperty("napoli.func.mixGroupName"));
        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName3, NapoliTestUtil.getProperty("napoli.func.mixGroupName"));
        
        HttpUtil.createVtopic(NapoliTestUtil.getAddress(), topicName, new String[]{queueName1, queueName2, queueName3});
        
        log.info("yanny: AbstractNapoliClientTest started with topicName=" + topicName + " with queues:" + queueName1 + ","+ queueName2 + ","+ queueName3);
        
        vSender = new DefaultAsyncSender();
        vSender.setConnector(sendConnector);
        vSender.setName(topicName);

        vSender.setStoreEnable(true);
        vSender.setReprocessInterval(100 * 1000 * 1000);

        vSender.init();

        qSender = new DefaultAsyncSender();
        qSender.setConnector(sendConnector);
        qSender.setName(queueName3);
        qSender.setStoreEnable(true);
        qSender.setReprocessInterval(100 * 1000 * 1000);
        qSender.init();

        recConnector = new NapoliConnector();
        recConnector.setAddress(NapoliTestUtil.getAddress());
        recConnector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath"));
        recConnector.init();

        q1Receiver = new DefaultAsyncReceiver();
        q1Receiver.setConnector(recConnector);
        q1Receiver.setStoreEnable(true);
        q1Receiver.setName(queueName1);
        q1Receiver.setWorker(q1Worker);
        q1Receiver.init();
        q1Receiver.start();

        q2Receiver = new DefaultAsyncReceiver();
        q2Receiver.setConnector(recConnector);
        q2Receiver.setStoreEnable(true);
        q2Receiver.setName(queueName2);
        q2Receiver.setWorker(q2Worker);
        q2Receiver.init();
        q2Receiver.start();

        heymanReceiver = new DefaultAsyncReceiver();
        heymanReceiver.setConnector(recConnector);
        heymanReceiver.setName(queueName3);
        heymanReceiver.setWorker(q3Worker);
        heymanReceiver.setStoreEnable(true);
        heymanReceiver.init();
        heymanReceiver.start();
       
        SenderMonitorFilter.setMonitor(napoliStatManager);
        ReceiverMonitorFilter.setMonitor(napoliStatManager);
    }

    @Before
    public void setup() {
        napoliSenderStat = mock(NapoliSenderStat.class);
        napoliReceiverStat = mock(NapoliReceiverStat.class);
        when(napoliStatManager.getSenderStat(anyString(), anyString())).thenReturn(napoliSenderStat);

        when(napoliStatManager.getReceiverStat(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(napoliReceiverStat);
    }

    @AfterClass
    public static void dispose() {
        log.info("yanny AfterClass is executed " + System.currentTimeMillis());
        try {
            q1Receiver.close();

            q2Receiver.close();
            heymanReceiver.close();
            vSender.close();
            qSender.close();
        } catch (Exception e) {
        }

        sendConnector.close();
        recConnector.close();
        HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName1);
        HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName2);
        HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName3);
        
        HttpUtil.deleteVtopic(NapoliTestUtil.getAddress(), topicName);
        
        ConsoleConnector.closeAll();
    }
}
