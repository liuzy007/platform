/**
 * Project: napoli.client
 * 
 * File Created at Sep 15, 2009
 * $Id: BdbPersistenceStore.java 166475 2012-04-23 09:38:23Z haihua.chenhh $
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

import com.alibaba.napoli.common.persistencestore.PersistenceException;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.common.persistencestore.PersistenceStore;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentMutableConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

/**
 * @author guolin.zhuanggl
 * @author ding.lid
 */
public class BdbPersistenceStore<T extends Serializable> implements PersistenceStore<T> {
    private static final Log    log                  = LogFactory.getLog(BdbPersistenceStore.class);

    private final static String MESSAGE_DBNAME       = "MESSAGE_DB";
    private final static String MESSAGE_CLASS_DBNAME = "MESSAGE_CLASS_DB";
    //当数据对象大于该值时，打日志提醒
    private int                 MAX_OBJECT_SIZE       = 100*1024;

    private String              bdbStorePath;
    // 默认值 10M
    private long                bdbCheckpointBytes   = 10 * 1024 * 1024;
    // 默认值 5M
    private long                bdbCacheSize         = 5 * 1024 * 1024;
 
    private boolean 			txnNoSync            = false;
    private boolean             txnWriteNoSync       = true;

    private Environment         bdbEnvironment;
    private Database            bdb;
    private StoredClassCatalog  bdbClassCatalog;

    private final Class<T>      dataClass;

    public String getBdbStorePath() {
        return bdbStorePath;
    }

    /**
     * 设定BDB数据的存储目录。如果这个目录不存在，会被自动创建。
     * 
     * @param storePath BDB数据的存储目录
     */
    public void setBdbStorePath(final String storePath) {
        this.bdbStorePath = storePath;
    }

    public long getBdbCheckpointBytes() {
        return bdbCheckpointBytes;
    }

    /**
     * 设置DBD的CheckpointBytes参数。默认值 10M。
     */
    public void setBdbCheckpointBytes(final long dbdCheckpointBytes) {
        this.bdbCheckpointBytes = dbdCheckpointBytes;
    }

    public long getBdbCacheSize() {
        return bdbCacheSize;
    }

    /**
     * 设置DBD的CacheSize参数。默认值 5M。
     */
    public void setBdbCacheSize(final long dbdCacheSize) {
        this.bdbCacheSize = dbdCacheSize;
    }

    public void init() throws DatabaseException {
       init(false);
    }

    public void init(boolean readonly) throws DatabaseException {
        if (null == bdbStorePath) {
            throw new IllegalStateException("Member bdbStorePath is null!");
        }

        open(readonly);
    }
    
