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


public class SimpleIPFlowData {
    
    private AtomicInteger[] data;
    
    private int slotCount;

    private Timer timer = new Timer();

    class DefaultIPFlowDataManagerTask extends TimerTask {
        @Override
        public void run() {
            rotateSlot();
        }
    }


    public SimpleIPFlowData(int slotCount, int interval) {
        this.slotCount = slotCount;
        data = new AtomicInteger[slotCount];
        for (int i = 0; i < data.length; i++) {
            data[i] = new AtomicInteger(0);
        }
        timer.schedule(new DefaultIPFlowDataManagerTask(), interval, interval);
    }

    public int incrementAndGet(String ip) {
    	int index = 0;
    	if(ip!=null){
    		index = ip.hashCode()%slotCount;
    	}
        return data[index].incrementAndGet();
    }


    public void rotateSlot() {        
        for (int i = 0; i < slotCount; i++) {
        	data[i].set(0);
        }      
    }
    public int getCurrentCount(String ip) {
    	int index = 0;
    	if(ip!=null){
    		index = ip.hashCode()%slotCount;
    	}
        return data[index].get();
    }
}
