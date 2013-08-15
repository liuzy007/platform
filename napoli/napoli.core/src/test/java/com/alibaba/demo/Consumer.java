/**
 * Project: napoli.client
 * 
 * File Created at 2009-7-7
 * $Id: Consumer.java 154477 2012-03-13 03:57:55Z haihua.chenhh $
 * 
 * Copyright 2008 Alibaba.com Croporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.demo;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.connector.NapoliConnector;

/**
 * @author xiaosong.liangxs
 */
public class Consumer {

    private int             clientSize;
    private int             connetionSize;
    private String          configAddress;
    private String          destination;

    private NapoliConnector connector;
    private AtomicInteger   counter = new AtomicInteger(0);

    private static String   STORE_PATH;

    /**
     * @param args
     * @throws NapoliClientException
     */
    public static void main(String[] args) throws NapoliClientException {
        Consumer consumer = new Consumer();
        consumer.setClientSize(Integer.valueOf(args[0]));
        consumer.setConnetionSize(Integer.valueOf(args[1]));
        consumer.setConfigAddress(args[2]);
        consumer.setDestination(args[3]);

        STORE_PATH = args[4];
        if (!STORE_PATH.endsWith("/")) {
            STORE_PATH = STORE_PATH + "/";
        }
        STORE_PATH = STORE_PATH + "receive/";
        consumer.start();
    }

    public void start() throws NapoliClientException {
        connector = new NapoliConnector();
        connector.setStorePath(STORE_PATH);
        connector.setAddress(configAddress);
        connector.init();
        final Receiver[] receivers = new Receiver[clientSize];
        for (int i = 0; i < receivers.length; i++) {
            receivers[i] = new Receiver(i);
        }

        final long start = System.currentTimeMillis();
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            int previous;
            int zeroTimes;

            public void run() {
                long currentTime = System.currentTimeMillis();
                int current = counter.get();
                if (current == previous) {
                    zeroTimes = zeroTimes + 1;
                    if (zeroTimes > 20) {
                        for (int i = 0; i < receivers.length; i++) {
                            if (receivers[i] != null) {
                                receivers[i].close();
                            }
                        }
                        System.out.println("...closing...");
                        connector.close();
                        System.exit(0);
                        return;
                    }
                } else {
                    zeroTimes = 0;
                }
                System.out.println("CONSUMER previous second:" + (current - previous) + ",avg:"
                        + (current * 1000.00 / (currentTime - start)) + ",sum:" + current);
                previous = current;
            }
        }, 1000, 1000);

        for (int i = 0; i < receivers.length; i++) {
            receivers[i].listening();
        }
    }

    /**
     * @param clientSize the clientSize to set
     */
    public void setClientSize(int clientSize) {
        this.clientSize = clientSize;
    }

    /**
     * @param connetionSize the connetionSize to set
     */
    public void setConnetionSize(int connetionSize) {
        this.connetionSize = connetionSize;
    }

    /**
     * @param configAddress the configAddress to set
     */
    public void setConfigAddress(String configAddress) {
        this.configAddress = configAddress;
    }

    /**
     * @param destination the destinationContext to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * @param connector the connector to set
     */
    public void setConnector(NapoliConnector connector) {
        this.connector = connector;
    }

    /**
     * @param counter the counter to set
     */
    public void setCounter(AtomicInteger counter) {
        this.counter = counter;
    }

    /**
     * @author xiaosong.liangxs
     */
    public class Receiver {
        private DefaultAsyncReceiver asyncReceiver;

        /**
         * @param i
         * @throws NapoliClientException
         */
        public Receiver(int index) throws NapoliClientException {
            this.asyncReceiver = new DefaultAsyncReceiver();
            this.asyncReceiver.setName(destination);
            this.asyncReceiver.setConnector(connector);
            this.asyncReceiver.setInstances(connetionSize);
            this.asyncReceiver.init();
        }

        /**
         * 
         */
        public void listening() {
            try {
                this.asyncReceiver.setWorker(new AsyncWorker() {
                    public boolean doWork(Serializable msg) {
                        counter.incrementAndGet();
                        return true;
                    }
                });
                this.asyncReceiver.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 
         */
        public void close() {
            try {
                this.asyncReceiver.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.asyncReceiver.close();
        }

    }
}
