package com.alibaba.napoli.receiver.filter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class SimpleContext implements Context {

    Map<String, String> props = new HashMap<String, String>();
    protected Serializable in, out;

    public SimpleContext(Serializable in){
        this.in = in;
        this.out = in;
    }

    public String getContextProperty(String name, String value) {
        return props.get(name);
    }

    public Serializable getInputObject() {
        return in;
    }

    public Serializable getOutputObject() {
        return out;
    }

    public void setContextProperty(String name, String value) {
        props.put(name, value);
    }

    public void setOutputObject(Serializable out) {
        this.out = out;
    }

}
