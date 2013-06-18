package com.alibaba.napoli.metamorphosis.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.napoli.metamorphosis.Message;

public class FilterImpl implements Filter {

    private static final Log                    log         = LogFactory.getLog(FilterImpl.class);
    private final FilterParser                  parser      = new FilterParser();
    private final SimpleString                  sfilterString;
    private final Object                        result;
    private final Class<? extends Object>       resultType;
    private final Map<SimpleString, Identifier> identifiers = new HashMap<SimpleString, Identifier>();

    public static Filter createFilter(final String filterStr) throws Throwable {
        return FilterImpl.createFilter(SimpleString.toSimpleString(filterStr == null ? null : filterStr.trim()));
    }

    public static Filter createFilter(final SimpleString filterStr) throws Throwable {
        if (filterStr == null || filterStr.length() == 0) {
            return null;
        } else {
            return new FilterImpl(filterStr);
        }
    }

    private FilterImpl(final SimpleString str) throws Throwable {
        sfilterString = str;

        try {
            result = parser.parse(sfilterString, identifiers);

            resultType = result.getClass();
        } catch (Throwable e) {
            throw e;
        }
    }

    @Override
    public synchronized boolean match(Message message) {
    	List<Identifier> ids = new ArrayList<Identifier>();
        try {
        	Map<String, Object> props = JSON.parseObject(message.getAttribute());
        	/*if(!props.containsKey("type")){
        		System.err.println("error occurs");
        	}*/
            for (Identifier id : identifiers.values()) {
                String key = id.getName().toString();
                Object val = props.get(key);
                if (val != null) {
                    if (val instanceof String){
                        id.setValue(new SimpleString(val.toString()));
                    }else {
                        id.setValue(val);
                    }
                    ids.add(id);
                }
            }
            if (resultType.equals(Identifier.class)) {
                return (Boolean) ((Identifier) result).getValue();
            } else if (resultType.equals(Operator.class)) {
                Operator op = (Operator) result;
                return (Boolean) op.apply();
            } else {
                throw new Exception("Bad object type: " + result);
            }
        } catch (Exception e) {
            log.error(e);
            return false;
        }finally{
        	for(Identifier id:ids){
        		id.setValue(null);
        	}
        }
    }

    /*private Properties getProps(Message message) {
        Properties props = new Properties();
        if (message == null || StringUtils.isBlank(message.getAttribute())) {
            return props;
        }
        String attribute = message.getAttribute();
        Map<String, Object> msgProps = JSON.parseObject(attribute);
        *//*
         * String[] array = StringUtils.split(attribute); if(array == null ||
         * array.length == 0) return props; for(String str:array){
         * if(str.indexOf("=") != -1){
         * props.setProperty(StringUtils.trim(str.substring
         * (0,str.indexOf("="))),
         * StringUtils.trim(str.substring(str.indexOf("=")+1))); } } return
         * props;
         *//*
    }*/

}
