package com.alifi.mizar.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class FormatUtil {
    
    private FormatUtil() {};
    
    public static String formatString(String key, Map<String, String> map) {
        return map.get(key);
    }
    
    public static Boolean formatBoolean(String key, Map<String, String> map) {
        String defaultTrueValue = "T";
        String value = formatString(key, map);
        return value != null && value.equalsIgnoreCase(defaultTrueValue);
    }
    
    public static Integer formatInteger(String key, Map<String, String> map) {
        try {
            return Integer.parseInt(formatString(key, map));
        } catch (Exception e) {
            return -1;
        }
    }
    
    public static Long formatLong(String key, Map<String, String> map) {
        try {
            return Long.parseLong(formatString(key, map));
        } catch (Exception e) {
            return -1L;
        }
    }
    
    public static Double formatDouble(String key, Map<String, String> map) {
        try {
            return Double.parseDouble(formatString(key, map));
        } catch (Exception e) {
            return 0d;
        }
    }
    
    public static Date formatDate(String key, Map<String, String> map) {
        String date = formatString(key, map);
        String pattern = "yyyy-MM-dd";
        if (date.length() > 10) {
            pattern += " HH:mm:ss";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            return format.parse(formatString(key, map));
        } catch (Exception e) {
            return null;
        }
    }
}
