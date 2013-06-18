package com.alibaba.demo.verify;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.connector.NapoliConnector;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: heyman
 * Date: 11/14/11
 * Time: 2:48 下午
 */
public class ReceiverMain {
    private DefaultAsyncReceiver receiver;

    public ReceiverMain(String url,String qname) {
        NapoliConnector connector = new NapoliConnector();
        connector.setAddress(url);
        connector.setStorePath("./target/verify_data");
        connector.setUserName("napoli");
        connector.setPassword("napoli");

        connector.setInterval(2000000); //no更新配置
        connector.setSendSessionControl(true);
        connector.setPoolSize(10);
        connector.setSendTimeout(20000);
        connector.init();

        receiver = new DefaultAsyncReceiver();
        receiver.setConnector(connector);
        receiver.setName(qname);
        receiver.setStoreEnable(false);
        receiver.setInstances(5);

        AsyncWorker worker = new AsyncWorker() {
            private AtomicInteger count = new AtomicInteger(0);
            public boolean doWork(Serializable message) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("receive dowork ok!"+count.incrementAndGet());
                /*try {
                    int i;
                    while(true){
                        i =0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                return true;
            }
        };
        receiver.setWorker(worker);
        /***
        try {
			receiver.setWorker(worker);
		} catch (NapoliClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		***/
    }

    public DefaultAsyncReceiver getReceiver() {
        return receiver;
    }

    public static void main(String[] args) throws Exception{
        if (args.length != 2) {
            System.out.println("use default args");
            args = new String[2];
            args[0] = "10.20.153.62";
            args[1] = "heymandev";
        }
        ReceiverMain receiverMain = new ReceiverMain(args[0],args[1]);
        DefaultAsyncReceiver defaultAsyncReceiver = receiverMain.getReceiver();
        defaultAsyncReceiver.init();
        defaultAsyncReceiver.setPeriod(1000);
        defaultAsyncReceiver.start();
        System.out.println("start");
    }
}
