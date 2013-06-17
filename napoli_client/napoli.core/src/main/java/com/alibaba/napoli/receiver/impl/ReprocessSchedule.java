package com.alibaba.napoli.receiver.impl;

//import com.alibaba.dragoon.stat.napoli.NapoliReceiverStat;
//import com.alibaba.dragoon.stat.napoli.NapoliStatManager;

import com.alibaba.napoli.common.persistencestore.StoreDiscard;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.client.async.inner.StoreItem;
import com.alibaba.napoli.common.util.ExtensionLoader;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.receiver.NapoliReceiver;
import com.alibaba.napoli.receiver.NapoliReceiverContext;
import com.alibaba.napoli.sender.NapoliResult;

public class ReprocessSchedule implements Runnable {

    protected static final Log           log                 = LogFactory.getLog(ReprocessSchedule.class);

    private KVStore                      kvStore;
    private int                          dataBatchReadCount  = 1000;
    private static int                   secondDataCountBase = 200;

    private NapoliWorker                 worker;

    /**
     * 发送失败，要再发的消息。
     */
    private final Map<String, StoreItem> secondHandData      = new HashMap<String, StoreItem>();          // latest corruption items
    private int                          reloadCount         = 0;
    private NapoliReceiver               napoliReceiver;
    private ConnectionParam              connectionParam;
    private StoreDiscard storeDiscard;

    public static void setSecondDataCountBase(int base) {
        secondDataCountBase = base;
    }

    public ReprocessSchedule(ConnectionParam connectionParam, int dataBatchReadCount, NapoliWorker worker) {
        this.connectionParam = connectionParam;
        this.kvStore = ConsoleConnector.getorCreateBdbKvStore(connectionParam.getStorePath(),
                connectionParam.getName(), NapoliConstant.CLIENT_TYPE_RECEIVER);
        this.dataBatchReadCount = dataBatchReadCount;
        this.worker = worker;
        napoliReceiver = ExtensionLoader.buildReceiverFilterChain(new String[] { "monitor" });
    }

