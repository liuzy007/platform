package com.alibaba.napoli.mqImpl.activemq;


import com.alibaba.napoli.client.async.NapoliMessage;
import org.apache.activemq.command.ActiveMQObjectMessage;

import javax.jms.*;
import java.io.Serializable;
import java.util.Map;
import org.apache.activemq.command.ActiveMQTextMessage;

/**
 * User: heyman
 * Date: 1/17/12
 * Time: 5:18 下午
 */
public class AMQMessageUtil {
    public static Message toJmsMessage(NapoliMessage napoliMessage, Session session) throws JMSException {
        if (napoliMessage.getContent() == null){
            throw new JMSException("content type error or content is null ");
        }
        if (napoliMessage.getContent() instanceof Message){
            return (Message)napoliMessage.getContent();
        }
        
        Message jmsMessage;
        Serializable content = (Serializable) napoliMessage.getContent();
        Map<String, Object> props = napoliMessage.getProps();
        if (content instanceof String) {
            ActiveMQTextMessage txtMessage = (ActiveMQTextMessage)session.createTextMessage();
            txtMessage.setText((String) content);
            jmsMessage = txtMessage;
            napoliMessage.setProperty(NapoliMessage.MSG_PROP_KEY_MSGSIZE, ((String) content).length());
        } else {
            ObjectMessage objMessage = session.createObjectMessage();
            objMessage.setObject(content);
            jmsMessage = objMessage;
            if (((ActiveMQObjectMessage) jmsMessage).getContent() != null) {
                napoliMessage.setProperty(NapoliMessage.MSG_PROP_KEY_MSGSIZE, ((ActiveMQObjectMessage) jmsMessage).getContent().getLength());
            }
        }
        // 优先级和超时属性
        Object p = props.get(NapoliMessage.MSG_PROP_KEY_PRIORITY);
        if (p instanceof Integer) {
            int priority = (Integer) p;
            jmsMessage.setJMSPriority(priority);
        }
        Object e = props.get(NapoliMessage.MSG_PROP_KEY_EXPIRATION);
        if (e instanceof Long) {
            long expiration = ((Long) e).intValue();
            jmsMessage.setJMSExpiration(expiration);
        }
        // 其它属性，包括用户自定义属性
        for (String key : props.keySet()) {
            if (key.equals(NapoliMessage.MSG_PROP_KEY_EXPIRATION) || key.equals(NapoliMessage.MSG_PROP_KEY_PRIORITY)
                    || key.equals(NapoliMessage.MSG_PROP_KEY_STORE_ENABLE)) {
                continue;
            }
            jmsMessage.setObjectProperty(key, props.get(key));
        }
        // 设置消息内容
        return jmsMessage;
    }
}
