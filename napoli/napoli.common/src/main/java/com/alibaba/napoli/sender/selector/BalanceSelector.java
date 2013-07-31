/**
 * Project: napoli.client
 * 
 * File Created at Sep 19, 2009
 * $Id: BalanceSelector.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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
package com.alibaba.napoli.sender.selector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 遍历所有候选者被挑选的次数，以保证挑出被选次数最少的候选者。 <br>
 * 计算比较耗时，谨慎使用。
 * 
 * @author guolin.zhuanggl
 * @author ding.lid
 */
public class BalanceSelector<T> implements Selector<T> {
    Map<T, Long> balance = new HashMap<T, Long>();

    public synchronized T select(final List<T> givenList) {
        if (null == givenList) {
            throw new NullPointerException("argument is null!");
        }

        // 如果列表为空，返回null，表示没有合适的候选。
        if (givenList.isEmpty()) {
            return null;
        }

        // 如果只有一个候选，直接返回。
        if (givenList.size() == 1) {
            return givenList.get(0);
        }

        for (final T key : givenList) {
            if (!balance.containsKey(key)) { // 新增的候选者
                balance.put(key, 0L);
                // TODO: 新增者 设置为0，会导致之后的消息都发给它，直至平衡！
            }
        }

        for (Iterator<Entry<T, Long>> iterator = balance.entrySet().iterator(); iterator.hasNext();) {
            Entry<T, Long> next = iterator.next();
            T key = next.getKey();

            if (!givenList.contains(key)) { // 消失的候选者
                iterator.remove();
            }
        }

        long minCount = Long.MAX_VALUE;
        T ret = null;
        for (final T key : givenList) {
            if (balance.get(key) < minCount) {
                minCount = balance.get(key);
                ret = key;
            }
        }
        balance.put(ret, balance.get(ret) + 1);
        return ret;
    }

    public synchronized void reset() {
        balance.clear();
    }
}
