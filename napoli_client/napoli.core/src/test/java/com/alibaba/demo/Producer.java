/**
 * Project: napoli.client
 * 
 * File Created at 2009-7-7
 * $Id: Producer.java 154477 2012-03-13 03:57:55Z haihua.chenhh $
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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;

/**
 * @author xiaosong.liangxs
 */
public class Producer {

    private int             clientSize;
    private int             concurrentSize;
    private int             connetionSize;
    private int             sendTimes;
    private String          configAddress;
    private String          destination;
    private ExecutorService executorService;
    private NapoliConnector connector;
    private AtomicInteger   counter = new AtomicInteger(0);

    private static String   MESSAGE;
    private static String   STORE_PATH;

    public static void main(String[] args) throws NapoliClientException {
        Producer producer = new Producer();
        producer.setClientSize(Integer.parseInt(args[0]));
        producer.setConcurrentSize(Integer.parseInt(args[1]));
        producer.setConnetionSize(Integer.parseInt(args[2]));
        producer.setSendTimes(Integer.parseInt(args[3]));
        producer.setConfigAddress(args[4]);
        producer.setDestination(args[5]);
        MESSAGE = genrateMsg(Integer.parseInt(args[6]));
        STORE_PATH = args[7];
        if (!STORE_PATH.endsWith("/")) {
            STORE_PATH = STORE_PATH + "/";
        }
        STORE_PATH = STORE_PATH + "send/";
        producer.executorService = Executors.newFixedThreadPool(producer.clientSize
                * producer.concurrentSize);

        producer.start();

    }

    /**
     * @throws NapoliClientException
     */
    private void start() throws NapoliClientException {
        connector = new NapoliConnector();
        connector.setStorePath(STORE_PATH);
        connector.setAddress(configAddress);
        connector.init();
        final Sender[] senders = new Sender[clientSize];
        for (int i = 0; i < senders.length; i++) {
            senders[i] = new Sender(i);
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
                        for (int i = 0; i < senders.length; i++) {
                            if (senders[i] != null) {
                                senders[i].close();
                            }
                        }
                        System.out.println("...closing...");
                        connector.close();
                        executorService.shutdown();
                        System.exit(0);
                        return;
                    }
                } else {
                    zeroTimes = 0;
                }
                System.out.println("PRODUCER previous second:" + (current - previous) + ",avg:"
                        + (current * 1000.00 / (currentTime - start)) + ",sum:" + current);
                previous = current;
            }
        }, 1000, 1000);

        for (int i = 0; i < senders.length; i++) {
            senders[i].send();
        }

    }

    /**
     * @param clientSize the clientSize to set
     */
    public void setClientSize(int clientSize) {
        this.clientSize = clientSize;
    }

    /**
     * @param concurrentSize the concurrentSize to set
     */
    public void setConcurrentSize(int concurrentSize) {
        this.concurrentSize = concurrentSize;
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

    class Sender {
        private DefaultAsyncSender asyncSender;

        Sender(int index) throws NapoliClientException {
            asyncSender = new DefaultAsyncSender();
            asyncSender.setName(destination);
            asyncSender.setConnector(connector);
            asyncSender.setInstances(connetionSize);
            asyncSender.init();
        }

        /**
         * 
         */
        public void close() {
            if (asyncSender != null) {
                asyncSender.close();
            }

        }

        void send() {
            for (int i = 0; i < concurrentSize; i++) {
                executorService.submit(new Runnable() {
                    public void run() {
                        for (int j = 0; j < sendTimes; j++) {
                            boolean success = asyncSender.send(MESSAGE);
                            if (success) {
                                counter.incrementAndGet();
                            }
                        }
                    }
                });
            }

        }
    }

    /**
     * @param destination the destinationContext to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    private static String genrateMsg(int messageSize) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < messageSize; i++) {
            buffer.append('X');
        }
        return buffer.toString();
    }

    /**
     * @param sendTimes the sendTimes to set
     */
    public void setSendTimes(int sendTimes) {
        this.sendTimes = sendTimes;
    }

}
