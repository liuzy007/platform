package com.alibaba.napoli.common.persistencestore;

import java.util.concurrent.ConcurrentHashMap;

/**
 * User: heyman
 * Date: 12/8/11
 * Time: 5:49 下午
 */
public class KVStoreContext {
    private static ConcurrentHashMap<String, KVStore> kvStoreMap = new ConcurrentHashMap<String, KVStore>();

    public void putKVStore(String name, KVStore kvStore) {
        kvStoreMap.putIfAbsent(name, kvStore);
    }

    public KVStore getKVStore(String name) {
        return kvStoreMap.get(name);
    }
}
