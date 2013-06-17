/**
 * Project: napoli.client
 * 
 * File Created at Sep 27, 2009
 * $Id: WeightSelectorTest.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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

import com.alibaba.napoli.sender.selector.WeightSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ding.lid
 */
public class WeightSelectorTest {
    private static final Log    log         = LogFactory.getLog(WeightSelectorTest.class);
    private static final String PREFIX      = "cadidate-";
    private final static int[]  weights     = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };

    WeightSelector<String> selector    = null;
    List<String>                list        = null;
    int                         totolWeight = 0;

    @Before
    public void setUp() throws Exception {
        list = new ArrayList<String>();
        selector = new WeightSelector<String>();

        final Map<String, Integer> weightMap = new HashMap<String, Integer>();

        for (int i = 0; i < weights.length; ++i) {
            weightMap.put(PREFIX + i, weights[i]);
            list.add(PREFIX + i);

            totolWeight += weights[i];
        }
        selector.setWeightForCandidate(weightMap);
    }

    @Test
    public void test() {
        final Map<String, Integer> counter = new HashMap<String, Integer>(10);

        // 在我的开发机上，这个测试的运动时间 ~1秒。
        final int selectTime = 500 * 1000; // 运行50万次。 
        for (int i = 0; i < selectTime; ++i) {
            final String select = selector.select(list);
            if (counter.containsKey(select)) {
                final Integer integer = counter.get(select);
                counter.put(select, integer + 1);
            } else {
                counter.put(select, 1);
            }
        }

        final int valueInPefectWorld = selectTime / totolWeight;
        for (int i = 0; i < list.size(); ++i) {
            final int actual = counter.get(PREFIX + i);
            final int expected = valueInPefectWorld * weights[i];

            // 抖动不超过10%
            assertTrue(actual > expected / 10 * 9);
            assertTrue(actual < expected / 10 * 11);

            // 实际测试输出，抖动多半不超过0.5%
            log.info(PREFIX + i + " (actual/expected/proportion): " + actual + "/" + expected + "/"
                    + (((double) actual / expected - 1) * 100) + "%");
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgument() {
        selector.select(null);
    }

}
