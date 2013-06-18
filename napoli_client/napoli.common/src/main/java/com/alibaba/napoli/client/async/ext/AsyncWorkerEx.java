package com.alibaba.napoli.client.async.ext;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.NapoliWorker;

public interface AsyncWorkerEx extends NapoliWorker {
	boolean doWork(NapoliMessage message);
}
