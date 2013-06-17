/**
 * Project: napoli_demo File Created at 2011-1-5 $Id: MyWorker.java 149160 2012-02-27 06:34:18Z haihua.chenhh $ Copyright 2008 Alibaba.com Croporation Limited. All rights
 * reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.napoli.client.model;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Date;

import com.alibaba.napoli.client.async.AsyncWorker;

import com.sleepycat.je.DatabaseException;

/**
 * 实现AsyncWorker接口，处理非事务消息 该类必须是线程安全的。
 * 
 * @author rain.chenjr
 */
public class MyWorker implements AsyncWorker {
    private double            delaySecond = 0;
    private AtomicInteger     accessNum   = new AtomicInteger();
    private Object            lock        = new Object();

    public boolean            logEnabled  = false;
    public String             workerName  = "none";
    int                       batchSize   = 1000;
    public boolean            isPerf      = false;

    private ArrayList<String> receivedIds = new ArrayList<String>();

    public MyWorker() {

    }

    public MyWorker(double delay) {
        this.delaySecond = delay;
    }

    public MyWorker(double delay, int batchSize) {
        this.delaySecond = delay;
        this.batchSize = batchSize;
    }

    public boolean doWork(Serializable msg) {
        if (msg == null) {
            return false;
        }

        try {
            Thread.sleep((int) (1000 * this.delaySecond));

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (this.logEnabled) {
            if (msg instanceof Person) {
                System.out.println((new Date()).toString() + " yanny " + this.workerName + " handles Person object message " + ((Person)msg).getPersonId()
                        + " at No." + accessNum.get());
            } else {
                System.out.println((new Date()).toString() + " yanny " + this.workerName + " handles message " + msg
                        + " at No." + accessNum.get());
            }
        }

        accessNum.incrementAndGet();

        synchronized (lock) {
            if (accessNum.get() % batchSize == 0) {
                System.out.println((new Date()).toString() + " yanny " + this.workerName + " handles message " + msg
                        + " at No." + accessNum.get());
            }
            if (!isPerf) {
                receivedIds.add(msg.toString());
            }
        }

        return true;
    }

    public boolean doWork(Serializable msg, String dd) throws DatabaseException {
        return false;
    }

    public int getAccessNum() {
        return accessNum.get();
    }

    public void resetAccessNum() {
        accessNum.lazySet(0);
    }

    public ArrayList<String> getReceivedIds() {
        return this.receivedIds;
    }

    public void resetReceivedIds() {
        this.receivedIds.clear();
    }

    public void reset() {
        this.resetAccessNum();
        this.resetReceivedIds();

    }

    public void tillNoConsume() {
        do {
            int count = this.getAccessNum();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            int newCount = this.getAccessNum();
            if (count == newCount) {
                break;
            }
        } while (true);
    }

}
