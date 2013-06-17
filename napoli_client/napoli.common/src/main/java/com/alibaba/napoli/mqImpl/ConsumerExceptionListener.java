package com.alibaba.napoli.mqImpl;

import com.alibaba.napoli.spi.TransportConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

/**
 * User: heyman
 * Date: 12/14/11
 * Time: 4:09 下午
 */
public class ConsumerExceptionListener implements ExceptionListener{
    private TransportConsumer transportConsumer;
    private static final Log logger = LogFactory.getLog(ConsumerExceptionListener.class);

    public ConsumerExceptionListener(TransportConsumer transportConsumer) {
        this.transportConsumer = transportConsumer;
    }

    public void onException(JMSException e) {
        if (logger.isWarnEnabled()){
            logger.warn("connection exception happened!"+e.getMessage(),e);
        }
        transportConsumer.stopListen();
        transportConsumer.startListen();
    }
}