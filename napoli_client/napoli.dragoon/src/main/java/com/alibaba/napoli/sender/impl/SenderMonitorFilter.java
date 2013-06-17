package com.alibaba.napoli.sender.impl;

import com.alibaba.dragoon.stat.napoli.NapoliSenderStat;
import com.alibaba.dragoon.stat.napoli.NapoliStatManager;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.common.Extension;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.sender.NapoliResult;
import com.alibaba.napoli.sender.NapoliSender;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.spi.SenderFilter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: heyman Date: 2/16/12 Time: 4:56 下午
 */
@Extension("monitor")
public class SenderMonitorFilter implements SenderFilter {
    private static NapoliStatManager monitor = NapoliStatManager.getInstance();
    private static AtomicLong bdbSize;

    //for mock test
    public static void setMonitor(NapoliStatManager monitorMock) {
        monitor = monitorMock;
    }

    public NapoliResult doSend(NapoliSender sender, NapoliSenderContext senderContext) {
        NapoliMessage message = senderContext.getMessage();

        message.setProperty(NapoliMessage.MSG_PROP_KEY_SRCIP, NapoliStatManager.getNativeIp());
        message.setProperty(NapoliMessage.MSG_PROP_KEY_SRC_HOSTNAME, NapoliStatManager.getHostName());
        if (NapoliStatManager.getAppNumber() == null) {
            message.setProperty(NapoliMessage.MSG_PROP_KEY_APPNUM, "N/A");
        } else {
            message.setProperty(NapoliMessage.MSG_PROP_KEY_APPNUM, NapoliStatManager.getAppNumber());
        }
        message.setProperty(NapoliMessage.MSG_PROP_KEY_BEGINTIME, System.currentTimeMillis());
        if (senderContext.getMachine() != null) {
            message.setProperty(NapoliMessage.MSG_PROP_KEY_TARGETADDRESS, senderContext.getMachine().getIp() + ":" + senderContext.getMachine().getPort());
        }
        message.setProperty(NapoliMessage.MSG_PROP_KEY_MSGSIZE, 0);
        message.setProperty(NapoliMessage.MSG_PROP_KEY_QUEUENAME, senderContext.getDestinationName());


        NapoliResult result = sender.sendMessage(senderContext);
        if (result.isCommand()) {
            return result;
        }
        NapoliSenderStat napoliSenderStat = monitor.getSenderStat(message.getTargetAddress(), message.getQueueName());
        //init bdbSize
        if (bdbSize == null && senderContext.getConnectionParam().isStoreEnable()) {
            KVStore kvStore = senderContext.getConnector().getSenderKVStore(senderContext.getDestinationName());
            long size = kvStore.getStoreSize();
            bdbSize = new AtomicLong(size);
            napoliSenderStat.setLocalStoreCount(bdbSize.get());
        } else {
            bdbSize = new AtomicLong(0);
        }

        if (result.isSuccess()) {
            if (message.isLocalStoreMessage()) {//本地重发成功
                napoliSenderStat.resendSuccess(message.getSize(), System.currentTimeMillis() - message.getBeginTime());
                napoliSenderStat.setLocalStoreCount(bdbSize.decrementAndGet());
            } else if (senderContext.isStoreOk()) { //发送失败，存本地成功
                napoliSenderStat.sendFailure(message.getSize(), System.currentTimeMillis() - message.getBeginTime());
                napoliSenderStat.setLocalStoreCount(bdbSize.incrementAndGet());
            } else { //一次性发送成功
                napoliSenderStat.sendSuccess(message.getSize(), System.currentTimeMillis() - message.getBeginTime());
            }
        } else {
            if (message.isLocalStoreMessage()) {
                napoliSenderStat.resendFailure(message.getSize(), System.currentTimeMillis() - message.getBeginTime());
            } else {
                napoliSenderStat.sendFalse(message.getSize(), System.currentTimeMillis() - message.getBeginTime());
            }
        }
        return result;
    }
}
