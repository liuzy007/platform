/**
 * Project: napoli.client-1.4.0
 * 
 * File Created at 2010-8-17
 * $Id: NapoliTextFilter.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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

import java.util.HashSet;
import java.util.Set;

/**
 * TODO Comment of NapoliTextFilter
 * @author dacy
 *
 */
public class NapoliTextFilter implements DestinationFilter{

    /* (non-Javadoc)a00001 aa aaa11
     * "a" 发送到a00001
     * "b" 发送到 a00001 aa
     * "c" 发送到  a00001 aa aaa11
     * @see com.alibaba.napoli.client.async.DestinationFilter#getFilterededQueueName(java.util.Map, java.io.Serializable)
     */
    public Set<String> getFilterededQueueName(String topicName, Set<String> queueNameSet,NapoliMessage message) {
        
        if(queueNameSet.isEmpty()) return null;
        String text = (String)message.getContent();
        HashSet<String> result = new HashSet<String>();         
        
        if(text.equalsIgnoreCase("a")){
            if(queueNameSet.contains("a00001")){
                result.add("a00001");
            }
        }
        else if(text.equalsIgnoreCase("b")){
            if(queueNameSet.contains("a00001")){
                result.add("a00001");
            }
            if(queueNameSet.contains("aa")){
                result.add("aa");
            }
           
         }
        else if(text.equalsIgnoreCase("c")){
           result.addAll(queueNameSet);             
         }
        return result;
    }
}