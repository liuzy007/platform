package com.alibaba.napoli.spi;

import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.sender.NapoliResult;
import com.alibaba.napoli.sender.NapoliSender;

/**
 * User: heyman Date: 2/16/12 Time: 1:45 下午
 */
public interface SenderFilter {
    NapoliResult doSend(NapoliSender sender, NapoliSenderContext senderContext);
}
