package com.alibaba.demo;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: heyman
 * Date: 8/28/12
 * Time: 1:46 PM
 */
public class SimpleSender {
    public static void main(String[] args) throws Exception{
        NapoliConnector connector = new NapoliConnector("10.20.153.30:90/hz-lp-daily");
        connector.setStorePath("/tmp/napoli");
        connector.init();
        //String destination = "TRADING_BUYER_REFUND_TOPIC";
        String destination = "test-topic";
        DefaultAsyncSender sender = DefaultAsyncSender.createSender(connector,destination,false);
        sender.setReprocessInterval(1000);
        sender.init();
        sender.send("hello1");
        System.out.println("=========send ok==============");
        //sender.send("hello2");
        //Thread.sleep(100000);
        sender.close();
       /* final AtomicInteger count = new AtomicInteger(0);
        DefaultAsyncReceiver receiver = DefaultAsyncReceiver.createReceiver(connector,destination,false,4,new AsyncWorker() {
            @Override
            public boolean doWork(Serializable message) {
                count.incrementAndGet();
                return true;
            }
        });
        receiver.start();
        sender.close();
        while (count.get() < 2){
            Thread.sleep(100);
        }
        receiver.close();*/
        connector.close();
        //System.exit(1);
    }
}



