/**
 * Project: napoli.client File Created at 2009-6-15 $Id: NapoliConnector.java 14007 2009-06-18 05:54:40Z guolin.zhuanggl
 * $ Copyright 2008 Alibaba.com Croporation Limited. All rights reserved. This software is the confidential and
 * proprietary information of Alibaba Company. ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the license agreement you entered into with
 * Alibaba.com.
 */
package com.alibaba.napoli.client.connector;

import com.alibaba.napoli.connector.ConsoleConnector;

/**
 * 用于获得一个Destination关联的Pool和ConfigLoader。
 * 
 * @author guolin.zhuanggl
 */
public class NapoliConnector extends ConsoleConnector {

    private String userName = "napoli";
    private String password = "napoli";

    public NapoliConnector() {
        super();
    }

    public NapoliConnector(String address) {
        super(address);
    }

    public NapoliConnector(String address, int sendPoolSize, int prefetch) {
        super(address, sendPoolSize, prefetch);
    }

    public synchronized void stop() {
        //only receiver support stop

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static NapoliConnector createConnector(String address, int sendPoolSize, int prefetch) {
        NapoliConnector consoleConnector = new NapoliConnector();
        consoleConnector.setAddress(address);
        consoleConnector.setPoolSize(sendPoolSize);
        consoleConnector.setPrefetch(prefetch);
        consoleConnector.init();
        return consoleConnector;
    }

    public static NapoliConnector createConnector(String address) {
        NapoliConnector consoleConnector = new NapoliConnector();
        consoleConnector.setAddress(address);
        consoleConnector.init();
        return consoleConnector;
    }

    /*public DefaultAsyncSender createSender(String destination, boolean storeable) throws NapoliClientException {
        DefaultAsyncSender defaultSender = new DefaultAsyncSender();
        defaultSender.setConnector(this);
        defaultSender.setName(destination);
        defaultSender.setStoreEnable(storeable);
        defaultSender.init();
        return defaultSender;
    }*/

    /*public DefaultAsyncReceiver createReceiver(String destination, boolean storeable, int instances, NapoliWorker worker)
            throws NapoliClientException {
        return createReceiver(destination, storeable, instances, worker, null);
    }*/

    /*public DefaultAsyncReceiver createReceiver(String destination, boolean storeable, int instances,
                                               NapoliWorker worker, String group) throws NapoliClientException {
        DefaultAsyncReceiver defaultReceiver = new DefaultAsyncReceiver();
        defaultReceiver.setConnector(this);
        defaultReceiver.setName(destination);
        defaultReceiver.setStoreEnable(storeable);
        defaultReceiver.setInstances(instances);
        defaultReceiver.setWorkerPri(worker);
        if (group != null) {
            defaultReceiver.setGroup(group);
        }
        defaultReceiver.init();
        return defaultReceiver;
    }*/
}
