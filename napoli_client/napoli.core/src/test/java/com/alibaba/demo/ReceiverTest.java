package com.alibaba.demo;

import com.alibaba.napoli.ResourceConstant;
import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.receiver.Receiver;
import java.io.Serializable;

/**
 * User: heyman Date: 2/1/12 Time: 4:09 下午
 */
public class ReceiverTest {

    public void testHeartBeatWhenMqShutdown() throws Exception {
        AsyncWorker worker = new AsyncWorker() {
            public boolean doWork(Serializable message) {
                System.out.println("receive the message:" + message);
                return true;
            }
        };
        NapoliConnector consoleConnector = NapoliConnector.createConnector(ResourceConstant.shConsole);
        Receiver receiver = DefaultAsyncReceiver.createReceiver(consoleConnector,"hqtest", false, 5, worker);
        receiver.start();
        System.out.println("start....");
    }

    public static void main(String[] args) throws Exception {
        AsyncWorker worker = new AsyncWorker() {
            public boolean doWork(Serializable message) {
                System.out.println("receive the message:" + message);
                return true;
            }
        };
        NapoliConnector consoleConnector = NapoliConnector.createConnector(ResourceConstant.shConsole);
        Receiver receiver = DefaultAsyncReceiver.createReceiver(consoleConnector,"heymantest", false, 5, worker);
        receiver.start();
        System.out.println("start....");

    }
}
