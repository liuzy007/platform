package com.alibaba.demo;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.model.Person;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: heyman
 * Date: 4/12/12
 * Time: 5:42 下午
 */
public class ReceiverExample {
    private static final String address = "10.33.145.22:90/hz-cn";
    private static final String queue = "HornetqTest";
    private static final int instances = 10;
    private static final int sum = 10000000;

    public static void main(String[] args) throws Exception {
        
        
        NapoliConnector connector = new NapoliConnector();
        connector.setAddress(address);
        connector.setPoolSize(5);
        connector.setPrefetch(5);
        connector.setStorePath("./target/" + System.currentTimeMillis());
        connector.setInterval(10000);
        connector.setConnectionCheckPeriod(30000);
        connector.init();
        
        
        //NapoliConnector connector = NapoliConnector.createConnector(address);
        final AtomicInteger count = new AtomicInteger();
        AsyncWorker worker = new AsyncWorker() {
            public boolean doWork(Serializable message) {
                System.out.println(count.incrementAndGet()+" name="+((Person)message).getPenName());
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
        defaultReceiver.setPeriod(1000);
        defaultReceiver.setGroup("HEYMANCHEN2");
        defaultReceiver.start();
        System.out.println("receiver start ok");
        
        
        while (count.get() < sum){
            Thread.sleep(5000);
        }
        
        defaultReceiver.close();
    }
}