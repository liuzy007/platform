package com.alibaba.napoli.common.util;

import com.alibaba.napoli.domain.client.ClientDestination;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.domain.client.ClientTopic;
import com.alibaba.napoli.domain.client.ClientVirtualTopic;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 9/26/11 Time: 9:53 上午
 */
public class JmxUtil {

    private static final Log logger     = LogFactory.getLog("JmxUtil");

    public static boolean    logEnabled = false;

    private static String getJMSObjectNameFormat(String machineType, String type) {
        String format = "";
        if (isBlank(machineType) || machineType.toUpperCase().equals("ACTIVEMQ")) {
            format = "org.apache.activemq:BrokerName=localhost,Type=Queue,Destination=%s";

        } else if (machineType.toUpperCase().equals("HORNETQ")) {
            format = "org.hornetq:module=Core,type=Queue,address=\"%1$s\",name=\"%1$s\"";
        }

        return format;
    }

    private static String getAttributeName(String machineType, String attribute) {
        String name = "";
        if (attribute.equals("QueueSize")) {
            if (isBlank(machineType) || machineType.toUpperCase().equals("ACTIVEMQ")) {
                name = "QueueSize";
            } else if (machineType.toUpperCase().equals("HORNETQ")) {
                name = "MessageCount";
            }
        } else if (attribute.equals("EnqueueCount")) {
            if (isBlank(machineType) || machineType.toUpperCase().equals("ACTIVEMQ")) {
                name = "EnqueueCount";
            } else if (machineType.toUpperCase().equals("HORNETQ")) {
                name = "MessagesAdded";
            }
        }

        return name;
    }

    public static JMXConnector connectWithTimeout(final JMXServiceURL url, long timeout, TimeUnit unit)
            throws IOException {
        final BlockingQueue<Object> mailbox = new ArrayBlockingQueue<Object>(1);
        ExecutorService executor = Executors.newSingleThreadExecutor(daemonThreadFactory);
        executor.submit(new Runnable() {
            public void run() {
                try {
                    JMXConnector connector = JMXConnectorFactory.connect(url);
                    if (!mailbox.offer(connector))
                        connector.close();
                } catch (Throwable t) {
                    mailbox.offer(t);
                }
            }
        });
        Object result;
        try {
            result = mailbox.poll(timeout, unit);
            if (result == null) {
                if (!mailbox.offer(""))
                    result = mailbox.take();
            }
        } catch (InterruptedException e) {
            throw initCause(new InterruptedIOException(e.getMessage()), e);
        } finally {
            executor.shutdown();
        }
        if (result == null)
            throw new SocketTimeoutException("Connect timed out: " + url);
        if (result instanceof JMXConnector)
            return (JMXConnector) result;
        try {
            throw (Throwable) result;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            // In principle this can't happen but we wrap it anyway
            throw new IOException(e.toString(), e);
        }
    }

    private static <T extends Throwable> T initCause(T wrapper, Throwable wrapped) {
        wrapper.initCause(wrapped);
        return wrapper;
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    }

    private static final ThreadFactory daemonThreadFactory = new DaemonThreadFactory();

    // JMX operations.

