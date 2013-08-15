package com.alibaba.napoli.spi;

import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.mqImpl.TransportException;
import com.alibaba.napoli.receiver.filter.Filter;

import java.util.List;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * User: heyman Date: 12/14/11 Time: 3:12 下午
 */
public interface TransportConsumer {
    public void startListen();

    public void stopListen();

    //public void close();
    public void onSessionException(Exception exception, Session session, MessageConsumer messageConsumer);

    public void heartbeat();

    public int getInstance();

    public Message receive(int during);

    String getName();

    TransportConsumer createTransportConsumer(ConsoleConnector connector, ClientMachine machine,
                                              ConnectionParam connectionParam, NapoliWorker worker,
                                              List<Filter> filterList) throws TransportException;
}