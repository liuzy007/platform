package com.alibaba.napoli.receiver;

import com.alibaba.napoli.sender.NapoliResult;

/**
 * User: heyman
 * Date: 2/20/12
 * Time: 4:06 下午
 */
public interface NapoliReceiver {
    NapoliResult onMessage(NapoliReceiverContext receiverContext);
}
