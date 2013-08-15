package com.alibaba.napoli.client.mock;

import com.alibaba.napoli.receiver.filter.Context;
import com.alibaba.napoli.receiver.filter.FilterFinder;
import com.alibaba.napoli.receiver.filter.Filter;
import com.thoughtworks.xstream.XStream;

public class XStreamEncodeFilter implements Filter {

    XStream encoder;

    public void destroy(){
        encoder = null;
    }

    public void filter(Context context, FilterFinder next){
        Object obj = context.getOutputObject();
//        encoder.alias(alias(obj), obj.getClass());
        String output = encoder.toXML(obj);
        context.setOutputObject(output);
        Filter nextFilter = next.nextFilter(this);
        if (nextFilter != null) {
            nextFilter.filter(context, next);
        }

    }

    public void init(){
        encoder = new XStream();
    }

//    private static String alias(Object obj) {
//        String clazz = obj.getClass().getCanonicalName();
//        int index = clazz.lastIndexOf(".");
//        if (index >= 0) {
//            return clazz.substring(index + 1);
//        } else {
//            return clazz;
//        }
//    }
}
