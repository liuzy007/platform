/**
 * Project: napoli.client
 * 
 * File Created at Sep 15, 2009
 * $Id: PersistenceStore.java 166475 2012-04-23 09:38:23Z haihua.chenhh $
 * 
 * Copyright 2008 Alibaba.com Croporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.napoli.common.persistencestore;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 持久化仓库的接口。
 * 
 * @author ding.lid
 * @author guolin.zhuanggl
 */
public interface PersistenceStore<T extends Serializable> {
    /**
     * 写入一条数据。会自动生成一个与之对应的key。
     * 
     * @param data
     */
    void write(T data);

    void update(final String key,final T object);

    /**
     * 写入多条数据。
     * 
     * @param dataList
     */
    void batchWrite(List<T> dataList);

    /**
     * 读一条数据。<br>
     * 如果读多条数据的情况下，尽量使用{@link #batchRead(int)}方法，有更好的效率。
     * 
     * @return 读到的数据。如果store中没有数据，则返回<code>null</code>。
     */
    Map.Entry<String, T> read();

    /**
     * 读多条数据。
     * 
     * @param count 要读数据的条数
     * @return 读到的数据。如果store中没有数据，则返回空的Map(即size ==0)。
     */
    Map<String, T> batchRead(int count);

    /**
     * 删除一条数据。
     * 
     * @param key
     */
    void delete(String key);

    /**
     * 删除多条数据。
     * 
     * @param keys 要删除数据的Key
     */
    void delete(List<String> keys);

    long getSize();
}
