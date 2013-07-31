package com.alibaba.napoli.sender;

/**
 * User: heyman
 * Date: 2/16/12
 * Time: 2:13 下午
 */
public interface NapoliSender {
    NapoliResult sendMessage(NapoliSenderContext senderContext);
}
