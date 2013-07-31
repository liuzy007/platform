package com.alibaba.napoli.mqImpl;

/**
 * User: heyman
 * Date: 12/7/11
 * Time: 10:08 上午
 */
public interface NapoliConnection<T> {   
    T getConnection();
    
    void close();

    long getLastUsedTime();

    boolean isIdle();

    boolean hasCapacity();  //是否能创建session

    void returnCapacity();  //将session返回给Capacity

    boolean isClose();

}