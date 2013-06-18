/**
 * Project: napoli.client
 * 
 * File Created at 2011-9-19
 * $Id: MockStore.java 176848 2012-06-05 06:04:18Z haihua.chenhh $
 * 
 * Copyright 1999-2100 Alibaba.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.napoli.client.mock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.client.async.inner.StoreItem;
import com.alibaba.napoli.common.persistencestore.PersistenceStore;

public class MockStore implements PersistenceStore<StoreItem> {



    public Map<String, StoreItem> depot;

    public long getSize() {
        return 0;  
    }

    public MockStore(Map<String, StoreItem> src){
        this.depot = src;
    }

    @SuppressWarnings("unchecked")
    public Map<String, StoreItem> batchRead(int count) {
        int sub = depot.size() - count;
        Map<String, StoreItem> ret = new HashMap<String, StoreItem>(depot);
        Iterator it = ret.entrySet().iterator();
        for (int i = 0; i < sub && it.hasNext(); i++) {
            it.next();
            it.remove();
        }
        return ret;
    }

    public void batchWrite(List<StoreItem> dataList) {
        for (int i = 0; i < dataList.size(); i++) {
            StoreItem item = dataList.get(i);
            write(item);
        }
    }

    public void delete(String key) {
        depot.remove(key);
    }

    public void delete(List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            String item = keys.get(i);
            delete(item);
        }
    }

    public Entry<String, StoreItem> read() {
        return depot.entrySet().iterator().next();
    }

    public void  write(StoreItem data) {
        depot.put(NapoliTestUtil.createName(this.getClass().getName()), data);
    }

    public void update(String key, StoreItem object) {
    }
}

