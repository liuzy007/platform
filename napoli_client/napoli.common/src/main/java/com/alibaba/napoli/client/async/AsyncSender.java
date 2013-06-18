/**
 * Project: napoli.client
 * 
 * File Created at 2009-6-5
 * $Id: AsyncSender.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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
 * 消息异步发送者的接口。
 * 
 * @author guolin.zhuanggl
 */
public interface AsyncSender {
	/**
	 * @since 1.0.0
	 * @param message
	 * @return
	 */
	boolean send(Serializable message);	
}
