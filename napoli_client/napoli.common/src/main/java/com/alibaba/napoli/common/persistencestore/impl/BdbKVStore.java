package com.alibaba.napoli.common.persistencestore.impl;

//import com.alibaba.dragoon.stat.napoli.NapoliSenderStat;
//import com.alibaba.dragoon.stat.napoli.NapoliStatManager;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.inner.StoreItem;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.common.persistencestore.PersistenceException;
import com.alibaba.napoli.common.util.StringUtils;
import com.sleepycat.je.DatabaseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

/**
 * User: heyman Date: 12/1/11 Time: 11:55 上午
 */
public class BdbKVStore implements KVStore {
    private static final Log log = LogFactory.getLog(BdbKVStore.class);
    private String storePath;
    private String name;
    private String clientType;
    private File storeDirectory;
    private volatile BdbPersistenceStore<StoreItem> persistenceStore;
       
    public BdbKVStore(String storePath, String name, String clientType) {
        if (StringUtils.isEmpty(storePath) || StringUtils.isEmpty(name)
                || StringUtils.isEmpty(clientType)) {
            throw new IllegalArgumentException(
                    "storePath is empty or name or clientType is empty,BdbKVStore create error!");
        }
        this.clientType = clientType;
        this.storePath = storePath;
        this.name = name;
        this.storeDirectory = getFile(name);
        this.persistenceStore = createBdbStore();
    }

    public void storeMessage(NapoliMessage napoliMessage) {
        StoreItem item = new StoreItem(napoliMessage.getQueueName(), napoliMessage);
        this.persistenceStore.write(item);
    }

    public void update(String key, NapoliMessage napoliMessage) {
        StoreItem item = new StoreItem(napoliMessage.getQueueName(), napoliMessage);
        this.persistenceStore.update(key,item);
    }

    public Map<String, StoreItem> batchRead(int batchReadCount) {
        return persistenceStore.batchRead(batchReadCount);
    }

    public String getName() {
        return name;
    }

    public void delete(String key) {
        persistenceStore.delete(key);
    }

    public void clear() {
        while (true) {
            Map<String, StoreItem> items = batchRead(1000);
            if (items.size() == 0) {
                break;
            }

            for (final Iterator<Map.Entry<String, StoreItem>> iterator = items
                    .entrySet().iterator(); iterator.hasNext(); ) {

                final Map.Entry<String, StoreItem> entry = iterator.next();
                final String key = entry.getKey();
                delete(key);

                iterator.remove();
            }
        }
    }

    private File getFile(String name) {
        // 校验存储目录
        String keyPath = NapoliConstant.CLIENT_DOMAIN_ASYNC + "-" + clientType
                + "-" + name;
        final File sp = new File(storePath, keyPath);
        if (!sp.exists()) {
            if (!sp.mkdirs()) {
                throw new PersistenceException("mkdir (" + storePath + "," + keyPath
                        + " failed!");
            }
        }
        if (sp.isFile()) {
            throw new PersistenceException("a file exist,(" + storePath + "/"
                    + keyPath + "must be a dir!");
        }
        return sp;
    }

    /**
     * 在给定的目录下创建一个新bdb目录，避免和已有冲突
     *
     * @return
     */
    private BdbPersistenceStore<StoreItem> createBdbStore() {
        if (storeDirectory == null) {
            throw new PersistenceException("can't access " + name + " path:"
                    + storeDirectory);
        }
        BdbPersistenceStore<StoreItem> persistenceStore = null;
        try {
            if (storeDirectory.isDirectory()) {
                persistenceStore = BdbPersistenceStore
                        .createBdbPersistenceStore(StoreItem.class);
                persistenceStore.setBdbStorePath(storeDirectory
                        .getAbsolutePath());
                persistenceStore.init();
            }
        } catch (final DatabaseException e) {
            try {
                persistenceStore.close();
            } catch (DatabaseException e2) {
                throw new PersistenceException(
                        "Fail to close persistence storestore at Path("
                                + storeDirectory.getAbsolutePath() + ")", e2);
            }
            throw new PersistenceException(
                    "Fail to create persistence storestore at Path("
                            + storeDirectory.getAbsolutePath() + ")", e);
        }
        return persistenceStore;
    }

    public long getStoreSize() {
        if (this.persistenceStore == null) {
            return 0;
        } else {
            return this.persistenceStore.getSize();
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.alibaba.napoli.common.persistencestore.KVStore#close()
      */
    public void close() {
        if (this.persistenceStore != null) {
            try {
                this.persistenceStore.close();
            } catch (DatabaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                log.error(e.getMessage(),e);
            }
        }
    }
}
