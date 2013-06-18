/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.napoli.client.benchmark;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.IdGenerator;

/**
 * Abstract base class for some simple benchmark tools
 * 
 * @author James Strachan
 * @version $Revision: 149160 $
 */
public class BenchmarkSupport {

    protected int                     connectionCount   = 1;
    protected int                     batch             = 1000;

    protected String[]                subjects;

    private boolean                   topic             = true;
    private boolean                   durable;
    private ActiveMQConnectionFactory factory;
    private String                    url;
    protected volatile int            counter;
    private volatile int              concurrent        = 1;

    private NumberFormat              formatter         = NumberFormat.getInstance();
    private AtomicInteger             connectionCounter = new AtomicInteger(0);
    private IdGenerator               idGenerator       = new IdGenerator();
    private boolean                   timerLoop;

    private List<Object>              resources         = Collections
                                                                .synchronizedList(new ArrayList<Object>());

    public BenchmarkSupport() {
    }

    public void start() {
        System.out.println("Using: " + connectionCount + " connection(s)");
        this.factory = createFactory();

        subjects = new String[connectionCount];
        for (int i = 0; i < connectionCount; i++) {
            subjects[i] = "BENCHMARK.FEED" + i;
        }
        if (useTimerLoop()) {
            Thread timer = new Thread() {
                public void run() {
                    timerLoop();
                }
            };
            timer.setDaemon(false);
            timer.start();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isTopic() {
        return topic;
    }

    public void setTopic(boolean topic) {
        this.topic = topic;
    }

    public ActiveMQConnectionFactory getFactory() {
        return factory;
    }

    public void setFactory(ActiveMQConnectionFactory factory) {
        this.factory = factory;
    }

    public void setSubject(String subject) {
        connectionCount = 1;
        subjects = new String[] { subject };
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    protected Session createSession() throws JMSException {

        Connection connection = getFactory().createConnection("admin", "admin");
        if (durable) {
            connection.setClientID(idGenerator.generateId());
        }
        connection.start();
        int value = connectionCounter.incrementAndGet();
        addResource(connection);
        System.out.println("Created connection: " + value + " = " + connection);
        // connection.createSession(arg0, arg1)
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //session.
        //Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // session.

        addResource(session);
        // session.close();

        return session;
    }

    protected ActiveMQConnectionFactory createFactory() {

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(
                getUrl());

        return activeMQConnectionFactory;
    }

    protected synchronized void count(int count) {
        counter += count;
        /*
         * if (counter > batch) { counter = 0; long current =
         * System.currentTimeMillis(); double end = current - time; end /= 1000;
         * time = current; System.out.println("Processed " + batch + " messages
         * in " + end + " (secs)"); }
         */
    }

    protected synchronized int resetCount() {
        int answer = counter;
        counter = 0;
        return answer;
    }

    protected void timerLoop() {
        int times = 0;
        int total = 0;
        // int dumpVmStatsFrequency = 10;
        // Runtime runtime = Runtime.getRuntime();
        double average_before_30 = 0;

        long start = System.currentTimeMillis();
        int tatal_before_30 = 0;
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int processed = resetCount();
            double average = 0;
            if (processed >= 0) {
                total += processed;
                times++;
            }
            // if (times > 0) {
            // average = total / (double) times;
            // }

            if (times % 30 == 0) {
                average_before_30 = (total - tatal_before_30) / 30.00;
                tatal_before_30 = total;
            }

            long end = System.currentTimeMillis();

            if (end > start) {

                average = total / ((end - start) / 1000.00);
            }
            System.out
                    .println((new Date()) + "-" + getClass().getSimpleName() + " Processed: "
                            + processed + " messages this second. Average: " + average
                            + "/s,Average previous 30 seconds :" + average_before_30 + "/s total: "
                            + total);
            if (times > 60 && average_before_30 == 0) {
                System.out.println("..........closing .........");
                try {
                    for (Object object : resources) {
                        if (object instanceof Session) {
                            Session session = (Session) object;

                            // ActiveMQSession
                            // activeMQSession= (ActiveMQSession)session;
                            // activeMQSession.

                            session.close();

                        }
                    }
                    for (Object object : resources) {
                        if (object instanceof Connection) {
                            Connection connection = (Connection) object;
                            connection.stop();
                            connection.close();

                        }
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }

                System.exit(1);
            }

            // if ((times % dumpVmStatsFrequency) == 0 && times != 0) {
            // System.out
            // .println("Used memory: "
            // + asMemoryString(runtime.totalMemory()
            // - runtime.freeMemory())
            // + " Free memory: "
            // + asMemoryString(runtime.freeMemory())
            // + " Total memory: "
            // + asMemoryString(runtime.totalMemory())
            // + " Max memory: "
            // + asMemoryString(runtime.maxMemory()));
            // }

        }
    }

    protected String asMemoryString(long value) {
        return formatter.format(value / 1024) + " K";
    }

    protected boolean useTimerLoop() {
        return timerLoop;
    }

    protected Destination createDestination(Session session, String subject) throws JMSException {

        if (topic) {
            return session.createTopic(subject);
        } else {
            return session.createQueue(subject);
        }
    }

    public int getCounter() {
        return counter;
    }

    public void setTimerLoop(boolean timerLoop) {
        this.timerLoop = timerLoop;
    }

    protected static boolean parseBoolean(String text) {
        return text.equalsIgnoreCase("true");
    }

    public int getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(int concurrent) {
        this.concurrent = concurrent;
    }

    protected void addResource(Object resource) {
        resources.add(resource);
    }
}
