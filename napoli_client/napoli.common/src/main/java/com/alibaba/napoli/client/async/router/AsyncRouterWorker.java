package com.alibaba.napoli.client.async.router;

import com.alibaba.napoli.client.async.NapoliWorker;

import javax.jms.Message;

public interface AsyncRouterWorker extends NapoliWorker {
    /**
     * 工作者方法，客户端业务逻辑入口,参数是原生的javax.jms.Message。<br>
     * 本方法会被来自Napoli的多个线程调用，注意方法的同步。
     * 
     * @param message 消息
     * @return 消息是否消费完。<code>true</code>，表示消费成功。<br>
     *         <code>false</code> ，表示消费不成功，该消息会再次回调该方法处理。
     */
    boolean doWork(Message message);
}
