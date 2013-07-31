package com.alibaba.napoli.spi;

import com.alibaba.napoli.receiver.NapoliReceiver;
import com.alibaba.napoli.receiver.NapoliReceiverContext;
import com.alibaba.napoli.sender.NapoliResult;
import com.alibaba.napoli.sender.NapoliSender;
import com.alibaba.napoli.sender.NapoliSenderContext;

/**
 * User: heyman
 * Date: 2/20/12
 * Time: 4:14 下午
 */
public interface ReceiverFilter {
    NapoliResult doMessage(NapoliReceiver receiver, NapoliReceiverContext receiverContext);
}
