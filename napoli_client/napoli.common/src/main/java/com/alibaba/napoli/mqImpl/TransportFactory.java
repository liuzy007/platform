package com.alibaba.napoli.mqImpl;

import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.util.StringUtils;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.receiver.filter.Filter;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.spi.TransportConsumer;
import com.alibaba.napoli.spi.TransportSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 12/2/11 Time: 11:24 上午
 */
public class TransportFactory {
    private static final Log                      log                  = LogFactory.getLog(TransportFactory.class);
    private static final Object                   lock                 = new Object();
    private static Map<String, TransportSender>   transportSenderMap   = new HashMap<String, TransportSender>();
    private static Map<String, TransportConsumer> transportConsumerMap = new HashMap<String, TransportConsumer>();
    static {
        ServiceLoader<TransportSender> transportSenders = ServiceLoader.load(TransportSender.class);
        for (TransportSender transportSender : transportSenders) {
            transportSenderMap.put(transportSender.getName(), transportSender);
        }

        ServiceLoader<TransportConsumer> transportConsumers = ServiceLoader.load(TransportConsumer.class);
        for (TransportConsumer transportConsumer : transportConsumers) {
            transportConsumerMap.put(transportConsumer.getName(), transportConsumer);
        }
    }

    public TransportFactory() {

    }

    public TransportSender getTrasportSender(NapoliSenderContext senderContext) throws TransportException {
        ConsoleConnector connector = senderContext.getConnector();
        ClientMachine machine = senderContext.getMachine();
        TransportSender sendTransportSender = connector.getTransportSender(machine);
        if (sendTransportSender != null) {
            return sendTransportSender;
        }
        
        synchronized (lock) {
            if (connector.getTransportSender(machine) != null){
                 return connector.getTransportSender(machine);
            }
            String type = "activemq";
            if (!StringUtils.isEmpty(machine.getMachineType())) {
                type = machine.getMachineType();
            }
            TransportSender transportSenderFactory = transportSenderMap.get(type);
            if (transportSenderFactory == null) {
                throw new TransportException("can't find the sender impl class of type[" + type + "]");
            }

            sendTransportSender = transportSenderFactory.createTransportSender(senderContext);

            connector.addTransportSender(machine, sendTransportSender);
            if (log.isInfoEnabled()) {
                log.info("create senderTransport for the machine:" + machine);
            }
            return sendTransportSender;
        }
    }

    public TransportConsumer getTransportConsumer(ConsoleConnector connector, ClientMachine machine,
                                                  ConnectionParam connectionParam, NapoliWorker worker,
                                                  List<Filter> filterList) throws TransportException {

        String type = "activemq";
        if (!StringUtils.isEmpty(machine.getMachineType())) {
            type = machine.getMachineType();
        }

        TransportConsumer transportConsumerFactory = transportConsumerMap.get(type);
        if (transportConsumerFactory == null) {
            throw new TransportException("can't find the consumer impl class of type[" + type + "]");
        }

        return transportConsumerFactory
                .createTransportConsumer(connector, machine, connectionParam, worker, filterList);
    }
}
