/**
 * Project: napoli.client
 * 
 * File Created at Sep 27, 2009
 * $Id: RoundRobinSelectorTest.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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
package com.alibaba.napoli.client.inner.selector;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.napoli.sender.selector.RoundRobinSelector;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ding.lid
 */
public class RoundRobinSelectorTest {
    RoundRobinSelector<String> selector = null;
    List<String>                list     = null;

    private static final String PREFIX   = "cadidate-";

    @Before
    public void setUp() throws Exception {
        selector = new RoundRobinSelector<String>();
        list = new ArrayList<String>();

        for (int i = 0; i < 10; ++i) {
            list.add(PREFIX + i);
        }
    }

    @Test
    public void test() {
        // 在我的开发机上，这个测试的运动时间不超过0.5秒。
        final int selectTime = 1000 * 1000; // 运行一百万次。 
        for (int i = 0; i < selectTime; ++i) {
            final String select = selector.select(list);
            assertEquals(PREFIX + ((i+1) % 10), select);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgument() {
        selector.select(null);
    }
}
