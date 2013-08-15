package com.alifi.mizar.util;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class XMLSerializeHelper {

    protected final static int  HIGHEST_ENCODABLE_TEXT_CHAR = '>';
    private final static Log logger = LogFactory.getLog(XMLSerializeHelper.class);

    public static void objectSerialize(XMLStreamWriter write, Object result)
            throws XMLStreamException {
        objectSerialize(write, result, null);
    }

    protected static boolean needCData(String value) {
        final int length = value.length();
        for (int i = 0; i < length; i++) {
            final char c = value.charAt(i);
            if (c <= HIGHEST_ENCODABLE_TEXT_CHAR) {
                if (c <= 0x001f) {
                    if (c != '\n' && c != '\t' && c != '\r') { // fine as is
                        return true;
                    }
                } else if (c == '<' || c == '&') {
                    return true;
                }
            }
        }
        return false;
    }

    public static void objectSerialize(XMLStreamWriter writer, Object result, SameObjectChecker objs)
            throws XMLStreamException {
        if (result == null) {
            return;
        }
        final Class<?> clazz = result.getClass();
        if (CharSequence.class.isAssignableFrom(clazz)) {
            final String value = String.valueOf(result);
            if (needCData(value)) {
                writer.writeCData(value);
            } else {
                writer.writeCharacters(value);
            }
        } else if (Number.class.isAssignableFrom(clazz) || clazz == Boolean.class
                || clazz == Character.class) {
            writer.writeCharacters(String.valueOf(result));
        } else
        // 日期
        if (Date.class.isAssignableFrom(clazz)) {
            writer.writeCharacters(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) result));
        } else
        if(Class.class.equals(clazz)){
            writer.writeCharacters(((Class<?>)result).getSimpleName());
        }else
        // 数组
        if (TypeHelper.isArrayType(clazz)) {
            if (objs == null) {
                objs = new SameObjectChecker();
            }
            if (objs.checkThenPush(result)) {
                objectListSerialize(writer, result, objs, StringUtils.EMPTY);
                objs.pop();
            } else {
               logger.warn("iterative reference ,ignore it.");
            }
        } else
        // map
        if (TypeHelper.isMapType(clazz)) {
            if (objs == null) {
                objs = new SameObjectChecker();
            }
            if (objs.checkThenPush(result)) {
                Map<?, ?> map = (Map<?, ?>) result;
                objectMapSerialize(writer, map, objs);
            } else {
                logger.warn("iterative reference ,ignore it.");
            }
        } else {
            writer.writeCharacters(result.toString());
        }
    }

    private static void objectListSerialize(XMLStreamWriter writer, Object result,
                                            SameObjectChecker objs, String name)
            throws XMLStreamException {
        if (result.getClass().isArray()) {
            final int length = Array.getLength(result);
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    if (Array.get(result, i) != null) {
                        objectSerialize(writer, Array.get(result, i), objs);
                    }
                }
            }
        } else {
            final Collection<?> coll = (Collection<?>) result;
            final int length = coll.size();
            if (length > 0) {
                for (Object obj : coll) {
                    if (obj != null) {
                        objectSerialize(writer, obj, objs);
                    }
                }
            }
        }
    }

    private static void objectMapSerialize(XMLStreamWriter writer, Map<?, ?> map,
                                           SameObjectChecker objs) throws XMLStreamException {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            final String key = String.valueOf(entry.getKey());
            final Object value = entry.getValue();
            if (value != null) {
                final Class<?> clazz = value.getClass();
                if (TypeHelper.isArrayType(clazz)) {
                    if (objs == null) {
                        objs = new SameObjectChecker();
                    }
                    if (objs.checkThenPush(value)) {
                        objectListSerialize(writer, value, objs, key);
                        objs.pop();
                    } else {
                        logger.warn("iterative reference ,ignore it.");
                    }
                    continue;
                }
            }
            if (value != null) {
                writer.writeStartElement(key);
                objectSerialize(writer, value, objs);
                writer.writeEndElement();
            }
        }
    }

    private XMLSerializeHelper() {
    }
}
