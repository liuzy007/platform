/**
 * Project: napoli.client
 * 
 * File Created at Sep 22, 2009
 * $Id: RandomSelectorTest.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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

import com.alibaba.napoli.sender.selector.RandomSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ding.lid
 */
// TODO 没有加上异常系的测试： 1. 列表参数为null。 2. 列表只有一个参数。 etc..
public class RandomSelectorTest {
    private static final Log    log      = LogFactory.getLog(RandomSelectorTest.class);
    private static final String PREFIX   = "cadidate-";

    RandomSelector<String> selector = null;
    List<String>                list     = null;

    @Before
    public void setUp() throws Exception {
        selector = new RandomSelector<String>();

        list = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            list.add(PREFIX + i);
        }
    }

    @Test
    public void test() {
        final Map<String, Integer> counter = new HashMap<String, Integer>(10);

        // 在我的开发机上，这个测试的运动时间不超过0.5秒。
        final int selectTime = 1000 * 1000; // 运行一百万次。 
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
            // 抖动不超过10%
            assertTrue(actual > valueInPefectWorld / 10 * 9);
            assertTrue(actual < valueInPefectWorld / 10 * 11);

            // 实际测试输出，抖动很少超过0.5%
            log.info(entry.getKey() + " (actual/expected/proportion): " + actual + "/"
                    + valueInPefectWorld + "/" + (((double) actual / valueInPefectWorld - 1) * 100)
                    + "%");
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgument() {
        selector.select(null);
    }
}
