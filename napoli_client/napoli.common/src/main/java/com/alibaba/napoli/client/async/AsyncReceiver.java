/**
 * Project: napoli.client
 * 
 * File Created at 2009-6-5
 * $Id: AsyncReceiver.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.ext.AsyncWorkerEx;
import com.alibaba.napoli.client.async.router.AsyncRouterWorker;

/**
 * 消息异步接收者的接口。
 * 
 * @author guolin.zhuanggl
 * @see AsyncWorker
 */
public interface AsyncReceiver {
    /**
     * 设置工作者接口，实现用户义务逻辑回调，用户需要在调用start之前显式调用。
     * 
     * @param worker 工作者，应用提供
     * @throws NapoliClientException 发生错误时封装返回
     * @see AsyncWorker
     */
    void setWorker(AsyncWorker worker) throws NapoliClientException;
    /**
     * 设置工作者接口，实现用户义务逻辑回调，用户需要在调用start之前显式调用。
     * 
     * @param worker 工作者，应用提供
     * @throws NapoliClientException 发生错误时封装返回
     * @see AsyncWorker
     */
    void setExWorker(AsyncWorkerEx worker) throws NapoliClientException;
    /**
     * 设置工作者接口，实现用户义务逻辑回调，用户需要在调用start之前显式调用。
     * 
     * @param worker 工作者，应用提供
     * @throws NapoliClientException 发生错误时封装返回
     * @see AsyncWorker
     */
    void setRouterWorker(AsyncRouterWorker worker) throws NapoliClientException;
    /**
     * 设置redivery回调接口，实现处理失败逻辑的策略回调。
     * 
     * @param worker 工作者，应用提供
     * @throws NapoliClientException 发生错误时封装返回
     * @see AsyncWorker
     */
    void setRedeliveryCallback(final RedeliveryCallback redeliveryCallback)throws NapoliClientException;

    /**
     * 启动消费者，用户需要显式调用。
     * 
     * @throws NapoliClientException 发生异常时抛出
     */
    void start() throws NapoliClientException;

    /**
     * 停止一个消费者，停止后可以调用start重新开始
     * 
     * @throws NapoliClientException
     */
    void stop() throws NapoliClientException;

}
