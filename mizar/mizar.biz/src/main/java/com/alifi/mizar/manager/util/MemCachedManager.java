package com.alifi.mizar.manager.util;

import java.util.Date;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

public class MemCachedManager {
    
    private String[] servers;   //memCached服务器地址
    
    private Integer[] weights;  //memCached各个服务器之间的权重值

    private boolean failover;   //是否在一台服务器当掉后自动连接到另外一台服务器

    private boolean failback;   //在failover到另外一台服务器时是否监视原服务器

    private int initConn;       //初始连接数

    private int minConn;        //最小连接数

    private int maxConn;        //最大连接数

    private long maxIdle;       //最大空闲时间

    private int maintSleep;     //连接池保持线程的休眠时间，每隔x秒会唤醒该线程以保持连接池

    private boolean nagle;      //是否启用nagle算法

    private int socketTO;       //socket读取超时时间

    private int socketConnectTO;    //socket连接超时时间

    private MemCachedClient memCachedclient;

    public void init() {
        SockIOPool pool = SockIOPool.getInstance();
        pool.setServers(servers);
        pool.setWeights(weights);
        pool.setFailover(failover);
        pool.setFailback(failback);
        pool.setInitConn(initConn);
        pool.setMinConn(minConn);
        pool.setMaxConn(maxConn);
        pool.setMaxIdle(maxIdle);
        pool.setMaintSleep(maintSleep);
        pool.setNagle(nagle);
        pool.setSocketTO(socketTO);
        pool.setSocketConnectTO(socketConnectTO);
        pool.initialize();
        memCachedclient = new MemCachedClient();
    }

    public Object get(String key) {
        return memCachedclient.get(key);
    }
    
    //仅当存储空间中不存在键相同的数据时才保存
    public boolean add(String key, Object value) {
        return memCachedclient.add(key, value);
    }
    
    public boolean add(String key, Object value, Date expiry) {
        return memCachedclient.add(key, value, expiry);
    }
    
    //仅当存储空间中存在键相同的数据时才保存
    public boolean replace(String key, Object value) {
        return memCachedclient.replace(key, value);
    }
    
    public boolean replace(String key, Object value, Date expiry) {
        return memCachedclient.replace(key, value, expiry);
    }
    
    //不管是否存在键相同的数据，都会保存
    public boolean set(String key, Object value) {
        return memCachedclient.set(key, value);
    }
    
    public boolean set(String key, Object value, Date expiry) {
        return memCachedclient.set(key, value, expiry);
    }
    
    public boolean delete(String key) {
        return memCachedclient.delete(key);
    }
    
    public boolean delete(String key, Date expiry) {
        return memCachedclient.delete(key, expiry);
    }

    public void setServers(String servers) {
        this.servers = servers.split(",");
    }
    
    public void setWeights(String weights) {
        String[] weightArray = weights.split(",");
        this.weights = new Integer[weightArray.length];
        for (int i = 0, j = weightArray.length; i < j; i++) {
            try {
                this.weights[i] = Integer.valueOf(weightArray[i].trim());
            } catch (NumberFormatException e) {
                this.weights[i] = null;
            }
        }
    }

    public void setFailover(boolean failover) {
        this.failover = failover;
    }

    public void setFailback(boolean failback) {
        this.failback = failback;
    }

    public void setInitConn(int initConn) {
        this.initConn = initConn;
    }

    public void setMinConn(int minConn) {
        this.minConn = minConn;
    }

    public void setMaxConn(int maxConn) {
        this.maxConn = maxConn;
    }

    public void setMaxIdle(long maxIdle) {
        this.maxIdle = maxIdle;
    }

    public void setMaintSleep(int maintSleep) {
        this.maintSleep = maintSleep;
    }

    public void setNagle(boolean nagle) {
        this.nagle = nagle;
    }

    public void setSocketTO(int socketTO) {
        this.socketTO = socketTO;
    }

    public void setSocketConnectTO(int socketConnectTO) {
        this.socketConnectTO = socketConnectTO;
    }
}
