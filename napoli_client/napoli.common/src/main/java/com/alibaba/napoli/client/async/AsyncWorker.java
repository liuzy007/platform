/**
 * Project: napoli.client
 * 
 * File Created at 2009-6-5
 * $Id: AsyncWorker.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
 * 
 * Copyright 2008 Alibaba.com Croporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.napoli.client.async;

import java.io.Serializable;

/**
 * 当收到消息后的回调接口。
 * 
 * @author guolin.zhuanggl
 * @see AsyncReceiver
 */
public interface AsyncWorker extends NapoliWorker{
    /**
     * 工作者方法，客户端业务逻辑入口。<br>
     * 本方法会被来自Napoli的多个线程调用，注意方法的同步。
     * 
     * @param message 消息体
     * @return 消息是否消费完。<code>true</code>，表示消费成功。<br>
     *         <code>false</code> ，表示消费不成功，该消息会再次回调该方法处理。
     */
    boolean doWork(Serializable message);
}
