package com.alibaba.napoli.receiver.impl;

import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.connector.ConsoleConnector;
import com.alibaba.napoli.connector.DestinationContext;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.mqImpl.TransportFactory;
import com.alibaba.napoli.receiver.filter.Filter;
import com.alibaba.napoli.spi.TransportConsumer;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 12/16/11 Time: 2:29 下午
 */
public class HeartbeatSchedule implements Runnable {
    private static final Log        logger           = LogFactory.getLog(HeartbeatSchedule.class);

    private static TransportFactory transportFactory = new TransportFactory();
    private NapoliWorker            worker;
    private List<Filter>            filterList;
    private ConsoleConnector        connector;

    private final DestinationContext      destinationContext;

    public HeartbeatSchedule(DestinationContext destinationContext, ConsoleConnector connector, NapoliWorker worker,
                             List<Filter> filterList) {
        this.destinationContext = destinationContext;
        this.setFilterList(filterList);
        this.worker = worker;
        this.connector = connector;
    }

    public void setFilterList(List<Filter> filterList) {
        this.filterList = filterList;
    }

    public void run() {
        try {
            
            /*if (logger.isInfoEnabled()) {
            logger.info("run heartbeat schedule"); }*/
            

            //对于新增的machine,启动listener,放在heartbeat中是为了避免init且没有start时，避免listener的启动
            synchronized (destinationContext) {
                for (ClientMachine machine : destinationContext.getReceiveMachineList()) {
                    if (destinationContext.getConsumerMachineMap().get(machine) == null) {
                        //create consumer
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info("open consumer for new machine " + machine);
                            }

                            TransportConsumer transportConsumer = transportFactory.getTransportConsumer(connector,
                                    machine, destinationContext.getConnectionParam(), worker, filterList);

                            destinationContext.getConsumerMachineMap().put(machine, transportConsumer);
                            transportConsumer.startListen();
                        } catch (Throwable e) {
                            if (!e.getCause().toString().contains("java.io.InterruptedIOException")
                                    && !e.getMessage().equals("Interrupted.")
                                    && !e.getMessage().equals("The Consumer is closed")) {
                                logger.error("yanny e.getClass is " + e.getClass() + " getCause is " + e.getCause());
                                logger.error(e.getMessage(), e);
                            }
                        }

                    }
                }

                //执行heartbeat
                for (Map.Entry<ClientMachine, TransportConsumer> entry : destinationContext.getConsumerMachineMap().entrySet()) {
                    TransportConsumer consumer = entry.getValue();
                    consumer.heartbeat();
                }
            }
            
        } catch (Exception e) {
            if (!e.getCause().toString().contains("java.io.InterruptedIOException")
                    && !e.getMessage().equals("Interrupted.")) {
                logger.warn("yanny getCause is " + e.getCause());
                logger.warn("TransportConsumer heartbeat happen error", e);
            }
        }

    }
}
