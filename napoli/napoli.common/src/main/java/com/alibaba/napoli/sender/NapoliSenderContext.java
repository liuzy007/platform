package com.alibaba.napoli.sender;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.connector.ConsoleConnector;

import com.alibaba.napoli.domain.client.ClientMachine;
import java.util.HashMap;
import java.util.Map;

/**
 * User: heyman Date: 2/16/12 Time: 2:21 下午
 */
public class NapoliSenderContext {
    private final String           destinationName;
    private final ClientMachine          machine;
    private NapoliMessage          message;
    private final ConsoleConnector connector;
    private boolean                storeOk;

    private Map<String, String>    props;
    private final ConnectionParam  connectionParam;
    private Runnable               bizCall;

    public NapoliSenderContext(String destinationName, ClientMachine machine, NapoliMessage message, ConnectionParam params,
                               ConsoleConnector connector) {
        this.destinationName = destinationName;
        this.machine = machine;
        this.message = message;
        this.connector = connector;
        this.props = new HashMap<String, String>();
        this.connectionParam = params;
    }

    public ConnectionParam getConnectionParam() {
        return connectionParam;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public ClientMachine getMachine() {
        return machine;
    }

    public NapoliMessage getMessage() {
        return message;
    }

    public ConsoleConnector getConnector() {
        return connector;
    }

    public boolean isStoreOk() {
        return storeOk;
    }

    public void setStoreOk(boolean storeOk) {
        this.storeOk = storeOk;
    }

    public void setPropertie(String key, String value) {
        props.put(key, value);
    }

    public String getPropertie(String key) {
        return props.get(key);
    }

    public Runnable getBizCall() {
        return bizCall;
    }

    public void setBizCall(Runnable bizCall) {
        this.bizCall = bizCall;
    }
}
