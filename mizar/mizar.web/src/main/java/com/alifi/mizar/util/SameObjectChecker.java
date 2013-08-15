/**
 * Project: ocean.common-1.1.0-SNAPSHOT
 * 
 * File Created at 2010-6-5
 * $Id: SameObjectChecker.java 111031 2011-09-09 07:32:14Z yuming.wangym $
 * 
 * Copyright 2008 Alibaba.com Croporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alifi.mizar.util;

import java.util.ArrayList;
import java.util.List;

/**
 * SameObjectChecker
 * @author jade
 *
 */
public class SameObjectChecker {
    private final List<Object> objs = new ArrayList<Object>(20);
    
    public boolean checkThenPush(Object in){
        if(in == null){
            return true;
        }
        for (Object obj : objs) {
            if(obj == in){
                return false;
            }
        }
        objs.add(in);
        return true;
    }
    
    public Object pop(){
        if(objs.size() == 0){
            return null;
        }
        return objs.remove(objs.size() - 1);
    }
}
