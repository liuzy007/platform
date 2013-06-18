package com.alibaba.napoli.sender.impl;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.common.Extension;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.sender.NapoliResult;
import com.alibaba.napoli.sender.NapoliSender;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.spi.SenderFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 2/17/12 Time: 11:23 上午
 */
@Extension("bdbStore")
public class SenderStoreFilter implements SenderFilter {
    private static final Log log = LogFactory.getLog(SenderStoreFilter.class);

    public NapoliResult doSend(NapoliSender sender, NapoliSenderContext senderContext) {
        NapoliResult result = sender.sendMessage(senderContext);
        if (result.isSuccess() || result.isCommand()) {
            return result;
        }
        NapoliMessage napoliMessage = senderContext.getMessage();
        //关闭了storeEnable或者不能存本地或者是从本地取出来的，直接返回
        if (!napoliMessage.canStore2Local()) {
            return result;
        }
        
        ConsoleConnector connector = senderContext.getConnector();
        KVStore kvStore = connector.getSenderKVStore(senderContext.getDestinationName());

        try {
            kvStore.storeMessage(napoliMessage);
            result.setSuccess(true);
            senderContext.setStoreOk(true);
        } catch (Exception e) {
            result.addException("kvstore error!",e);
        }
        return result;
    }
}
