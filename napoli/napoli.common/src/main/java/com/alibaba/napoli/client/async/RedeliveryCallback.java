/**
 * Project: napoli.client
 * 
 * File Created at 2009-6-5
 * $Id: RedeliveryCallback.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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
 * 重发失败后应用调用接口，用于报警，失败处理等。
 * 
 * @author guolin.zhuanggl
 * @see RedeliveryCallback
 */
public interface RedeliveryCallback {
	/**
	 * 当redelivery失败的时候调用
	 * @param msg 消息体
	 * @param t 处理过程中发现的异常
	 */
	public void redeliveryFailed(Serializable msg,Throwable t);
}
