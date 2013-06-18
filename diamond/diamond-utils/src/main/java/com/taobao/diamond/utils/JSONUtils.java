/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.utils;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;


public class JSONUtils {

    public static String serializeObject(Object o) throws Exception {
        return mapper.writeValueAsString(o);
    }

    static ObjectMapper mapper = new ObjectMapper();


    public static Object deserializeObject(String s, Class<?> clazz) throws Exception {
        return mapper.readValue(s, clazz);
    }
    
    public static Object deserializeObject(String s,TypeReference<?> typeReference) throws Exception {
        return mapper.readValue(s, typeReference);
    }
}
