package com.alibaba.napoli.receiver.impl;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.client.async.ext.AsyncWorkerEx;
import com.alibaba.napoli.common.util.NapoliMessageUtil;
import com.alibaba.napoli.receiver.NapoliReceiver;
import com.alibaba.napoli.receiver.NapoliReceiverContext;
import com.alibaba.napoli.receiver.ReceiverException;
import com.alibaba.napoli.receiver.filter.Filter;
import com.alibaba.napoli.sender.NapoliResult;

import java.io.Serializable;
import java.util.List;

/**
 * User: heyman Date: 2/20/12 Time: 4:16 下午
 */
public class ReceiverMq implements NapoliReceiver {

    public NapoliResult onMessage(NapoliReceiverContext receiverContext) {
        NapoliWorker napoliWorker = receiverContext.getWorker();
        NapoliMessage napoliMessage = receiverContext.getNapoliMessage();
        if (napoliWorker == null || napoliMessage == null) {
            return new NapoliResult("worker or napoliMessage is null!",new ReceiverException("worker or napoliMessage is null"));
        }
        try {
            if (napoliWorker instanceof AsyncWorker) {
                AsyncWorker worker = (AsyncWorker) napoliWorker;
                Object content = napoliMessage.getContent();
                if (content instanceof Serializable) {
                    Serializable message = (Serializable) content;
                    List<Filter> filterList = receiverContext.getFilterList();
                    
                    final Serializable filterMsg = NapoliMessageUtil.filterMsg(filterList,message);
                    // 过滤器返回null，即 消息被过滤掉了
                    if (filterMsg == null) {
                        return new NapoliResult(true); // 直接返回
                    }                   
                    
                    return new NapoliResult(worker.doWork(message));
                } else {
                    return new NapoliResult("content isn't Serializable",new ReceiverException("the message content isn't Serializable,class="
                            + content.getClass().getName() + " can't match the AsyncWorker"));
                }
            } else if (napoliWorker instanceof AsyncWorkerEx) {
                AsyncWorkerEx worker = (AsyncWorkerEx) napoliWorker;
                return new NapoliResult(worker.doWork(napoliMessage));
            } else {
                return new NapoliResult("can't support the worker type!",new ReceiverException("can't support the worker type="
                        + napoliWorker.getClass().getName()));
            }
        } catch (Throwable e) {
            return new NapoliResult("ReceiverMq runtime error!",e);
        }
    }
}
