package com.alibaba.demo;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.connector.NapoliConnector;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: heyman
 * Date: 5/11/12
 * Time: 10:17 上午
 */
public class ReceiverExample2 {
    private static final String address = "10.33.145.22";
    private static final String queue = "chhtest2";
    private static final int instances = 10;
    private static final int sum = 100000;

    public static void main(String[] args) throws Exception {


        NapoliConnector connector = new NapoliConnector();
        connector.setAddress(address);
        connector.setPoolSize(5);
        connector.setPrefetch(5);
        connector.setStorePath("./target/"+System.currentTimeMillis());
        connector.init();


        //NapoliConnector connector = NapoliConnector.createConnector(address);
        final AtomicInteger count = new AtomicInteger();
        AsyncWorker worker = new AsyncWorker() {
            public boolean doWork(Serializable message) {
                System.out.println(count.incrementAndGet());
                /*try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                return true;
            }
        };




        //final DefaultAsyncReceiver receiver = connector.createReceiver(queue, true, instances, worker);
        DefaultAsyncReceiver defaultReceiver = new DefaultAsyncReceiver();
        defaultReceiver.setConnector(connector);
        defaultReceiver.setName(queue);
        defaultReceiver.setStoreEnable(false);
        defaultReceiver.setWorkerPri(worker);
        defaultReceiver.setReprocessNum(6);
        defaultReceiver.init();
        defaultReceiver.setGroup("heymanchen");
        defaultReceiver.start();



        while (count.get() < sum){
            Thread.sleep(5000);
        }

        defaultReceiver.close();
    }
}
