package com.alibaba.napoli.sender.listener;


import com.alibaba.napoli.domain.client.ClientMachine;

/**
 * User: heyman
 * Date: 2/24/12
 * Time: 11:30 上午
 */
public interface TransportSenderListener {
    public void close(ClientMachine machine);
}
