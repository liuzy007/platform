package com.alifi.mizar.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;

import junit.framework.TestCase;

import static com.alifi.mizar.util.FormatUtil.*;


public class FormatUtilTest extends TestCase {
    
    private Map<String, String> params;
    
    public void setUp() {
        params = new HashMap<String, String>();
        params.put("name", "tomp");
        params.put("isMale", "t");
        params.put("isFemale", "F");
        params.put("number", "10");
        params.put("notNumber", "string");
        params.put("complexDate", "2011-11-11 11:11:11");
        params.put("simpleDate", "2011-11-11");
    }

    public void testFormatString() {
        assertEquals("tomp", formatString("name", params));
    }
    
    public void testFormatBoolean() {
        assertTrue(formatBoolean("isMale", params));
        assertFalse(formatBoolean("isFemale", params));
    }
    
    public void testFormatInteger() {
        assertEquals(new Integer(10), formatInteger("number", params));
        assertEquals(new Integer(-1), formatInteger("notNumber", params));
    }
    
    public void testFormatLong() {
        assertEquals(new Long(10L), formatLong("number", params));
        assertEquals(new Long(-1L), formatLong("notNumber", params));
    }
    
    public void testFormatDouble() {
        assertEquals(new Double(10D), formatDouble("number", params));
        assertEquals(new Double(0D), formatDouble("notNumber", params));
    }
    
    public void testFormatDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2011, 10, 11, 11, 11, 11);
        assertTrue(DateUtils.isSameDay(calendar.getTime(), formatDate("complexDate", params)));
        assertTrue(DateUtils.isSameDay(calendar.getTime(), formatDate("simpleDate", params)));
        assertNull(formatDate("notNumber", params));
    }
}
