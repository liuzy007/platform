package com.alibaba.demo.verify;

import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * User: heyman
 * Date: 11/2/11
 * Time: 3:43 下午
 */
public abstract class Sender {
    public static NapoliConnector connector;
    public static DefaultAsyncSender sender;
    public static DefaultAsyncReceiver receiver;

    @BeforeClass
    public static void init() throws Exception {
        connector = new NapoliConnector();
        connector.setAddress("127.0.0.1:8080");
        connector.setStorePath("./target/verify_data");
        connector.setUserName("napoli");
        connector.setPassword("napoli");

        connector.setInterval(2000000); //no更新配置
        connector.setSendSessionControl(true);
        connector.setPoolSize(10);
        connector.setSendTimeout(20000);
        connector.init();

        sender = new DefaultAsyncSender();
        sender.setConnector(connector);
        sender.setName("queuetest0");
        sender.setStoreEnable(false);
        sender.setPendingInterval(5000);
        sender.init();
    }

    @AfterClass
    public static void close() throws Exception {
        sender.close();
        connector.close();
    }
}
