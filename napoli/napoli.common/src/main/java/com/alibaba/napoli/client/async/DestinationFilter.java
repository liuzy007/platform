/**
 * Project: napoli.client-1.4.0
 * 
 * File Created at 2010-7-29
 * $Id: DestinationFilter.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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

import java.util.Set;
/**
 * TODO 消息选择路由到topic对应的哪些queue上
 * @author dacy
 *
 */
public interface DestinationFilter {
    
/**
 * 过滤topic中的queue
 * @param Topic的name，对应的queue的nam集合，消息
 * @return 返回目标queue的name集合
 */
public Set<String>  getFilterededQueueName(String topicName,Set<String> queueNameSet,NapoliMessage message);
    
}
