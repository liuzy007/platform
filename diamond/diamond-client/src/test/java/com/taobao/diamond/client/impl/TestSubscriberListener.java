package com.taobao.diamond.client.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import com.taobao.diamond.client.SubscriberListener;
import com.taobao.diamond.configinfo.ConfigureInfomation;


public class TestSubscriberListener implements SubscriberListener {
    private String configureInfomation;
    private AtomicBoolean updateConfigInfo = new AtomicBoolean(false);


    public Executor getExecutor() {
        return null;
    }


    public void receiveConfigInfo(ConfigureInfomation configureInfomation) {
        this.configureInfomation = configureInfomation.getConfigureInfomation();
        updateConfigInfo.set(true);
    }


    public String getConfigureInfomation() {
        return configureInfomation;
    }


    public boolean getUpdateConfigInfo() {
        return updateConfigInfo.get();
    }


    public void setUpdateConfigInfo(boolean updateConfigInfo) {
        this.updateConfigInfo.set(updateConfigInfo);
    }
}