    public void run() {
        if (kvStore == null) {
            return;
        }

        // phase 1, scan corruption
        if (log.isDebugEnabled()) {
            log.debug("reprocessTask start to handle persitence message.");
        }

        if (log.isDebugEnabled()) {
            log.debug("about to scan corruption! corruption size: " + secondHandData.size());
        }

        final boolean isHealthy = sendSecondHandData();

        if (!isHealthy) {
            return;
        }

        //bug if sendSecondHandData failed to send out (but still reaturn healthy), it will do another send in sendData().
        //that means it always send 1 + 2*(n-1) instead of 1+(n-1)
        //当队列是not sendable的时候，重试次数非常高
        // phase 2, scan db
        if (log.isDebugEnabled()) {
            log.debug("about to scan db!");
        }

        final Map<String, StoreItem> data = kvStore.batchRead(dataBatchReadCount);
        final int readDataCount = data.size();
        if (readDataCount == 0) {
            return;
        }

        final boolean isHealthy2 = sendData(data);
        if (!isHealthy2) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("reprocessTask finished handle persitence message.");
        }
    }

    /**
     * 发送从kvstore中读到的数据。发送失败的数据放到secondHandData中。
     * 
     * @return 发送的状况是否是健康的，可以做为”否有必要继续执行发送操作“的依据。
     */
    private boolean sendData(final Map<String, StoreItem> dataMap) {

        final int total = dataMap.size();

        int failed = 0;
        int success = 0;
        for (final Iterator<Map.Entry<String, StoreItem>> iterator = dataMap.entrySet().iterator(); iterator.hasNext();) {

            final Map.Entry<String, StoreItem> entry = iterator.next();
            final String key = entry.getKey();

            //if the item has already be in the secondHandData, then it has been tried resend by sendSecondHandData(), should not do send again.
            //fix of bug ...
            if (secondHandData.containsKey(key)) {
                continue;
            }

            final StoreItem storeItem = entry.getValue();
            final Serializable message = storeItem.getContent();

            if (reprocessWithoutException(key,message)) {
                if (log.isDebugEnabled()) {
                    log.debug("success to resend msg: " + message);
                }
                kvStore.delete(key);
                secondHandData.remove(key); // 以防万一，这条数据在之前发送的数据中也有
                iterator.remove();
                success++;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("failed to resend msg: " + message);
                }
                failed++;

                if (!secondHandData.containsKey(key)) {
                    secondHandData.put(key, storeItem);
                }
                if (isSecondHandDataAboveCordon()) {
                    if (log.isInfoEnabled()) {
                        log.info("sendData, bad circumstance because corruption size: " + secondHandData.size());
                    }
                    // 不再发送
                    break;
                }
            }
        } // end of for loop

        if (log.isWarnEnabled() && failed > 0) {
            log.warn("sendData, Store [" + kvStore.getName() + "] Read " + total
                    + " messages from database and process " + success + " messages " + "failed " + failed);
        }

        if (log.isDebugEnabled()) {
            log.debug("sent msg from db: " + success);
        }

        // 分析发送的健康情况
        if (success == 0) {
            //很小数量的累积会导致死循环，这里break -- zgl
            return false;
        }

        return !isSecondHandDataAboveCordon();

    }

    /**
     * 再次发送未成功发送的数据，即{@link #secondHandData}的中的数据。
     * 
     * @return 发送的状况是否是健康的，可以做为”否有必要继续执行发送操作“的依据。
     */
    private boolean sendSecondHandData() {
        final int total = secondHandData.size();

        int failed = 0;
        int success = 0;
        for (final Iterator<Map.Entry<String, StoreItem>> iterator = secondHandData.entrySet().iterator(); iterator
                .hasNext();) {

            final Map.Entry<String, StoreItem> entry = iterator.next();
            final String key = entry.getKey();
            final StoreItem storeItem = entry.getValue();
            final Serializable message = storeItem.getContent();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                log.error(e.getMessage(), e);
            }

            if (reprocessWithoutException(key,message)) {
                // 发送成功
                kvStore.delete(key);
                iterator.remove();

                success++;
            } else {
                failed++;
            }
        } // end of for loop

        if (log.isWarnEnabled() && failed > 0) {
            log.warn("sendSecondHandData, Store [" + kvStore.getName() + "] Read " + total
                    + " messages from database and process " + success + " messages " + "failed " + failed);
        }

        // FIXME 可能有这样的情况会,消息总是发不出去！
        // 1. 消息发送失败是因为数据不对，这样 应用总是报告该消息发送的失败
        // 2. 每次读数据会是读库中前面的数据
        // 3. 读出的这批数据，失败率高于设定值
        // 上面的条件满足后，这批数据问题消费不掉，后面的数据就会读不出来！
        //
        // 上面的情况，属于异常情况的异常情况，暂时不考虑。
        // 解决方法，可以调整Persistance Store的实现，不是总是从Store的前面读数据。

        // 分析发送的健康情况

        if (isFailureTooMany(success, total) && isSecondHandDataTooMany()) { // so bad and just keep silence

            if (log.isInfoEnabled()) {
                log.info("sendSecondHandData, bad circumstance, only send corruption: " + success);
            }

            if (++reloadCount >= 3) { // reload corruption if too much times lower sent proportion
                secondHandData.clear();
                reloadCount = 0;
            }

            return false;
        }

        return true;
    }

    /**
     * 包一下reprocess方法，保证reprocess不会抛出异常，打乱外部的处理。<br>
     * reprocess抛出异常，则认为reprocess是失败的。
     */
    private boolean reprocessWithoutException(final String key,final Serializable message) {
        try {
            if (message instanceof NapoliMessage) {
                NapoliMessage storeContent = (NapoliMessage) message;
                storeContent.setLocalStoreMessage(true);
                NapoliReceiverContext context = new NapoliReceiverContext(worker, storeContent, connectionParam);
                NapoliResult result = napoliReceiver.onMessage(context);
                if (!result.isSuccess() && connectionParam.getReprocessNum() > 0) {
                    int reprocessCount = storeContent.getReprocessNum() + 1;
                    if (reprocessCount >= connectionParam.getReprocessNum()) {
                        if (log.isInfoEnabled()) {
                            log.info("make the consumer success!because the reprocessNum[" + reprocessCount + ">="
                                    + connectionParam.getReprocessNum() + "] size="+kvStore.getStoreSize());
                        }
                        if (storeDiscard != null){
                            storeDiscard.storeDiscard(storeContent);
                        }
                        result.setSuccess(true);
                    } else {
                        storeContent.setReprocessNum(reprocessCount);
                    }
                    kvStore.update(key,storeContent);
                }
                return result.isSuccess();
            } else {
                log.error("can't support not NapoliMessage type.the storeItem type=[" + message.getClass().getName()
                        + "]");
                return false;
            }
        } catch (final Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Exception when reprocess message.", e);
            }
            return false;
        }
    }

    /**
     * 二手消息总量(即重发消息失败数)高于警戒线。<br>
     * 如果重发的消息数超过这个数值，认为消息发送情况不好。可以做一些处理：<br>
     * 比如，清空reprocessFailData、等等
     */
    private boolean isSecondHandDataAboveCordon() {
        return secondHandData.size() >= dataBatchReadCount / 10;
    }

    /**
     * 消息处理的失败率 是不是太高了。
     */
    private static boolean isFailureTooMany(final int success, final int totol) {
        // 成功发送小于20%
        return success < totol / 5;
    }

    /**
     * 二手消息总量大于设定的值
     */
    private boolean isSecondHandDataTooMany() {
        return secondHandData.size() >= secondDataCountBase;
    }

    public void setStoreDiscard(StoreDiscard storeDiscard) {
        this.storeDiscard = storeDiscard;
    }
}
