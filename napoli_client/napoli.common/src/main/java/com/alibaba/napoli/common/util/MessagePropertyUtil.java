package com.alibaba.napoli.common.util;

import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * message property Util
 * 
 * @author sait.xuc
 */
public class MessagePropertyUtil {

    private static final Log logger = LogFactory.getLog(MessagePropertyUtil.class);

    public static Long getMessageLongProperty(Message message, String proname) {
        long value = 0;
        try {
            if (message.propertyExists(proname)) {
                value = message.getLongProperty(proname);
            }
        } catch (Exception e) {
            logger.error("Get Mesage proname: " + proname + " value happen error. ");
        }
        return value;
    }

    public static String getMessageStringProperty(Message message, String proname) {
        String value = null;
        try {
            if (message.propertyExists(proname)) {
                value = message.getStringProperty(proname);
            }
        } catch (Exception e) {
            logger.error("Get Mesage proname: " + proname + " value happen error. ");
        }
        return value;
    }

    public static int getMessageIntProperty(Message message, String proname) {
        int value = 0;
        try {
            if (message.propertyExists(proname)) {
                value = message.getIntProperty(proname);
            }
        } catch (Exception e) {
            logger.error("Get Mesage proname: " + proname + " value happen error. ");
        }
        return value;
    }

    public static Double getMessageDoubleProperty(Message message, String proname) {
        double value = 0;
        try {
            if (message.propertyExists(proname)) {
                value = message.getDoubleProperty(proname);
            }
        } catch (Exception e) {
            logger.error("Get Mesage proname: " + proname + " value happen error. ");
        }
        return value;
    }

    public static Float getMessageFloatProperty(Message message, String proname) {
        float value = 0;
        try {
            if (message.propertyExists(proname)) {
                value = message.getFloatProperty(proname);
            }
        } catch (Exception e) {
            logger.error("Get Mesage proname: " + proname + " value happen error. ");
        }
        return value;
    }
}
