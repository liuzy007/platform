package com.alibaba.napoli.client.async.router;

import com.alibaba.napoli.client.async.NapoliWorker;

/**
 * User: heyman
 * Date: 3/22/12
 * Time: 1:36 下午
 */
public interface RouterWorker extends NapoliWorker {
    boolean doWork(Object message);
}
