package com.alibaba.napoli.client.async.inner;

import java.io.Serializable;

public final class StoreItem implements Serializable{
    private static final long serialVersionUID = 7295522440157029764L;
    private String       queueName;
    private Serializable content;

    public StoreItem(String queueName, Serializable content){
        this.queueName = queueName;
        this.content = content;
    }

    public String getQueueName() {
        return queueName;
    }

    public Serializable getContent() {
        return content;
    }
    public String toString(){
        return "["+queueName+" , "+content+" ]";
    }
}
