package com.alibaba.napoli.sender.impl;

import com.alibaba.napoli.client.async.BizInvokationException;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.mqImpl.TransportException;
import com.alibaba.napoli.mqImpl.TransportFactory;
import com.alibaba.napoli.sender.NapoliResult;
import com.alibaba.napoli.sender.NapoliSender;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.spi.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 2/16/12 Time: 2:42 下午
 */
public class SenderMq implements NapoliSender {
    private static final Log log = LogFactory.getLog(SenderMq.class);

    public NapoliResult sendMessage(NapoliSenderContext senderContext) {
        TransportFactory transportFactory = new TransportFactory();
        try {
            ClientMachine machine = senderContext.getMachine();
            if (machine == null) {
                return new NapoliResult("no sendable machine!", new TransportException(
                        "There is no sendable machine for queue=" + senderContext.getDestinationName()
                                + " the message=" + senderContext.getMessage()));
            }
            TransportSender transportSender = transportFactory.getTrasportSender(senderContext);
            if (senderContext.getBizCall() == null) {
                transportSender.send(senderContext.getDestinationName(), senderContext.getMessage());
            } else {
                transportSender.send(senderContext.getDestinationName(), senderContext.getMessage(),
                        senderContext.getBizCall());
            }
        } catch (TransportException e) {
            return new NapoliResult("SenderMq transport error!", e);
        } catch (BizInvokationException e) {
            return new NapoliResult("SenderMq bizInvoke error!", e);
        } catch (Throwable e) {
            return new NapoliResult("SenderMq runtime error!", e);
        }
        return new NapoliResult(true);
    }
}
