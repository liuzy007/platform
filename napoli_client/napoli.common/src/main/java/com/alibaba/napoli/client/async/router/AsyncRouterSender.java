package com.alibaba.napoli.client.async.router;

import javax.jms.Message;

public interface AsyncRouterSender {
    boolean send(Message message);
}
