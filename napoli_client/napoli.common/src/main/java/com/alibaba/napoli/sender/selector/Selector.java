/**
 * Project: napoli.client
 * 
 * File Created at Sep 15, 2009
 * $Id: Selector.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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

/**
 * 挑选器。从指定的对象列表中，挑出一个。
 * 
 * @author ding.lid
 */
public interface Selector<T> {
    /**
     * 从指定的对象列表中，挑出一个。
     * 
     * @param list 供挑选的对象列表。
     * @return 挑选出的对象。 <br>
     *         如果参数传入的列表是空的（size == 0），则返回<code>null</code>。<br>
     *         如果没有和要求的元素，则返回<code>null</code>。<br>
     *         比如在权重挑选器的情况下，供挑选的列表中的对象都是没有指定权重的(即认为不是要的候选对象)，则返回
     *         <code>null</code>。
     * @throws NullPointerException 如果传入参数是<code>null</code>时。
     */
    T select(List<T> list);

    /**
     * 重置该挑选器。
     */
    void reset();
}
