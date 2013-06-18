package com.alibaba.napoli.receiver.filter;

import java.io.Serializable;

public interface Context {

    Serializable getInputObject();

    Serializable getOutputObject();

    void setOutputObject(Serializable out);

    void setContextProperty(String name, String value);

    String getContextProperty(String name, String value);

}
