package com.taobao.diamond.client.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import com.taobao.diamond.manager.ManagerListener;


public class TestManagerListener implements ManagerListener {
    private String configInfo;
    private AtomicBoolean updateConfigInfo = new AtomicBoolean(false);


    public Executor getExecutor() {
        return null;
    }


    public void receiveConfigInfo(String configInfo) {
        this.configInfo = configInfo;
        this.updateConfigInfo.set(true);
    }


    public String getConfigInfo() {
        return configInfo;
    }


    public boolean getUpdateConfigInfo() {
        return updateConfigInfo.get();
    }


    public void setUpdateConfigInfo(boolean updateConfigInfo) {
        this.updateConfigInfo.set(updateConfigInfo);
    }
}
