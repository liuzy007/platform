package com.alibaba.demo;

import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.model.Person;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: heyman
 * Date: 4/12/12
 * Time: 5:31 下午
 */
public class SenderExample {
    private static final String address="10.33.145.22:90/hz-cn";
    private static final String queue="amqTEst";
    private static final int threadNum= 10;
    private static final AtomicInteger count = new AtomicInteger(0);
    private static final AtomicInteger failcount = new AtomicInteger(0);
    
    public static void main(String[] args) throws Exception{
        NapoliConnector connector = new NapoliConnector();
        connector.setAddress(address);
        connector.setSendTimeout(1000);
        //connector.setInterval(1);
        connector.setStorePath("./target");
        connector.setPoolSize(threadNum);
        connector.init();

        final DefaultAsyncSender sender = new DefaultAsyncSender();
        sender.setConnector(connector);
        sender.setName(queue);
        sender.setStoreEnable(false);
        sender.init();
        
        //final DefaultAsyncSender sender = connector.createSender(queue, false);
        final Person person = new Person();
        person.setPenName("heymantest");
        
        final CountDownLatch endLatch = new CountDownLatch(threadNum);
        for (int i=0;i<threadNum;i++){
            Thread thread = new Thread("queue=" + queue + " client=" + i) {
                public void run() {
                    try {
                        for (int j=0;j<100;j++){
                            if(sender.send(person)){
                                System.out.println("success="+count.incrementAndGet()+" fail="+failcount.get());
                            }else{
                                System.out.println("success="+count.get()+" fail="+failcount.incrementAndGet());
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    endLatch.countDown();
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
        endLatch.await();
        System.out.println("send ok!!!");
    }
}    