    public static int getConsumerSessionCount(String consoleAddress, String queueName) {
        ConfigServiceHttpImpl configServiceHttp = new ConfigServiceHttpImpl(consoleAddress);
        ClientDestination clientDestination = configServiceHttp.fetchDestination(queueName);
        int count = 0;
        if (clientDestination instanceof ClientQueue) {
            for (ClientMachine machine : ((ClientQueue) clientDestination).getMachineSet()) {
                JMXConnector connector = null;
                try {
                    connector = connectWithTimeout(new JMXServiceURL(JmxUtil.getJmxAddress(machine)), 2,
                            TimeUnit.SECONDS);
                    Set<ObjectName> names = null;
                    if (machine.getMachineType().toUpperCase().equals("ACTIVEMQ")) {
                        String format = "org.apache.activemq:BrokerName=localhost,Type=Subscription,destinationType=Queue,destinationName=%s,*";
                        ObjectName objName = new ObjectName(String.format(format, queueName));
                        names = connector.getMBeanServerConnection().queryNames(objName, null);
                        count += names.size();
                        if (logEnabled) {
                            logger.info(" queue " + queueName + " at " + connector + " has consumer session "
                                    + names.size());
                        }

                    } else if (machine.getMachineType().toUpperCase().equals("HORNETQ")) {
                        ObjectName objectName = new ObjectName(String.format(
                                getJMSObjectNameFormat(machine.getMachineType(), "Queue"), queueName));
                        count += (Integer) (connector.getMBeanServerConnection().getAttribute(objectName,
                                "ConsumerCount"));
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if (connector != null) {
                        try {
                            connector.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
            return count;
        }
        return count;
    }

    /*
     * public static int getConsumerSessionCount(String consoleAddress,
     * List<JMXConnector> jmxConnectors, String queueName) {
     * ConfigServiceHttpImpl config =
     * NapoliMessageUtil.getConfigService(consoleAddress); return
     * getConsumerSessionCount(config, jmxConnectors, queueName); } public
     * static int getConsumerSessionCount(ConfigServiceHttpImpl config,
     * List<JMXConnector> jmxConnectors, String queueName) { String[]
     * machineTypes = getMachineType(config, queueName); return
     * getConsumerSessionCount(jmxConnectors, queueName, machineTypes); } public
     * static int getConsumerSessionCount(List<JMXConnector> jmxConnectors,
     * String queueName, String[] machineTypes) { int count = 0; if
     * (machineTypes.length < jmxConnectors.size()) {
     * logger.warn("!!!!!!!!!!!!!machineTypes size " + machineTypes.length +
     * " less than jmxConnector size " + jmxConnectors.size() + "!!!!!!!!!!!!");
     * } List<String> ids = new ArrayList<String>(); for (int i = 0; i <
     * jmxConnectors.size(); i++) { JMXConnector connector =
     * jmxConnectors.get(i); Set<ObjectName> names = null; try { if
     * (isBlank(machineTypes[i]) ||
     * machineTypes[i].toUpperCase().equals("ACTIVEMQ")) { String format =
     * "org.apache.activemq:BrokerName=localhost,Type=Subscription,destinationType=Queue,destinationName=%s,*"
     * ; ObjectName objName = new ObjectName(String.format(format, queueName));
     * names = connector.getMBeanServerConnection().queryNames(objName, null);
     * count += names.size(); if (logEnabled) { logger.info(" queue " +
     * queueName + " at " + connector + " has consumer session " +
     * names.size()); } } else if
     * (machineTypes[i].toUpperCase().equals("HORNETQ")) { ObjectName objectName
     * = new ObjectName(String.format( getJMSObjectNameFormat(machineTypes[i],
     * "Queue"), queueName)); count = (Integer)
     * (connector.getMBeanServerConnection().getAttribute(objectName,
     * "ConsumerCount")); } } catch (Exception e) { logger.error(e.getMessage(),
     * e); } } return count; }
     */

    private static long getSingleQueueSize(String jmxAddress, String queueName, String machineType) {
        long queueSize = 0;
        JMXConnector connector = null;
        try {
            connector = connectWithTimeout(new JMXServiceURL(jmxAddress), 5, TimeUnit.SECONDS);
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            ObjectName objectName = new ObjectName(String.format(getJMSObjectNameFormat(machineType, "Queue"),
                    queueName));
            queueSize = (Long) connection.getAttribute(objectName, getAttributeName(machineType, "QueueSize"));
            if (logEnabled) {
                logger.info("queue size for " + queueName + " at " + jmxAddress + " is " + queueSize);
            }
        } catch (Exception e) {
            if (logEnabled) {
                logger.info(
                        "Warn! get queue size for " + queueName + " at " + jmxAddress + " get exception:"
                                + e.getMessage(), e);
            }
            // queueSize = -1;
        } finally {
            try {
                if (connector != null) {
                    connector.close();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
            }
        }

        return queueSize;
    }

    private static long getSingleEnQueue(String jmxAddress, String queueName, String machineType) {
        long enqueueCount = 0;
        JMXConnector connector = null;
        try {
            if (logEnabled) {
                logger.info("getSingleEnQueue at " + jmxAddress + " queue:" + queueName + " for machineType "
                        + machineType);
            }

            connector = JMXConnectorFactory.connect(new JMXServiceURL(jmxAddress));
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            ObjectName objectName = new ObjectName(String.format(getJMSObjectNameFormat(machineType, "Queue"),
                    queueName));
            enqueueCount = (Long) connection.getAttribute(objectName, getAttributeName(machineType, "EnqueueCount"));
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        } catch (MalformedObjectNameException e) {
            logger.error(e.getMessage(),e);
        } catch (MBeanException e) {
            logger.error(e.getMessage(),e);
        } catch (AttributeNotFoundException e) {
            logger.error(e.getMessage(),e);
        } catch (InstanceNotFoundException e) {
            logger.error(e.getMessage(),e);
        } catch (ReflectionException e) {
            logger.error(e.getMessage(),e);
        } finally {
            try {
                if (connector != null) {
                    connector.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return enqueueCount;
    }

    private static long getQueueSize(ClientQueue queue) {
        long queueSize = 0;
        for (ClientMachine machine : queue.getMachineSet()) {
            queueSize += getSingleQueueSize(JmxUtil.getJmxAddress(machine), queue.getName(), machine.getMachineType());
        }
        return queueSize;
    }

    /*
     * 轮询直到队列大小为指定数值，不hard code为0，是因为很多时候机器累计为负，为0的时候不足以证明清空了，精度为50毫秒级
     */
    private static long waitTillQueueSizeNotLargerThanTarget(ClientQueue clientQueue, long queueSize, long timeOut) {

        long size = 0;
        long time = 0;
        do {
            try {

                Thread.sleep(500);
                time += 500;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                logger.error(e.getMessage(),e);
            }
            size = getQueueSize(clientQueue);
            if (logEnabled) {
                logger.info("queue size is " + size);
            }
        } while (size == -1 || size > queueSize && (timeOut == -1 || timeOut > 0 && time > timeOut));

        return size;
    }

    // operations through consoleAddress.
    /*
     * public static List<JMXConnector> getJMXConnectors(String consoleAddress,
     * String queueName) { ConfigServiceHttpImpl config =
     * NapoliMessageUtil.getConfigService(consoleAddress); return
     * getJMXConnectors(config, queueName); }
     */

    public static void deleteAllMessage(String consoleAddress, String destinationName) {
        ConfigServiceHttpImpl config = NapoliMessageUtil.getConfigService(consoleAddress);

        deleteAllMessage(config, destinationName);
    }

    public static long getQueueSize(String consoleAddress, String queueName) {
        ConfigServiceHttpImpl config = NapoliMessageUtil.getConfigService(consoleAddress);
        return getQueueSize(config, queueName);
    }

    public static long getEnQueue(String consoleAddress, String queueName) {
        ConfigServiceHttpImpl config = NapoliMessageUtil.getConfigService(consoleAddress);
        return getEnQueue(config, queueName);
    }

    public static void waitTillQueueSizeZero(String consoleAddress, String destinationName) {
        waitTillQueueSizeAsTarget(consoleAddress, destinationName, 0);
    }

    public static void waitTillQueueSizeAsTarget(String consoleAddress, String destinationName, long queueSize) {
        waitTillQueueSizeAsTarget(consoleAddress, destinationName, queueSize, -1);
    }

    public static void waitTillQueueSizeAsTarget(String consoleAddress, String destinationName, long queueSize,
                                                 int timeOut) {

        ConfigServiceHttpImpl config = NapoliMessageUtil.getConfigService(consoleAddress);

        waitTillQueueSizeAsTarget(config, destinationName, queueSize, timeOut);
    }

    // operation through configService
    /*
     * public static List<JMXConnector> getJMXConnectors(ConfigServiceHttpImpl
     * config, String queueName) { List<JMXConnector> connectors = new
     * ArrayList<JMXConnector>(); //String[] jmxAddresses =
     * getJmxAddress(config, queueName);
     * logger.info("!!!!!!!!!!!yanny jmxAddress for queue " + queueName + " is "
     * + jmxAddresses.length); for (final String address : jmxAddresses) {
     * JMXConnector connector = null; try { connector = connectWithTimeout(new
     * JMXServiceURL(address), 2, TimeUnit.SECONDS); connectors.add(connector);
     * } catch (Exception e) { try { if (connector != null) { connector.close();
     * } } catch (Exception ex) { } } } return connectors; }
     */
    public static void deleteAllMessage(ConfigServiceHttpImpl config, String destination) {
        ClientDestination clientDestination = config.fetchDestination(destination);
        if (clientDestination instanceof ClientQueue) {
            deleteQueueMessage((ClientQueue) clientDestination);
        } else if (clientDestination instanceof ClientVirtualTopic) {
            ClientVirtualTopic clientVirtualTopic = (ClientVirtualTopic) clientDestination;
            for (ClientQueue clientQueue : clientVirtualTopic.getClientQueueList()) {
                deleteQueueMessage(clientQueue);
            }
        }
    }

    private static void deleteQueueMessage(ClientQueue clientQueue) {
        for (ClientMachine machine : clientQueue.getMachineSet()) {
            JMXConnector connector = null;
            try {
                String machineType = machine.getMachineType();
                connector = connectWithTimeout(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + machine.getIp()
                        + ":" + machine.getJmxPort() + "/jmxrmi"), 2, TimeUnit.SECONDS);

                ObjectName objName = new ObjectName(String.format(getJMSObjectNameFormat(machineType, "Queue"),
                        clientQueue.getName()));

                Object params[] = {};
                String signature[] = {};

                if (isBlank(machineType) || machineType.toUpperCase().equals("ACTIVEMQ")) {
                    connector.getMBeanServerConnection().invoke(objName, "purge", params, signature);
                } else if (machineType.toUpperCase().equals("HORNETQ")) {
                    connector.getMBeanServerConnection().invoke(objName, "removeMessages", new Object[] { "" },
                            new String[] { "java.lang.String" });
                }
            } catch (Exception e) {
                logger.error("Error!", e);
            } finally {
                if (connector != null) {
                    try {
                        connector.close();
                    } catch (IOException e) {
                        logger.error(e.getMessage(),e);
                    }
                }
            }
        }
    }

    public static void waitTillQueueSizeAsTarget(ConfigServiceHttpImpl config, String queueName, long queueSize) {
        waitTillQueueSizeAsTarget(config, queueName, queueSize, -1);
    }

    public static void waitTillQueueSizeAsTarget(ConfigServiceHttpImpl config, String destinationName, long queueSize,
                                                 int timeOut) {
        logger.info("wait till queueSize=" + queueSize + " for destination " + destinationName);

        /*
         * //String[] jmxAddress = getJmxAddress(config, queueName);
         * ArrayList<String> queues = NapoliMessageUtil.getQueueNamess(config,
         * queueName); String[] machineTypes = getMachineType(config,
         * queueName);
         */

        ClientDestination destination = config.fetchDestination(destinationName);
        long size = 0;
        if (destination instanceof ClientQueue) {
            ClientQueue clientQueue = (ClientQueue) destination;
            size = waitTillQueueSizeNotLargerThanTarget(clientQueue, queueSize, timeOut);
        } else if (destination instanceof ClientVirtualTopic) {
            ClientVirtualTopic clientVirtualTopic = (ClientVirtualTopic) destination;
            for (ClientQueue clientQueue : clientVirtualTopic.getClientQueueList()) {
                waitTillQueueSizeNotLargerThanTarget(clientQueue, queueSize, timeOut);
            }
        } else {
            throw new RuntimeException("destination type " + destination + " is not supported.");
        }

        logger.info("queue size is <= " + size + " for queue " + destinationName + " with timeout set to " + timeOut);
    }

    public static long getQueueSize(ConfigServiceHttpImpl config, String queueName) {
        ClientQueue queue = (ClientQueue) config.fetchDestination(queueName);
        long totalQueueSize = 0;
        for (ClientMachine machine : queue.getMachineSet()) {
            totalQueueSize += getSingleQueueSize(
                    "service:jmx:rmi:///jndi/rmi://" + machine.getIp() + ":" + machine.getJmxPort() + "/jmxrmi",
                    queueName, machine.getMachineType());
        }
        return totalQueueSize;
    }
    
    public static long getDLQQueueSize(String address,String queueName){
        ConfigServiceHttpImpl config = NapoliMessageUtil.getConfigService(address);
        ClientQueue queue = (ClientQueue) config.fetchDestination(queueName);
        long totalQueueSize = 0;
        for (ClientMachine machine : queue.getMachineSet()) {
            totalQueueSize += getSingleQueueSize(
                    "service:jmx:rmi:///jndi/rmi://" + machine.getIp() + ":" + machine.getJmxPort() + "/jmxrmi",
                    "DLQ."+queueName, machine.getMachineType());
        }
        return totalQueueSize;
    }

    public static long waitTillMetaqTopicSizeAsTarget(String consoleAddress, String topicName, String consumerGroup,
                                                      long targetSize, int timeOut) {
        ConfigServiceHttpImpl config = NapoliMessageUtil.getConfigService(consoleAddress);

        long size = 0;
        long time = 0;
        ClientTopic topic = (ClientTopic) config.fetchDestination(topicName);

        do {
            try {

                Thread.sleep(500);
                time += 500;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                logger.error(e.getMessage(),e);
            }

            size = 0;
            for (ClientMachine machine : topic.getMachineSet()) {
               
                size += getMetaqTopicSize(
                        "service:jmx:rmi:///jndi/rmi://" + machine.getIp() + ":" + machine.getJmxPort() + "/jmxrmi",
                        topicName, consumerGroup);
            }

            if (logEnabled) {
                logger.info("yanny clientTopic size for " + consumerGroup + " is " + size);
            }
        } while (size == -1 || size > targetSize && (timeOut == -1 || timeOut > 0 && time > timeOut));

        return size;
    }

    public static long getMetaqTopicSize(String jmxAddress, String topicName, String consumerGroup) {
        long totalQueueSize = 0;
        JMXConnector connector = null;
        try {
            connector = connectWithTimeout(new JMXServiceURL(jmxAddress), 5, TimeUnit.SECONDS);

            ObjectName objName = new ObjectName("MetaqServer:type=monitor");
            Object params[] = { topicName };
            String signature[] = { "java.lang.String" };
            List<String[]> result = new ArrayList<String[]>();
            List<CompositeData> tmp = (List<CompositeData>) connector.getMBeanServerConnection().invoke(objName,
                    "getUnConsumedOffsetInfos", params, signature);
            if (tmp != null && tmp.size() >0 ) {
              
                int match = 0;
                for (CompositeData data : tmp) {                    
                  
                    String group = data.get("group").toString();
                    String size = data.get("mc").toString();
                    if (size.equals("Unknown")) {
                        logger.info("yanny size is unknown, set to 1000");
                        size = "1000";
                    }
                    if (logEnabled) {
                        logger.info(" yanny group " + group + " partition:" + data.get("partition").toString()
                                + " size is " + size);
                    }

                    if (group.equals(consumerGroup)) {

                        match++;
                        totalQueueSize += Long.parseLong(size);
                    }
                }
               
                if(match==0){
                    //metaq jmx seems not stable, sometimes doesn't contain the consumer group.
                    //risk is if the user input the wrong consumer group, the parent method won't return.
                    //but this should be found during debug phase.
                    totalQueueSize = 777;
                }
                
            } else {
                logger.error("yanny get jmx params return null or mepty array");
                totalQueueSize = 333;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            totalQueueSize = 444;
        } finally {
            try {
                if (connector != null) {
                    connector.close();
                }
            } catch (IOException e) {
                logger.error("error when close JMXConnector: " + e);
            }
        }

        return totalQueueSize;
    }

    public static long getEnQueue(ConfigServiceHttpImpl config, String queueName) {
        ClientQueue queue = (ClientQueue) config.fetchDestination(queueName);
        long totalEnQueue = 0;
        for (ClientMachine machine : queue.getMachineSet()) {
            totalEnQueue += getSingleEnQueue(
                    "service:jmx:rmi:///jndi/rmi://" + machine.getIp() + ":" + machine.getJmxPort() + "/jmxrmi",
                    queueName, machine.getMachineType());
        }
        return totalEnQueue;
    }

    /*
     * public static void waitTillConsumerSessionCountZero(List<JMXConnector>
     * jmxConnectors, String consoleAddress, String queueName) {
     * ConfigServiceHttpImpl config =
     * NapoliMessageUtil.getConfigService(consoleAddress);
     * waitTillConsumerSessionCountZero(jmxConnectors, config, queueName); }
     */

    /*
     * public static void waitTillConsumerSessionCountZero(List<JMXConnector>
     * jmxConnectors, ConfigServiceHttpImpl config, String queueName) { String[]
     * machineTypes = getMachineType(config, queueName);
     * waitTillConsumerSessionCountZero(jmxConnectors, queueName, machineTypes);
     * }
     */

    public static void waitTillConsumerSessionCountZero(String consoleAddress, String queueName) {
        long sessionCount = 0;

        if (logEnabled) {
            logger.info("wait ConsumerSessionCount zero for queue " + queueName);
        }

        do {
            try {

                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                logger.error(e.getMessage(),e);
            }
            sessionCount = getConsumerSessionCount(consoleAddress, queueName);
            if (logEnabled) {
                logger.info("SessionCount for queueName " + queueName + " is " + sessionCount);
            }
        } while (sessionCount > 0);
        if (logEnabled) {
            logger.info("finish wait ConsumerSessionCount zero for queue " + queueName);
        }
    }

    public static boolean isBlank(String str) {
        if (str == null || str.equals("")) {
            return true;
        } else {
            return false;
        }

    }

    public static void shutdownMachine(String jmxAddress, String type) {
        try {
            JMXServiceURL jmxServiceURL = new JMXServiceURL(jmxAddress);
            ObjectName objName;
            if ("activemq".equals(type)) {
                objName = new ObjectName(
                        "org.apache.activemq:BrokerName=localhost,Type=Connector,ConnectorName=openwire");
            } else if ("hornetQ".equals(type)) {
                objName = new ObjectName("org.hornetq:module=Core,type=Acceptor,name=\"netty-throughput\"");
            } else {
                throw new RuntimeException("type " + type + " is not support!");
            }
            JMXHelper.executeMethod(jmxServiceURL, objName, "stop", null, null);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(),e);
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (MalformedObjectNameException e) {
            logger.error(e.getMessage(),e);
        }
    }

    public static void startMachine(String jmxAddress, String type) {
        try {
            JMXServiceURL jmxServiceURL = new JMXServiceURL(jmxAddress);
            ObjectName objName;
            if ("activemq".equals(type)) {
                objName = new ObjectName(
                        "org.apache.activemq:BrokerName=localhost,Type=Connector,ConnectorName=openwire");
            } else if ("hornetQ".equals(type)) {
                objName = new ObjectName("org.hornetq:module=Core,type=Acceptor,name=\"netty-throughput\"");
            } else {
                throw new RuntimeException("type " + type + " is not support!");
            }
            JMXHelper.executeMethod(jmxServiceURL, objName, "start", null, null);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(),e);
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (MalformedObjectNameException e) {
            logger.error(e.getMessage(),e);
        }
    }

    public static String getJmxAddress(ClientMachine machine) {
        return "service:jmx:rmi:///jndi/rmi://" + machine.getIp() + ":" + machine.getJmxPort() + "/jmxrmi";
    }

    public static void main(String[] args) throws Exception {
        /*
         * System.out.println(getSingleQueueSize("10.20.144.81:1099",
         * "DefaultAsyncReceiverTest", ""));
         * System.out.println(getSingleQueueSize("10.20.144.81:1099",
         * "DefaultAsyncReceiverTest", null));
         * System.out.println(getSingleEnQueue("localhost:1099", "queuetest0",
         * "ACTIVEMQ"));
         */
        // shutdownMachine("10.33.145.22:1099","activemq");
        // startMachine("10.33.145.22:1099","activemq");
    }

}
