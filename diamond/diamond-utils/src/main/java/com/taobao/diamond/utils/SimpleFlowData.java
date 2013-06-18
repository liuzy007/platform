/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;


public class SimpleFlowData {
    private int index = 0;
    private AtomicInteger[] data;
    private int average;
    private int slotCount;

    private Timer timer = new Timer();

    class DefaultFlowDataManagerTask extends TimerTask {
        @Override
        public void run() {
            rotateSlot();
        }
    }


    public SimpleFlowData(int slotCount, int interval) {
        this.slotCount = slotCount;
        data = new AtomicInteger[slotCount];
        for (int i = 0; i < data.length; i++) {
            data[i] = new AtomicInteger(0);
        }
        timer.schedule(new DefaultFlowDataManagerTask(), interval, interval);
    }


    public int addAndGet(int count) {
        return data[index].addAndGet(count);
    }


    public int incrementAndGet() {
        return data[index].incrementAndGet();
    }


    public void rotateSlot() {
        int total = 0;

        for (int i = 0; i < slotCount; i++) {
            total += data[i].get();
        }

        average = total / slotCount;

        index = (index + 1) % slotCount;
        data[index].set(0);
    }


    public int getCurrentCount() {
        return data[index].get();
    }


    public int getAverageCount() {
        return this.average;
    }


    public int getSlotCount() {
        return this.slotCount;
    }


    public String getSlotInfo() {
        StringBuilder sb = new StringBuilder();

        int index = this.index + 1;

        for (int i = 0; i < slotCount; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(this.data[(i + index) % slotCount].get());
        }
        return sb.toString();
    }


    public int getCount(int prevStep) {
        prevStep = prevStep % this.slotCount;
        int index = (this.index + this.slotCount - prevStep) % this.slotCount;
        return this.data[index].intValue();
    }

}
