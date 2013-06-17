/**
 * Project: napoli.client
 * 
 * File Created at Sep 15, 2009
 * $Id: WeightSelector.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 权重挑选器。按照权重挑出候选对象，即权重高的对象，挑中的概率高。
 * 
 * @author ding.lid
 */
public class WeightSelector<T> implements Selector<T> {
    private final Map<T, Integer> candidateWeightMap = new HashMap<T, Integer>();
    private final Random          random             = new Random();

    /**
     * 指定要的候选者的权重。
     * 
     * @param weight 一个候选者映射到权重的Map。
     * @throws IllegalArgumentException 权重不对（如，为空、或是不是正数）
     */
    public void setWeightForCandidate(final Map<T, Integer> weight) {
        for (final Map.Entry<T, Integer> entry : weight.entrySet()) {
            // 如果权重为空，或是小于等于0
            if (entry.getValue() == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException("weight(" + entry.getValue() + ") for object("
                        + entry.getKey() + ") must be a POSITIVE number!");
            }
        }

        this.candidateWeightMap.clear();
        this.candidateWeightMap.putAll(weight);
    }

    public synchronized T select(final List<T> givenList) {
        if (null == givenList) {
            throw new NullPointerException("argument is null!");
        }

        // 如果列表为空，返回null，表示没有合适的候选。
        if (givenList.isEmpty()) {
            return null;
        }

        // 如果只有一个候选
        if (givenList.size() == 1) {
            final T candidate = givenList.get(0);

            if (candidateWeightMap.get(candidate) != null) {
                return candidate;
            } else {
                return null;
            }
        }

        int totolWeight = 0;
        final List<T> candidatesWithWeight = new ArrayList<T>();

        for (final T candidate : givenList) {
            final Integer weight = candidateWeightMap.get(candidate);

            // 如果候选没有权重，则这个候选不放入列表中，即不会有被选中的机会。
            if (null != weight) {
                totolWeight += weight;
                candidatesWithWeight.add(candidate);
            }
        }

        if (candidatesWithWeight.isEmpty()) {
            return null;
        }

        final int position = random.nextInt(totolWeight);

        int sum = 0;
        for (final T candidate : candidatesWithWeight) {
            sum += candidateWeightMap.get(candidate);
            if (sum > position) {
                return candidate;
            }
        }

        // 不可能到这里
        return null;
    }

    public void reset() {
        // do nothing!
    }
}
