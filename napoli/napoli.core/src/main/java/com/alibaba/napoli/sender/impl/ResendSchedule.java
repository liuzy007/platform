package com.alibaba.napoli.sender.impl;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.client.async.inner.StoreItem;
import com.alibaba.napoli.sender.Sender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * User: heyman
 * Date: 11/30/11
 * Time: 5:09 下午
 */
public class ResendSchedule implements Runnable {
    private static final Log log = LogFactory.getLog(ResendSchedule.class);
    private KVStore kvStore;
    private int dataBatchReadCount = 1000;
    private static final int secondDataCountBase = 200;
    private Sender sender;

    /**
     * 发送失败，要再发的消息。
     */
    private final Map<String, StoreItem> secondHandData = new HashMap<String, StoreItem>(); // latest corruption items
    private int reloadCount = 0;


    public ResendSchedule(KVStore kvStore, int dataBatchReadCount, Sender sender) {
        this.kvStore = kvStore;
        this.dataBatchReadCount = dataBatchReadCount;
        this.sender = sender;
    }

    public void run() {
        if (kvStore == null) {
            return;
        }

        try {
            final boolean isHealthy = sendSecondHandData();

            if (!isHealthy) {
                return;
            }

            //bug if sendSecondHandData failed to send out (but still reaturn healthy), it will do another send in sendData().
            //that means it always send 1 + 2*(n-1) instead of 1+(n-1)
            //当队列是not sendable的时候，重试次数非常高
            // phase 2, scan db
            final Map<String, StoreItem> data = kvStore.batchRead(dataBatchReadCount);
            final int readDataCount = data.size();
            if (readDataCount == 0) {
                return;
            }
            sendData(data);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
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
        for (final Iterator<Map.Entry<String, StoreItem>> iterator = dataMap.entrySet().iterator(); iterator
                .hasNext(); ) {


            final Map.Entry<String, StoreItem> entry = iterator.next();
            final String key = entry.getKey();

            //if the item has already be in the secondHandData, then it has been tried resend by sendSecondHandData(), should not do send again.
            //fix of bug ...
            if (secondHandData.containsKey(key)) {
                continue;
            }

            final StoreItem storeItem = entry.getValue();
            final Serializable message = storeItem.getContent();

            if (reprocessWithoutException(message)) {
                if (log.isDebugEnabled()) {
                    log.debug("success to resend msg: " + message);
                }
                kvStore.delete(key);
                secondHandData.remove(key); // 以防万一，这条数据在之前发送的数据中也有
                iterator.remove();
                success++;
            } else {
                failed++;

                if (!secondHandData.containsKey(key)) {
                    secondHandData.put(key, storeItem);
                }
                if (isSecondHandDataAboveCordon()) {
                    // 不再发送
                    break;
                }
            }
        } // end of for loop

        if (log.isWarnEnabled() && failed > 0) {
            log.warn("sendData, Store [" + kvStore.getName() + "] Read " + total
                    + " messages from database and process " + success + " messages " + "failed "
                    + failed);
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
        for (final Iterator<Map.Entry<String, StoreItem>> iterator = secondHandData.entrySet()
                .iterator(); iterator.hasNext(); ) {

            final Map.Entry<String, StoreItem> entry = iterator.next();
            final String key = entry.getKey();
            final StoreItem storeItem = entry.getValue();
            final Serializable message = storeItem.getContent();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                log.error(e.getMessage(),e);
            }

            if (reprocessWithoutException(message)) {
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
                    + " messages from database and process " + success + " messages " + "failed "
                    + failed);
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
    private boolean reprocessWithoutException(final Serializable message) {
        try {
            NapoliMessage napoliMessage;
            if (message instanceof NapoliMessage) {
                napoliMessage = (NapoliMessage) message;
            } else {
                napoliMessage = new NapoliMessage(message);
                napoliMessage.setProperty(NapoliMessage.MSG_PROP_KEY_QUEUENAME, kvStore.getName());
            }
            napoliMessage.setLocalStoreMessage(true);
            return sender.sendMessage(napoliMessage);
        } catch (final Exception e) {
            log.error(e.getMessage(),e);
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
}
