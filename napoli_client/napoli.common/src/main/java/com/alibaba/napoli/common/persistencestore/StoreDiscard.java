package com.alibaba.napoli.common.persistencestore;

import com.alibaba.napoli.client.async.NapoliMessage;

/**
 * User: heyman
 * Date: 6/8/12
 * Time: 1:05 PM
 */
public interface StoreDiscard {
    public void storeDiscard(NapoliMessage napoliMessage);
}