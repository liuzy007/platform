package com.alibaba.napoli.mqImpl;

import com.alibaba.napoli.spi.TransportConsumer;
import com.alibaba.napoli.spi.TransportSender;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman
 * Date: 3/8/12
 * Time: 5:35 下午
 */
public class ProducerExceptionListener implements ExceptionListener {
    private TransportSender transportSender;
    private static final Log logger = LogFactory.getLog(ProducerExceptionListener.class);

    public ProducerExceptionListener(TransportSender transportSender) {
        this.transportSender = transportSender;
    }

    public void onException(JMSException e) {
        if (logger.isWarnEnabled()){
            logger.warn("connection exception happened!"+e.getMessage(),e);
        }
        transportSender.close();
    }
}
