package com.alibaba.demo.verify;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.ext.AsyncWorkerEx;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.connector.NapoliConnector;

import java.io.Serializable;

/**
 * User: heyman
 * Date: 11/7/11
 * Time: 11:52 上午
 */
public class Receiver {
    public static NapoliConnector connector;
    public static DefaultAsyncReceiver receiver;

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

        receiver = new DefaultAsyncReceiver();
        receiver.setConnector(connector);
        receiver.setName("queuetest0");
        receiver.setStoreEnable(true);
    }

    public static void close() throws Exception {
        receiver.close();
        connector.close();
    }

    public void startLongConsumer() throws Exception {
        AsyncWorker worker = new AsyncWorker() {
            public boolean doWork(Serializable message) {
                try {
                    Thread.sleep(50*60*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("receive dowork ok!");
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
        receiver.init();
        receiver.start();
        System.out.println("start");
        //Thread.sleep(10*60*1000);
    }

    public void startNapoliMessageConsumer() throws Exception {
         AsyncWorkerEx worker = new AsyncWorkerEx() {
            public boolean doWork(NapoliMessage message) {
                try {
                    Thread.sleep(5*60*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        };
        receiver.setExWorker(worker);;
        receiver.init();
        receiver.start();
        Thread.sleep(10*60*1000);
    }

    public static void main(String[] args) throws Exception{
        Receiver receiver = new Receiver();
        Receiver.init();
        
        receiver.startLongConsumer();
        receiver.startNapoliMessageConsumer();
        Receiver.close();
    }
}
