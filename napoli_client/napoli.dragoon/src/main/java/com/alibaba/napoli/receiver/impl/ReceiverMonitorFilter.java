package com.alibaba.napoli.receiver.impl;

import com.alibaba.dragoon.stat.Profiler;
import com.alibaba.dragoon.stat.napoli.NapoliReceiverStat;
import com.alibaba.dragoon.stat.napoli.NapoliStatManager;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.Extension;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.receiver.NapoliReceiver;
import com.alibaba.napoli.receiver.NapoliReceiverContext;
import com.alibaba.napoli.sender.NapoliResult;
import com.alibaba.napoli.spi.ReceiverFilter;
import java.util.concurrent.atomic.AtomicLong;


/**
 * User: heyman Date: 2/21/12 Time: 2:26 下午
 */
@Extension("monitor")
public class ReceiverMonitorFilter implements ReceiverFilter {

    private static NapoliStatManager monitor = NapoliStatManager.getInstance();
    private static AtomicLong bdbSize;

    // for mock test
    public static void setMonitor(NapoliStatManager monitorMock) {
        monitor = monitorMock;
    }

    public NapoliResult doMessage(NapoliReceiver receiver, NapoliReceiverContext receiverContext) {
        long beginTime = System.currentTimeMillis();
        NapoliMessage napoliMessage = receiverContext.getNapoliMessage();
        String srcIp = napoliMessage.getSrcIp();
        String srcHostname = napoliMessage.getSrcHostname();
        String appNum = napoliMessage.getAppnum();
        if (appNum == null || appNum.equals("")) {
            appNum = "N/A";
        }
        String address = napoliMessage.getTargetAddress();
        String queueName = napoliMessage.getQueueName();
        long srcBeginTime = napoliMessage.getBeginTime();

        NapoliReceiverStat receiverStat = monitor.getReceiverStat(srcIp, srcHostname, appNum, address, queueName);
        // init bdbSize
        if (bdbSize == null && receiverContext.getConnectionParam().isStoreEnable()) {
            ConnectionParam connectionParam = receiverContext.getConnectionParam();
            KVStore kvStore = ConsoleConnector.getorCreateBdbKvStore(connectionParam.getStorePath(),
                    connectionParam.getName(), NapoliConstant.CLIENT_TYPE_RECEIVER);
            // KVStore kvStore =
            // senderContext.getConnector().getSenderKVStore(senderContext.getDestinationName());
            long size = kvStore.getStoreSize();
            bdbSize = new AtomicLong(size);
            if (receiverStat != null) {
                receiverStat.setLocalStoreCount(bdbSize.get());
            }
        } else {
            bdbSize = new AtomicLong(0);
        }

        Profiler.enter(queueName, Profiler.EntryType.NAPOLI_SERVICE);
        NapoliResult result = receiver.onMessage(receiverContext);

        if (receiverStat != null) {
            receiverStat.recordProfilerInfo(Profiler.release());
            long now = System.currentTimeMillis();
            if (result.isSuccess()) {
                if (napoliMessage.isLocalStoreMessage()) {
                    receiverStat.rereceiveSuccess(now - beginTime, now - srcBeginTime);
                    receiverStat.setLocalStoreCount(bdbSize.decrementAndGet());
                } else if (receiverContext.isStoreOk()) {
                    receiverStat.receiveFailure(now - beginTime);
                    receiverStat.setLocalStoreCount(bdbSize.incrementAndGet());
                } else {
                    receiverStat.receiveSuccess(now - beginTime, now - srcBeginTime);
                }

                // bug NP-263
                // without previous "else if", the locaStoreMessage consume
                // success will be counted both rereceiveSuccess and
                // receiveSuccess.

            } else {
                if (napoliMessage.isLocalStoreMessage()) {
                    receiverStat.rereceiveFailure(now - beginTime);
                } else {
                    receiverStat.receiveFalse(now - beginTime);
                }
            }
        }
        return result;
    }
}
