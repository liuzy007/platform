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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

/**
 * @author James Strachan
 * @version $Revision: 149160 $
 */
public class Consumer extends BenchmarkSupport {

    private long waitTime = -1;

    public long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    public Consumer() {
    }

    public static void main(String[] args) {
        Consumer tool = new Consumer();
        if (args.length > 0) {
            tool.setUrl(args[0]);
        }
        if (args.length > 1) {
            tool.setTopic(parseBoolean(args[1]));
        }

        if (args.length > 2) {
            tool.setDurable(parseBoolean(args[2]));
        }
        if (args.length > 3) {
            tool.setConnectionCount(Integer.parseInt(args[3]));
        }

        if (args.length > 4) {
            tool.setConcurrent(Integer.parseInt(args[4]));
        }
        if (args.length > 5) {
            tool.setWaitTime(Long.parseLong(args[5]));
        }

        try {
            tool.setTimerLoop(true);
            tool.run();
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

    public void run() throws JMSException {
        start();
        subscribe();
    }

    protected void subscribe() throws JMSException {
        for (int i = 0; i < subjects.length; i++) {
            for (int j = 0; j < getConcurrent(); j++) {
                subscribe(subjects[i]);
            }
        }
    }

    protected void subscribe(String subject) throws JMSException {
        final Session session = createSession();
        // ActiveMQSession

        Destination destination = createDestination(session, subject);

        System.out.println("Consuming on : " + destination + " of type: "
                + destination.getClass().getName());

        MessageConsumer consumer = null;
        if (isDurable() && isTopic()) {
            consumer = session.createDurableSubscriber((Topic) destination, getClass().getName());
        } else {
            consumer = session.createConsumer(destination);
        }

        consumer.setMessageListener(new MessageListener() {

            public void onMessage(Message message) {
                try {
                    TextMessage textMessage = (TextMessage) message;

                    // lets force the content to be deserialized
                    textMessage.getText();

                    if (waitTime > 0) {
                        Thread.sleep(waitTime);
                    }
                    if (session.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE) {
                        message.acknowledge();
                    }
                    if (session.getTransacted()) {
                        session.commit();
                    }
                    //message.
                    count(1);
                    // consumer.close();
                    // lets count the messages
                    // message.acknowledge();
                } catch (JMSException e) {

                    e.printStackTrace();
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }
        });

    }

}
