package com.alibaba.napoli.mqImpl.hornetQ;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.common.NapoliException;
import com.alibaba.napoli.common.util.HessianUtil;
import com.alibaba.napoli.common.util.NapoliMessageUtil;
import com.alibaba.napoli.common.util.StringUtils;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hornetq.api.core.HornetQBuffer;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientSession;

/**
 * User: heyman Date: 1/17/12 Time: 5:18 下午
 */
public class HornetQMessageUtil {
    protected static final Log logger = LogFactory.getLog(HornetQMessageUtil.class);

    public static Serializable getMessageContent(ClientMessage clientMessage) {

        if (clientMessage.getType() == org.hornetq.api.core.Message.TEXT_TYPE) {

            HornetQBuffer buffer = clientMessage.getBodyBuffer();
            return buffer.duplicate().readSimpleString().toString();
            //return clientMessage.getBodyBuffer().readString();
        } else if (clientMessage.getType() == org.hornetq.api.core.Message.BYTES_TYPE) {

            byte[] b = new byte[clientMessage.getBodySize()];

            clientMessage.getBodyBuffer().readBytes(b);
            /*
             * ByteArrayInputStream bais = new ByteArrayInputStream(b);
             * ObjectInputStream ois; Serializable object = null; try { ois =
             * new org.hornetq.utils.ObjectInputStreamWithClassLoader(bais);
             * object = (Serializable) ois.readObject(); } catch (Exception e) {
             * logger.error("getMessageBody for BYTES_TYPE error!", e); }
             */
            Serializable object = null;

            try {
                object = HessianUtil.deserialize(b);
            } catch (Exception e) {
                logger.error("getMessageBody for BYTES_TYPE error!", e);
            }

            return object;

        } else {
            return "unsupported message type " + clientMessage.getType();
        }

    }

    public static ClientMessage toClientMessage(NapoliMessage napoliMessage, ClientSession session)
            throws HornetQException {
        ClientMessage clientMessage;
        //for router
        if (napoliMessage.getContent() instanceof ClientMessage) {
            return (ClientMessage) napoliMessage.getContent();
        }
        Serializable content = (Serializable) napoliMessage.getContent();
        Map<String, Object> props = napoliMessage.getProps();
        if (content == null) {
            throw new HornetQException(2, "content type error or content is null " + content);
        }
        // set message content
        if (content instanceof String) {
            clientMessage = session.createMessage(org.hornetq.api.core.Message.TEXT_TYPE, true);
            clientMessage.getBodyBuffer().writeSimpleString(new SimpleString((String) content));
            napoliMessage.setProperty(NapoliMessage.MSG_PROP_KEY_MSGSIZE, ((String) content).length());
        } else {
            clientMessage = session.createMessage(org.hornetq.api.core.Message.BYTES_TYPE, true);
            try {
                byte[] data = HessianUtil.serialize(content);
                clientMessage.getBodyBuffer().writeBytes(data);
                if (data != null) {
                    //get message size.
                    napoliMessage.setProperty(NapoliMessage.MSG_PROP_KEY_MSGSIZE, data.length);
                }
            } catch (Exception e) {
                throw new HornetQException(3, "set objectMessage error " + content);
            }
        }

        //   System.out.println("yanny message size is " + napoliMessage.getProperty(NapoliMessage.MSG_PROP_KEY_MSGSIZE));

        // priority and timeout
        Object p = props.get(NapoliMessage.MSG_PROP_KEY_PRIORITY);
        if (p instanceof Integer) {
            int priority = (Integer) p;
            clientMessage.setPriority((byte) priority);
        }
        Object e = props.get(NapoliMessage.MSG_PROP_KEY_EXPIRATION);
        if (e instanceof Long) {
            long expiration = ((Long) e).intValue();
            clientMessage.setExpiration(expiration);
        }
        // other properties, customized properities
        for (String key : props.keySet()) {
            if (key.equals(NapoliMessage.MSG_PROP_KEY_EXPIRATION) || key.equals(NapoliMessage.MSG_PROP_KEY_PRIORITY)
                    || key.equals(NapoliMessage.MSG_PROP_KEY_STORE_ENABLE)) {
                continue;
            }
            clientMessage.putObjectProperty(key, props.get(key));
        }

        return clientMessage;
    }

    public static NapoliMessage fromClientMessage(ClientMessage message) throws NapoliException {
        try {
            Serializable content = getMessageContent(message);

            NapoliMessage napoliMessage = new NapoliMessage(content);

            String queueName = message.getStringProperty(NapoliMessage.MSG_PROP_KEY_QUEUENAME);
            if (!StringUtils.isEmpty(queueName)) {
                napoliMessage.setProperty(NapoliMessage.MSG_PROP_KEY_QUEUENAME, queueName);
            }
            // 优先级和超时属性
            napoliMessage.setExpiration(message.getExpiration());
            napoliMessage.setPriority(message.getPriority());
            // 其它属性，包括用户自定义属性
            Set<SimpleString> props = message.getPropertyNames();

            for (SimpleString propname : props) {
                //inner property don't copy
                if (NapoliMessageUtil.isInnerProperties(propname.toString())) {
                    continue;
                }
                napoliMessage.setProperty(propname.toString(), message.getObjectProperty(propname));
            }
            return napoliMessage;
        } catch (Exception e) {
            throw new NapoliException("convert clientMessage to napoliMessage error!" + e.getMessage(), e);
        }
    }

    public static Long getMessageLongProperty(ClientMessage message, String proname) {
        long value = 0;
        try {
            value = message.getLongProperty(proname);
        } catch (Exception e) {
            logger.error("Get Mesage proname: " + proname + " value happen error. ");
        }
        return value;
    }

    public static String getMessageStringProperty(ClientMessage message, String proname) {
        String value = null;
        try {
            value = message.getStringProperty(proname);
        } catch (Exception e) {
            logger.error("Get Mesage proname: " + proname + " value happen error. ");
        }
        return value;
    }

}
