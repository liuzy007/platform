/**
 * Project: napoli.client
 * 
 * File Created at 2009-6-5
 * $Id: NapoliClientException.java 177625 2012-06-07 05:40:33Z haihua.chenhh $
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
package com.alibaba.napoli.client;


/**
 * 指示Napoli客户端的操作出了异常。
 * 
 * @author guolin.zhuanggl
 */
public class NapoliClientException extends Exception {
    private static final long serialVersionUID = 6747939651716293935L;

    public NapoliClientException(String msg) {
        super(msg);
    }

    public NapoliClientException(String msg, Throwable t) {
        super(msg, t);
    }
}
