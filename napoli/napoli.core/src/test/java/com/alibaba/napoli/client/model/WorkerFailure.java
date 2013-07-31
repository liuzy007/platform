package com.alibaba.napoli.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.client.async.AsyncWorker;

public class WorkerFailure implements AsyncWorker {
    private AtomicInteger     accessNum        = new AtomicInteger();
    private static Object     lock             = new Object();
    public boolean            logEnabled       = false;
    private static final Log  logger           = LogFactory.getLog("WorkerFailure");

    public String             workerName       = "none";

    private ArrayList<String> receivedMessages = new ArrayList<String>();

    public boolean doWork(Serializable message) {
        if (logEnabled) {
            logger.warn("=========== WorkerFailure worker " + workerName + " doWork(Serializable) yanny message is "
                    + message + "==============================" + accessNum.get());
        }
        
        accessNum.incrementAndGet();

        synchronized (lock) {
            if (!receivedMessages.contains(message.toString())) {
                receivedMessages.add(message.toString());
            }
        }

        return false;
    }

    public int getAccessNum() {
        return accessNum.get();
    }

    public void resetAccessNum() {
        accessNum.lazySet(0);
        receivedMessages.clear();
    }

    public int getMessageCount() {
        return this.receivedMessages.size();
    }
}
