package com.alibaba.demo;

import com.alibaba.napoli.ResourceConstant;
import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.model.Person;
import com.alibaba.napoli.sender.Sender;
import java.io.Serializable;

/**
 * User: heyman
 * Date: 4/5/12
 * Time: 11:22 上午
 */
public class Demo {
    public static void main(String[] args) throws Exception{
        NapoliConnector consoleConnector = NapoliConnector.createConnector(ResourceConstant.shConsole);
        consoleConnector.init();
        DefaultAsyncSender sender = DefaultAsyncSender.createSender(consoleConnector, "r1-src", false);
        sender.init();
        Person person = new Person();
        person.setPenName("heymantest");

        for (int i = 0; i < 100; i++) {
            sender.sendMessage(person);
        }

        sender.close();

        AsyncWorker worker = new AsyncWorker() {
            public boolean doWork(Serializable message) {
                Person person = (Person) message;
                System.out.println(person.getPenName());
                return true;
            }
        };

        DefaultAsyncReceiver receiver = DefaultAsyncReceiver.createReceiver(consoleConnector,"r1-hornetq-target",false,3,worker);
        receiver.init();
        receiver.start();
        Thread.sleep(5000);
        
        receiver.close();
        consoleConnector.close();
    }
}
