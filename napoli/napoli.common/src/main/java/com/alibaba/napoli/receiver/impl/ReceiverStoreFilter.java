package com.alibaba.napoli.receiver.impl;

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

/**
 * User: heyman
 * Date: 2/21/12
 * Time: 3:38 下午
 */
@Extension("bdbStore")
public class ReceiverStoreFilter implements ReceiverFilter {
    public NapoliResult doMessage(NapoliReceiver receiver, NapoliReceiverContext receiverContext) {
        NapoliResult result = receiver.onMessage(receiverContext);
        ConnectionParam connectionParam = receiverContext.getConnectionParam();
        if (!result.isSuccess() && receiverContext.getNapoliMessage().canStore2Local()){
            //NapoliMessage napoliMessage = receiverContext.getNapoliMessage();
            KVStore kvStore = ConsoleConnector.getorCreateBdbKvStore(connectionParam.getStorePath(), connectionParam.getName(), NapoliConstant.CLIENT_TYPE_RECEIVER);
            try {
                kvStore.storeMessage(receiverContext.getNapoliMessage());
                result.setSuccess(true);
                receiverContext.setStoreOk(true);
            }catch (Exception e) {
                result.addException("kvstore error!",e);
            }
        }
        return result;
    }
}
