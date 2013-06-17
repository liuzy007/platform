/**
 * Project: napoli.client
 * 
 * File Created at Sep 15, 2009
 * $Id: RoundRobinSelector.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询挑选器。
 * 
 * @author ding.lid
 */
public class RoundRobinSelector<T> implements Selector<T> {

    private final AtomicInteger index = new AtomicInteger();

    /**
     * 轮询的方式从候选列表中挑选一个对象。<br>
     * 当候选者列表不断变化、且变化较大的时候，这个算法会有问题。<br>
     * 列表少量变动时，挑选结果可以比较稳定。
     */
    public T select(final List<T> givenList) {
        if (null == givenList) {
            throw new NullPointerException("argument is null!");
        }

        final int size = givenList.size();
        // 如果列表为空，返回null，表示没有合适的候选。
        if (size == 0) {
            return null;
        }

        // 如果只有一个候选，直接返回。
        if (size == 1) {
            return givenList.get(0);
        }
        // 以下代码拷贝于AtomicInteger.getAndIncrement()，修改了next值的设置
        int next;
        for (;;) {
            int current = index.get();
            next = (current == Integer.MAX_VALUE ? 0 : current + 1);
            if (index.compareAndSet(current, next)){
             break;
            }
        }
        return givenList.get(next % size);
    }

    public void reset() {
        index.set(0);
    }
}
