/**
 * Project: napoli.client
 * 
 * File Created at Sep 16, 2009
 * $Id: SimpleEntry.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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
package com.alibaba.napoli.common.persistencestore.impl;

import java.util.Map;

/**
 * 没有打算要实现equal和hashCode方法。所以不要通用这个类，比如把这个类的对象存到List中，等等。
 * 
 * @author ding.lid
 */
class SimpleEntry<K, V> implements Map.Entry<K, V> {
    private final K key;
    private final V value;

    public SimpleEntry(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(final V value) {
        throw new UnsupportedOperationException();
    }
}
