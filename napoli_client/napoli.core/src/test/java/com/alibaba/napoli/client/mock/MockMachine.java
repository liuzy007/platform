/**
 * Project: napoli.client
 * 
 * File Created at 2011-9-19
 * $Id: MockMachine.java 177625 2012-06-07 05:40:33Z haihua.chenhh $
 * 
 * Copyright 1999-2100 Alibaba.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.napoli.client.mock;

import com.alibaba.napoli.domain.client.ClientMachine;

public class MockMachine extends ClientMachine {

    private static final long serialVersionUID = 1L;
    boolean                   available;

    public MockMachine(){
        this(true);
    }

    public MockMachine(boolean av){
        this.available = av;
    }

    public boolean isAvailable() {
        return available;
    }

}