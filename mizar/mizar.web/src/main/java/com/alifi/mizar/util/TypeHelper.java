package com.alifi.mizar.util;

import java.util.Collection;
import java.util.Map;

public class TypeHelper {

    public static boolean isMapType(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    public static boolean isArrayType(Class<?> clazz) {
        return clazz.isArray() || Collection.class.isAssignableFrom(clazz);
    }

    public static boolean isCollectionType(Class<?> clazz) {
        return isMapType(clazz) || isArrayType(clazz);
    }
}
