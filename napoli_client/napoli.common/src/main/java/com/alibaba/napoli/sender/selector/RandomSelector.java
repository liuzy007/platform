/**
 * Project: napoli.client
 * 
 * File Created at Sep 15, 2009
 * $Id: RandomSelector.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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
import java.util.Random;

/**
 * 随机挑选器。
 * 
 * @author ding.lid
 */
public class RandomSelector<T> implements Selector<T> {
    /**
     * 随机从列表中挑选一个对象。
     */
    public T select(final List<T> givenList) {
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

        // NOTE: 没有把Random作为成员变量，为了避免同步！
        // 结果： 运行一百万次的时间 由 不到0.2秒 变成 不到0.4秒。（在我的开发机上）
        final Random random = new Random();
        return givenList.get(random.nextInt(givenList.size()));
    }

    public void reset() {
        // do nothing!
    }
}
