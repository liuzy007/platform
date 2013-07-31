/**
 * Project: napoli.client File Created at Sep 18, 2009 $Id: NapoliClient.java 189920 2012-07-25 06:56:28Z haihua.chenhh $ Copyright 2008 Alibaba.com Croporation Limited. All rights
 * reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.napoli.client;

//import com.alibaba.dragoon.stat.napoli.NapoliStatManager;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.connector.DestinationContext;
import com.alibaba.napoli.domain.client.ClientDestination;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author haihua.chenhh
 */
public abstract class NapoliClient {

    private static final Log log = LogFactory.getLog(NapoliClient.class);

    protected NapoliConnector connector;
    protected String name;
    protected long reprocessInterval = 10 * 1000;
    protected boolean storeEnable = true;

    protected DestinationContext destinationContext;
    protected Runnable        heartbeatSchedule;

    // protected volatile ScheduledFuture<?> reprocessFuture;

    protected volatile boolean closed = false;
    protected volatile boolean started = false;
    //protected volatile boolean isStoreOpened = false;  
    protected String filterChain = "monitor,bdbStore";

    public static final String                              PROP_NAME              = "name";
    public static final String                              PROP_REPROCESSINTERVAL = "reprocessInterval";
    public static final String                              PROP_STOREENABLE       = "storeEnable";

    public void init() throws NapoliClientException {
        if (null == name || "".equals(name)) {
            throw new IllegalStateException("memeber name is null or empty!");
        }

        if (null == connector) {
            throw new IllegalStateException("memeber connector is null!");
        }
        //注册destination
        try {
            ClientDestination theDestination = connector.getConfigService().fetchDestination(name);
            //ClientDestination clientDestination = JsonUtil.fromDestination(theDestination);
            if (theDestination == null) {
                throw new RuntimeException("Destination[" + name + "] may not exist.");
            }
            destinationContext = new DestinationContext(theDestination);
            setName(theDestination.getName());
            connector.addDestination(name, destinationContext);
        } catch (Throwable e) {
            throw new IllegalStateException("Destination[" + name + "] may not exist.",e);
        }
        closed = false;
    }

    public boolean isClosed() {
        return closed;
    }

    public synchronized void close() {
        closed = true;
    }
    
    public boolean isStarted() {
		return started;
	}

	public void setConnector(NapoliConnector connector) {
        this.connector = connector;
    }
	
	public NapoliConnector getConnector() {
        return this.connector;
    }	

    public void setName(String name) {
        this.name = name;
    }

    public void setReprocessInterval(long reprocessInterval) {
        this.reprocessInterval = reprocessInterval;
    }

    public void setStoreEnable(boolean storeEnable) {
        this.storeEnable = storeEnable;
    }

    public String getName() {
        return name;
    }
  
    public DestinationContext getDestinationContext(){
    	return this.destinationContext;
    }

    public void setFilterChain(String filterChain) {
        this.filterChain = filterChain;
    }

    public Runnable getHeartbeatSchedule() {
        return heartbeatSchedule;
    }
}
