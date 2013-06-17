package com.alibaba.napoli.client.mock;

import java.io.Serializable;


import com.alibaba.napoli.receiver.filter.Context;
import com.alibaba.napoli.receiver.filter.FilterFinder;
import com.alibaba.napoli.receiver.filter.Filter;
import com.thoughtworks.xstream.XStream;

public class XStreamDecodeFilter implements Filter {

    XStream decoder;

    public void destroy(){
        decoder = null;
    }

    public void filter(Context context, FilterFinder next){
        Object obj = context.getOutputObject();
        if (obj instanceof String) {
            Serializable output = (Serializable) decoder.fromXML((String) obj);
            context.setOutputObject(output);
        } else {
            throw new RuntimeException("Object[" + obj + "] is not a string ,can not be filter.");
        }
        Filter nextFilter = next.nextFilter(this);
        if (nextFilter != null) {
            nextFilter.filter(context, next);
        }
    }

    public void init() {
        decoder = new XStream();
    }
}
