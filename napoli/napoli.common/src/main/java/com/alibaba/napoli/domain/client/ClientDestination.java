package com.alibaba.napoli.domain.client;

import java.io.Serializable;

/**
 * User: heyman
 * Date: 6/5/12
 * Time: 2:50 PM
 */
public class ClientDestination implements Serializable {
    private static final long serialVersionUID = -2989318308737491163L;
    public static final String TYPE_QUEUE = "queue";
    public static final String TYPE_VTOPIC = "vtopic";
    public static final String TYPE_TOPIC = "topic";
    

    protected String name;
    protected String type;
    protected long modified;
    protected boolean sendable = true;
    protected boolean receiveable = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public boolean isSendable() {
        return sendable;
    }

    public void setSendable(boolean sendable) {
        this.sendable = sendable;
    }

    public boolean isReceiveable() {
        return receiveable;
    }

    public void setReceiveable(boolean receiveable) {
        this.receiveable = receiveable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}