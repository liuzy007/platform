package com.alifi.mizar.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;

import junit.framework.TestCase;


public class OperationStringUtilTest extends TestCase {

    public void testParseQueryString() throws EncoderException {
        StringBuffer queryString = new StringBuffer();
        queryString.append("name=").append(new URLCodec().encode("t&c"));

        Map<String, String> parsed = OperationStringUtil.parseAndDecodeQueryString(queryString.toString());
        assertEquals("t&c", parsed.get("name"));
    }
    
    public void testParseQueryStringWhenWithoutValue() {
        Map<String, String> parsed = OperationStringUtil.parseAndDecodeQueryString("name=");
        assertEquals(0, parsed.entrySet().size());
    }
    
    public void testGetSortParameters() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("b", "second");
        params.put("a", "first");

        assertEquals("a=first&b=second", OperationStringUtil.getSortParameters(params));
    }
    
    public void testConvertMapOfArrayToString() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("name", new String[]{"tomp", "chen"});

        assertEquals("tomp", OperationStringUtil.convertMapOfArrayToString(params).get("name"));
    }
}
