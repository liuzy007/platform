/**
 * Project: napoli.client
 * 
 * File Created at Sep 22, 2009
 * $Id: BdbPersistenceStoreTest.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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
package com.alibaba.napoli.client.inner.persistencestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.napoli.common.persistencestore.impl.BdbPersistenceStore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.napoli.client.util.NapoliTestUtil;

/**
 * @author ding.lid
 */
// 1. 测试其它方法！  2. 测试异常情况！
public class BdbPersistenceStoreTest {
    BdbPersistenceStore<String> bdbPersistenceStore;
    String                      storePath;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        storePath = "./target/BDB_DATA_FOR_UT_" + System.nanoTime();

        bdbPersistenceStore = new BdbPersistenceStore<String>(String.class);
        bdbPersistenceStore.setBdbStorePath(storePath);
        bdbPersistenceStore.init();
    }

    @After
    public void tearDown() throws Exception {
        bdbPersistenceStore.close();

        NapoliTestUtil.rDel(storePath);
    }

    @Test
    public void testWhenNoWrite() {
        final File storeDir = new File(storePath);

        assertTrue(storeDir.exists());

        final Entry<String, String> read = bdbPersistenceStore.read();
        assertEquals(null, read);

        final Map<String, String> batchRead = bdbPersistenceStore.batchRead(10);
        assertEquals(0, batchRead.size());
    }

    @Test
    public void testWrite_Read() {
        final String data = "DATA1";
        bdbPersistenceStore.write(data);

        String key = null;
        // 数据可以重复读，所以下面重复读3遍
        for (int i = 0; i < 3; ++i) {
            final Entry<String, String> read = bdbPersistenceStore.read();
            assertEquals(data, read.getValue());

            if (null == key) {
                key = read.getKey();
            } else {
                assertEquals(key, read.getKey());
            }
        }
    }

    @Test
    public void testWrite_BatchRead() {
        final String data = "DATA";
        final String data0 = "DATA0";
        final String data1 = "DATA1";

        bdbPersistenceStore.write(data0);
        bdbPersistenceStore.write(data1);

        // 数据可以重复读，所以下面重复读3遍
        for (int i = 0; i < 3; ++i) {
            final Map<String, String> batchRead = bdbPersistenceStore.batchRead(1);

            assertEquals(1, batchRead.size());
            assertTrue(batchRead.containsValue(data0) || batchRead.containsValue(data1));
        }

        // 数据可以重复读，所以下面重复读3遍
        for (int i = 0; i < 3; ++i) {
            final Map<String, String> batchRead = bdbPersistenceStore.batchRead(2);

            assertEquals(2, batchRead.size());
            assertTrue(batchRead.containsValue(data0));
            assertTrue(batchRead.containsValue(data1));
        }

        // 数据可以重复读，所以下面重复读3遍
        for (int i = 0; i < 3; ++i) {
            final Map<String, String> batchRead = bdbPersistenceStore.batchRead(100);

            assertEquals(2, batchRead.size());
            assertTrue(batchRead.containsValue(data0));
            assertTrue(batchRead.containsValue(data1));
        }

        for (int i = 2; i < 51; i++) {
            bdbPersistenceStore.write(data + i);
        }

        for (int i = 0; i < 3; ++i) {
            final Map<String, String> batchRead = bdbPersistenceStore.batchRead(50);
            assertEquals(50, batchRead.size());
        }
    }

    public void testBatchWrite_Read() {
        final List<String> dataList = new ArrayList<String>();
        for (int i = 0; i < 10; ++i) {
            dataList.add("DATA" + i);
        }
        bdbPersistenceStore.batchWrite(dataList);

        final Map<String, String> batchRead = bdbPersistenceStore.batchRead(100);
        assertEquals(10, batchRead);

        final List<String> readDataList = new ArrayList<String>(batchRead.values());
        Collections.sort(readDataList);
        assertEquals(dataList, readDataList);
    }

    @Test
    public void testDelete() {
        final String data0 = "DATA0";
        final String data1 = "DATA1";

        bdbPersistenceStore.write(data0);
        bdbPersistenceStore.write(data1);

        final Map<String, String> batchRead1 = bdbPersistenceStore.batchRead(2);
        assertEquals(2, batchRead1.size());

        final Iterator<String> iterator = batchRead1.keySet().iterator();
        final String key1 = iterator.next();
        bdbPersistenceStore.delete(key1);
        iterator.remove();
        final String key2 = iterator.next();

        final Map<String, String> batchRead2 = bdbPersistenceStore.batchRead(2);
        assertEquals(1, batchRead2.size());
        assertTrue(batchRead2.containsKey(key2));
    }
}
