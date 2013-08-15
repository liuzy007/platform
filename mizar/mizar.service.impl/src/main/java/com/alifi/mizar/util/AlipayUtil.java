package com.alifi.mizar.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.common.lang.StringUtil;

import static com.alifi.mizar.util.FormatUtil.*;

public class AlipayUtil {
    
    private AlipayUtil() {};
    
    private static final Log LOG = LogFactory.getLog(AlipayUtil.class);
    
    public static <T> T map2Object(Map<String, String> keyAndValues, Class<T> clazz, Map<String, String> mapping) {
        try {
            T obj = clazz.newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || !Modifier.isPrivate(modifiers)) {
                    continue;
                }
                String key = field.getName();
                if (mapping != null && mapping.containsKey(key)) {
                    key = mapping.get(key);
                }
                if (!keyAndValues.containsKey(key)) {
                    key = StringUtil.toLowerCaseWithUnderscores(key);
                }
                if (!keyAndValues.containsKey(key)) {
                    continue;
                }
                setValue(field, obj, key, keyAndValues);
            }
            return obj;
        } catch (Exception e) {
            LOG.error("got exception when create new class [" + clazz.getName() + "]", e);
            return null;
        }
    }

    public static <T> T map2Object(Map<String, String> keyAndValues, Class<T> clazz) {
        return map2Object(keyAndValues, clazz, null);
    }

    public static Map<String, String> object2Map(Object obj) {
        Map<String, String> map = new HashMap<String, String>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || !Modifier.isPrivate(modifiers)) {
                continue;
            }
            Object value = getValueOfField(field, obj);
            if (value == null || value.equals("")) {
                continue;
            }
            map.put(StringUtil.toLowerCaseWithUnderscores(field.getName()), value.toString());
        }
        return map;
    }

    private static void setValue(Field field, Object obj, String key, Map<String, String> keyAndValues) throws IllegalArgumentException, IllegalAccessException {
        Class<?> type = field.getType();
        field.setAccessible(true);
        if (type.equals(boolean.class)) {
            field.setBoolean(obj, formatBoolean(key, keyAndValues));
        } else if (type.equals(int.class)) {
            field.setInt(obj, formatInteger(key, keyAndValues));
        } else if (type.equals(double.class)) {
            field.setDouble(obj, formatDouble(key, keyAndValues));
        } else if (type.equals(long.class)) {
            field.setLong(obj, formatLong(key, keyAndValues));
        } else {
            field.set(obj, formatData(key, keyAndValues, type));
        }
    }

    private static Object formatData(String key, Map<String, String> keyAndValues, Class<?> type) {
        if (type.equals(Boolean.class)) {
            return formatBoolean(key, keyAndValues);
        }
        if (type.equals(Integer.class)) {
            return formatInteger(key, keyAndValues);
        }
        if (type.equals(Double.class)) {
            return formatDouble(key, keyAndValues);
        }
        if (type.equals(Long.class)) {
            return formatLong(key, keyAndValues);
        }
        if (type.equals(String.class)) {
            return formatString(key, keyAndValues);
        }
        if (type.equals(Date.class)) {
            return formatDate(key, keyAndValues);
        }
        return null;
    }

    private static Object getValueOfField(Field field, Object obj) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            LOG.error("cann't get value of [" + field.getName() + "]", e);
            return null;
        }
    }
}
