package com.alibaba.napoli.mqImpl.activemq;

import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.util.NapoliMessageUtil;

import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.mqImpl.TransportException;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.JMSException;
import java.util.Properties;

/**
 * User: heyman
 * Date: 12/2/11
 * Time: 10:04 下午
 */
public class AMQConnectionFactory {
    private static final Log logger = LogFactory.getLog(AMQConnectionFactory.class);

    public static AMQConnection createConnection(ClientMachine machine, ConnectionParam params) throws TransportException {
        String address = machine.getIp()+":"+machine.getPort();
        if (!NapoliMessageUtil.verifyUrl(address)) {
            throw new IllegalArgumentException("illegal url[" + address + "]");
        }
        String brokerURL = "tcp://" + address + "?";
        brokerURL += "wireFormat.maxInactivityDuration=0";
        brokerURL += "&jms.prefetchPolicy.all=" + params.getPrefetch();
        brokerURL += "&" + "connectionTimeout=" + params.getConnectionTimeout();
        brokerURL += "&" + "jms.sendTimeout=" + params.getSendTimeout();

        Properties amqProperties = new Properties();
        amqProperties.put("brokerURL", brokerURL);
        amqProperties.put("userName", params.getJmsUserName());
        amqProperties.put("password", params.getJmsPassword());
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.buildFromProperties(amqProperties);
        ActiveMQConnection connection = null;
        try {
            connection = (ActiveMQConnection) activeMQConnectionFactory.createConnection();
            connection.start();
            return new AMQConnection(connection, params.getSendPoolsize());
        } catch (JMSException e) {
            if (connection != null) {
                //以上两个步骤有一个出错，关闭连接
                try {
                    connection.stop();
                } catch (JMSException e1) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("fail to stop connection:" + connection + ",reason:" + e1);
                    }
                }
                try {
                    connection.close();
                } catch (JMSException e2) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("fail to close connection:" + connection + ",reason:", e2);
                    }
                }
            }
            throw new TransportException("create connection failed with exception for brokerUrl:" + brokerURL, e);
        }

    }
}