    public long browserBdb(){
        Cursor cursor = null;
        long readNum = 0;
        try {
            cursor = bdb.openCursor(null, null);
            final DatabaseEntry foundKey = new DatabaseEntry();
            final DatabaseEntry foundData = new DatabaseEntry();

            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                readNum++;
            }

            if (log.isTraceEnabled()) {
                log.trace("read object from db, count= " + readNum);
            }
        } catch (final DatabaseException e) {
            log.error("Don't worry, read message from db error, " + "late read again: "
                    + e.getMessage(),e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (final DatabaseException e) {
                    // ignore
                }
            }
        }
        return readNum;
    }
    
        
    private void open(boolean readOnly) throws DatabaseException {
        final File bdbDir = new File(bdbStorePath);
        if (!bdbDir.exists()) {
            if (!bdbDir.mkdirs()) {
                throw new RuntimeException("Fail to create the store directory(" + bdbStorePath
                        + ") for bdb persistence store!");
            }
        }

//        final boolean readOnly = false;

        final EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setReadOnly(readOnly);
        envConfig.setTxnNoSync(txnNoSync);
        envConfig.setTxnWriteNoSync(txnWriteNoSync);
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true); // must setting
        // checkpoint occupied after data file increase some bytes
        envConfig.setConfigParam("je.checkpointer.bytesInterval", String
                .valueOf(bdbCheckpointBytes));

        final EnvironmentMutableConfig envMutableConfig = new EnvironmentMutableConfig();
        envMutableConfig.setCacheSize(bdbCacheSize);

        bdbEnvironment = new Environment(bdbDir, envConfig);
        bdbEnvironment.setMutableConfig(envMutableConfig);

        final DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setReadOnly(readOnly);
        dbConfig.setAllowCreate(!readOnly);
        dbConfig.setSortedDuplicates(false);
        dbConfig.setTransactional(true);

        bdb = bdbEnvironment.openDatabase(null, MESSAGE_DBNAME, dbConfig);

        // create class info db
        final Database classDb = bdbEnvironment.openDatabase(null, MESSAGE_CLASS_DBNAME, dbConfig);
        bdbClassCatalog = new StoredClassCatalog(classDb);
    }

    public void close() throws DatabaseException {
        // close db first, then close enviroment
        if (null != bdb) {
            try{
                bdb.close();
            }catch(Throwable e){
                if(log.isWarnEnabled()){
                    log.warn("bdb close error, " + e.getMessage());
                }
            }
            bdb = null;
        }

        if (null != bdbClassCatalog) {
            try {
                bdbClassCatalog.close();
            } catch (Throwable e) {
                if(log.isWarnEnabled()){
                    log.warn("bdbClassCatalog close error, " + e.getMessage());
                }
            }
            bdbClassCatalog = null;
        }

        if (bdbEnvironment != null) {
            try {
                bdbEnvironment.cleanLog();
                bdbEnvironment.close();
            } catch (Throwable e) {
                if(log.isWarnEnabled()){
                    log.warn("bdbEnvironment close error, " + e.getMessage());
                }
            }

            bdbEnvironment = null;
        }
    }

    /**
     * 也可以使用 {@link #createBdbPersistenceStore(Class)} 来获得一个实例。
     * 
     * @param clazz 存储数据的类型的class。
     */
    public BdbPersistenceStore(final Class<T> clazz) {
        dataClass = clazz;
    }

    /**
     * 生成BdbPersistenceStore工厂方法。功能上和构造函数一样，形式上简单些。
     * 
     * @param <DT> 存储数据的类型。
     * @param dataType 存储数据的类型的class。
     * @return 返回存储该数据类型的BdbPersistenceStore的泛型对象。
     */
    public static <DT extends Serializable> BdbPersistenceStore<DT> createBdbPersistenceStore(
                                                                                              final Class<DT> dataType) {
        return new BdbPersistenceStore<DT>(dataType);
    }

    /**
     * 使用UUID字符串产生DatabaseEntry作为Key。
     */
    private static DatabaseEntry generateKeyEntry() {
        final String uuid = UUID.randomUUID().toString();
        return generateKeyEntry(uuid);
    }

    /**
     * 根据一个字符串产生DatabaseEntry作为Key。
     */
    private static DatabaseEntry generateKeyEntry(final String key) {
        final DatabaseEntry entry = new DatabaseEntry();
        final EntryBinding keyBinding = TupleBinding.getPrimitiveBinding(String.class);
        keyBinding.objectToEntry(key, entry);
        return entry;
    }

    /**
     * 从DatabaseEntry还原出Key
     */
    private static String restoreKey(final DatabaseEntry entry) {
        final EntryBinding keyBinding = TupleBinding.getPrimitiveBinding(String.class);
        return (String) keyBinding.entryToObject(entry);
    }

    private DatabaseEntry generateDataEntry(final Object object) {
        final DatabaseEntry entry = new DatabaseEntry();
        final EntryBinding dataBinding = new SerialBinding(bdbClassCatalog, dataClass);
        dataBinding.objectToEntry(object, entry);

        return entry;
    }

    /**
     * 从DatabaseEntry还原出数据
     */
    @SuppressWarnings("unchecked")
    private T restoreData(final DatabaseEntry entry) {
        final EntryBinding dataBinding = new SerialBinding(bdbClassCatalog, dataClass);
        final T data = (T) dataBinding.entryToObject(entry);
        return data;
    }

    /**
     * 写入一个对象。会自动生成一个与之对应的key。 <br>
     * 如果出错，写入失败，忽略掉！
     */
    // FIXME 忽略出错？ 是否有其它的解决办法？？
    public void  write(final T object){
        if (object != null) {
            try {
                DatabaseEntry value = generateDataEntry(object);
                bdb.put(null, generateKeyEntry(), value);

                if (value.getSize() > MAX_OBJECT_SIZE) {
                    log.error("msg size[" + value.getSize() + "] is over 100K, please check it.");
                }
                if (log.isTraceEnabled()) {
                    log.trace("write object to db: " + object);
                }
            } catch (final DatabaseException e) {
                throw new PersistenceException("write object["+object+"] to bdb error!",e);
            }
        } else{
            throw new IllegalArgumentException("the object is null");
        }
    }
    
    public void update(final String key,final T object){
        if (object != null) {
            try {
                DatabaseEntry value = generateDataEntry(object);
                bdb.put(null, generateKeyEntry(key), value);

                if (value.getSize() > MAX_OBJECT_SIZE) {
                    log.error("msg size[" + value.getSize() + "] is over 100K, please check it.");
                }
                if (log.isTraceEnabled()) {
                    log.trace("write object to db: " + object);
                }
            } catch (final DatabaseException e) {
                throw new PersistenceException("write object["+object+"] to bdb error!",e);
            }
        } else{
            throw new IllegalArgumentException("the object is null");
        }
    }

    /**
     * 写入多个对象。 <br>
     * 如果出错，写入失败，忽略掉！
     */
    // FIXME 忽略出错？ 是否有其它的解决办法？？忽略write的返回值，可能会写失败
    public void batchWrite(final List<T> objectList) {
        for (final T obj : objectList) {
            write(obj);
        }
    }

    /**
     * 读数据。
     * 
     * @return 返回读到的数据。 如果store中没有数据、或是读出错，则返回null。
     */
    public Entry<String, T> read() {
        SimpleEntry<String, T> entry = null;
        Cursor cursor = null;

        try {
            cursor = bdb.openCursor(null, null);
            final DatabaseEntry foundKey = new DatabaseEntry();
            final DatabaseEntry foundData = new DatabaseEntry();

            if (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                entry = new SimpleEntry<String, T>(restoreKey(foundKey), restoreData(foundData));
            }

            if (log.isTraceEnabled()) {
                log.trace("read object from db");
            }
        } catch (final DatabaseException e) {
            log.error("Don't worry, read message from db error, " + "late read again: "
                    + e.getMessage(),e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (final DatabaseException e) {
                    // ignore
                }
            }
        }

        return entry;
    }

    /**
     * 读数据。
     * 
     * @return 返回读到的数据。 如果store中没有数据、或是读出错，则返回空的Map（即size为0的Map）。
     */
    public Map<String, T> batchRead(final int count) {
        final Map<String, T> map = new HashMap<String, T>(count);
        Cursor cursor = null;

        try {
            cursor = bdb.openCursor(null, null);
            final DatabaseEntry foundKey = new DatabaseEntry();
            final DatabaseEntry foundData = new DatabaseEntry();

            int readedNum = 0;
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS
                    && readedNum < count) {
                map.put(restoreKey(foundKey), restoreData(foundData));
                readedNum++;
            }

            if (log.isTraceEnabled()) {
                log.trace("read object from db, count= " + readedNum);
            }
        } catch (final DatabaseException e) {
            log.error("Don't warry, read message from db error, " + "late read again: "
                    + e.getMessage());
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (final DatabaseException e) {
                    // ignore
                }
            }
        }

        return map;
    }

    // FIXME 实现 忽略了 出错的情况。
    public void delete(final String key) {
        if (key == null) {
            return;
        }

        Transaction transaction = null;
        try {
            final TransactionConfig txnConfig = new TransactionConfig();
            txnConfig.setSync(true);

            transaction = bdbEnvironment.beginTransaction(null, txnConfig);
            if (log.isTraceEnabled()) {
                log.trace("BDB: begin to transaction");
            }

            bdb.delete(transaction, generateKeyEntry(key));
            if (log.isTraceEnabled()) {
                log.trace("BDB: delete message key=" + key);
            }

            transaction.commit();
            transaction = null;
            if (log.isTraceEnabled()) {
                log.trace("BDB: end of transaction");
            }
        } catch (final DatabaseException e) {
            log.fatal("BDB: delete failed: " + e.getMessage());
        } catch (final Throwable t) {
            log.fatal("BDB: delete failed: " + t.getMessage());
        } finally {
            if (transaction != null) {
                try {
                    transaction.abort();
                } catch (final DatabaseException e1) {
                }
            }
        }
    }

    public void delete(final List<String> keys) {
        // TODO 优化这个方法，以避免多次开关事务！
        for (final String key : keys) {
            delete(key);
        }
    }

    public long getSize() {
        return browserBdb();
    }
}
