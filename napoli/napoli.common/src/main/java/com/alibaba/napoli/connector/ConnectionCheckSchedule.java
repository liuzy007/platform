package com.alibaba.napoli.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 12/5/11 Time: 9:46 下午
 */
public class ConnectionCheckSchedule implements Runnable {
    private static final Log       log = LogFactory.getLog(ConnectionCheckSchedule.class);
    private final ConsoleConnector consoleConnector;

    public ConnectionCheckSchedule(ConsoleConnector consoleConnector) {
        this.consoleConnector = consoleConnector;
    }

    public void run() {
        try {
            consoleConnector.removeIdleSenderConnection();
            consoleConnector.removeIdleReceiverConnection();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }
}
