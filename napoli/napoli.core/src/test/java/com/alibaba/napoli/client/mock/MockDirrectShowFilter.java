package com.alibaba.napoli.client.mock;

import java.io.Serializable;

import com.alibaba.napoli.receiver.filter.Context;
import com.alibaba.napoli.receiver.filter.FilterFinder;
import com.alibaba.napoli.receiver.filter.Filter;


public class MockDirrectShowFilter implements Filter {

    public void destroy(){
        System.out.println("MockDirrectShowFilter destroy.");
    }

    public void filter(Context context, FilterFinder finder){
        System.out.println(context.getInputObject()); 
        Serializable out = context.getOutputObject();
        if(out != null){
            System.out.println(out); 
        }
        if(finder != null){
            Filter next = finder.nextFilter(this);
            if(next != null){
                next.filter(context, finder);
            }
        }
    }

    public void init(){
        System.out.println("MockDirrectShowFilter init.");

    }

}
