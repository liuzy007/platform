package com.alibaba.napoli.client.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.client.async.AsyncWorker;

public class WorkerRetrySuccess implements AsyncWorker {
    private static final Log     logger          = LogFactory.getLog("WorkerRetrySuccess");

    public String                workerName      = "none";

    private int                  delayMiliSecond = 0;

    public boolean               logEnabled      = false;

    public int                   retryTimes      = 1;
    private AtomicInteger        accessNum       = new AtomicInteger();

    private Map<String, Integer> messages        = new HashMap<String, Integer>();

    public WorkerRetrySuccess() {

    }

    public WorkerRetrySuccess(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public WorkerRetrySuccess(int retryTimes, int delayMiliSecond) {
        this.retryTimes = retryTimes;
        this.delayMiliSecond = delayMiliSecond;
    }

    public boolean doWork(Serializable message) {
        if (logEnabled) {
            logger.warn("=========================WorkerRetrySuccess worker " + workerName
                    + " doWork(Serializable) yanny message " + accessNum.get() + ":" + message
                    + "==============================");
        }
        
        accessNum.incrementAndGet();

        try {
            if (this.delayMiliSecond > 0) {
                Thread.sleep(this.delayMiliSecond);
            }

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String key = message.toString();

        if (messages.containsKey(key)) {
            if (messages.get(key) >= retryTimes) {
                return true;
            } else {
                messages.put(key, messages.get(key) + 1);
                return false;
            }

        } else {
            messages.put(key, 1);
            return false;
        }
    }

    public int getAccessNum() {
        return accessNum.get();
    }

    public void resetAccessNum() {
        accessNum.lazySet(0);
    }
}
