/**
 * Project: napoli.client
 * 
 * File Created at 2011-9-19
 * $Id: MockMock.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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

import java.io.Serializable;

public class MockMock implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String          name;

    public MockMock(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

