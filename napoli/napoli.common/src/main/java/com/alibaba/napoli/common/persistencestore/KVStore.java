package com.alibaba.napoli.common.persistencestore;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.inner.StoreItem;

import java.util.Map;

/**
 * User: heyman
 * Date: 12/1/11
 * Time: 11:54 上午
 */
public interface KVStore {
    void storeMessage(NapoliMessage napoliMessage);
    void update(String key,NapoliMessage napoliMessage);

    Map<String, StoreItem> batchRead(int batchReadCount);

    String getName();

    void delete(String key);
    
    long getStoreSize();
    
    void close();
    
    void clear();

}
