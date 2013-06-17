package com.alibaba.demo.verify;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;

/**
 * User: heyman
 * Date: 11/15/11
 * Time: 5:47 下午
 */
public class SenderMain {
    NapoliConnector connector;
    DefaultAsyncSender sender;

    public SenderMain(String url,String qname) {
        connector = new NapoliConnector();
        connector.setAddress(url);
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
        sender.setName(qname);
        sender.setStoreEnable(false);
        sender.setPendingInterval(5000);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                sender.close();
                connector.close();
            }
        }));
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("use default args");
            args = new String[2];
            args[0] = "10.20.153.62";
            args[1] = "heymandev";
        }
        SenderMain senderMain = new SenderMain(args[0],args[1]);
        senderMain.sender.init();
        NapoliMessage napoliMessage = new NapoliMessage("test");
        for (int i=0;i<100;i++){
            senderMain.sender.send(napoliMessage);
        }
        System.out.println("print ok");
    }
}
