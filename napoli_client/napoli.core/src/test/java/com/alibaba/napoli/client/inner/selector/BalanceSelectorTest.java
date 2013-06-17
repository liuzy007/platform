/**
 * Project: napoli.client
 * 
 * File Created at Sep 27, 2009
 * $Id: BalanceSelectorTest.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.napoli.sender.selector.BalanceSelector;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ding.lid
 */
public class BalanceSelectorTest {
    private static final String PREFIX   = "cadidate-";

    BalanceSelector<String> selector = null;
    List<String>                list     = null;

    @Before
    public void setUp() throws Exception {
        selector = new BalanceSelector<String>();
        list = new ArrayList<String>();

        for (int i = 0; i < 10; ++i) {
            list.add(PREFIX + i);
        }
    }

    @Test
    public void test() {
        final Map<String, Integer> counter = new HashMap<String, Integer>(10);

        // 在我的开发机上，这个测试的运动时间 ~0.5秒。
        final int selectTime = 100 * 1000; // 运行十万次。 
        for (int i = 0; i < selectTime; ++i) {
            final String select = selector.select(list);
            if (counter.containsKey(select)) {
                final Integer integer = counter.get(select);
                counter.put(select, integer + 1);
            } else {
                counter.put(select, 1);
            }
        }

        final int valueInPefectWorld = selectTime / list.size();
        for (final Map.Entry<String, Integer> entry : counter.entrySet()) {
            final int actual = entry.getValue().intValue();
            assertTrue(actual < valueInPefectWorld + 1);
            assertTrue(actual > valueInPefectWorld - 1);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgument() {
        selector.select(null);
    }
}
