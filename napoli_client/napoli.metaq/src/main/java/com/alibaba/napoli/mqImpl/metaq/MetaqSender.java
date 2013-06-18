package com.alibaba.napoli.mqImpl.metaq;

import com.alibaba.fastjson.JSON;
import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.BizInvokationException;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.common.util.HessianUtil;
import com.alibaba.napoli.common.util.JsonUtil;
import com.alibaba.napoli.metamorphosis.Message;
import com.alibaba.napoli.metamorphosis.client.producer.MessageProducer;
import com.alibaba.napoli.metamorphosis.client.producer.SendResult;
import com.alibaba.napoli.metamorphosis.exception.MetaClientException;
import com.alibaba.napoli.mqImpl.TransportException;
import com.alibaba.napoli.mqImpl.TransportFactory;
import com.alibaba.napoli.sender.NapoliResult;
import com.alibaba.napoli.sender.NapoliSender;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.spi.TransportSender;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman
 * Date: 4/24/12
 * Time: 4:14 下午
 */
public class MetaqSender implements NapoliSender {
    private static final Log log = LogFactory.getLog(MetaqSender.class);
    private final MessageProducer producer;

    public MetaqSender(MessageProducer producer) {
        this.producer = producer;
    }

    public NapoliResult sendMessage(NapoliSenderContext senderContext) {
        if (senderContext.getBizCall() == null) {
            NapoliMessage napoliMessage = senderContext.getMessage();
            try {
                String urgentId = (String)napoliMessage.getProperty(NapoliMessage._URGENTID);
                if (urgentId != null){
                    producer.makeUrgentMessage(urgentId);
                    NapoliResult result = new NapoliResult(true);
                    result.setCommand(true);
                    return result;
                }
                byte[] data = HessianUtil.serialize(napoliMessage);
                Message message = new Message(senderContext.getDestinationName(), data);
                message.setAttribute(JSON.toJSONString(napoliMessage.getProps()));
                final SendResult sendResult = producer.sendMessage(message);
                NapoliResult napoliResult = new NapoliResult(sendResult.isSuccess());
                if (sendResult.isSuccess()){
                   napoliResult.setMsgId(sendResult.getMessage());
                }else if (sendResult.getErrorMessage() != null){
                    napoliResult.addException(sendResult.getErrorMessage(),new NapoliClientException("metaq sender error happened!"));
                }
                return napoliResult;
            } catch (IOException e) {
                return new NapoliResult("MetaqSender IOException error!", e);
            } catch (MetaClientException e) {
                return new NapoliResult("MetaqSender MetaClientException error!", e);
            } catch (InterruptedException e) {
                return new NapoliResult("MetaqSender InterruptedException error!", e);
            }
        }else{
            NapoliMessage napoliMessage = senderContext.getMessage();
            try {
                producer.beginTransaction();
                byte[] data = HessianUtil.serialize(napoliMessage);
                final SendResult sendResult = producer.sendMessage(new Message(senderContext.getDestinationName(), data));
                if (!sendResult.isSuccess()){
                    NapoliResult napoliResult =  new NapoliResult(false);
                    if (sendResult.getErrorMessage() != null){
                        napoliResult.addException(sendResult.getErrorMessage(),new NapoliClientException("metaq sender error happened!"));
                    }
                    return napoliResult;
                }
            } catch (IOException e) {
                return new NapoliResult("MetaqSender IOException error!", new TransportException(e.getMessage(),e));
            } catch (MetaClientException e) {
                return new NapoliResult("MetaqSender MetaClientException error!", new TransportException(e.getMessage(),e));
            } catch (InterruptedException e) {
                return new NapoliResult("MetaqSender InterruptedException error!", new TransportException(e.getMessage(),e));
            }
            
            Runnable bizCall = senderContext.getBizCall();
            try {
                bizCall.run();
            } catch (Exception e) {
                try {
                    producer.rollback();
                } catch (MetaClientException e1) {
                    log.warn(e1.getMessage(),e1);
                }
                return new NapoliResult("BizCall Error Happened!"+e.getMessage(), new BizInvokationException(e.getMessage(),e));
            }

            try {
                producer.commit();
            } catch (MetaClientException e) {
                //commit error,save to localdb
                KVStore kvStore = senderContext.getConnector().getSenderKVStore(senderContext.getDestinationName());
                kvStore.storeMessage(napoliMessage);
                if (log.isInfoEnabled()){
                    log.info("commit message error!store the message to localstore! message is:"+napoliMessage);
                }
                return new NapoliResult(true);
            }
            return new NapoliResult(true);
        }
    }
}
