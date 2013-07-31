package com.alibaba.napoli.client.async;

import com.alibaba.napoli.client.NapoliClientException;

/**
 * User: heyman
 * Date: 8/29/11
 * Time: 1:40 下午
 */
public class NapoliRecvStoreOkException extends NapoliClientException {
    private static final long serialVersionUID = 6747939651716293939L;

    public NapoliRecvStoreOkException(String msg) {
        super(msg);
    }

    public NapoliRecvStoreOkException(String msg, Throwable t) {
        super(msg, t);
    }
}
