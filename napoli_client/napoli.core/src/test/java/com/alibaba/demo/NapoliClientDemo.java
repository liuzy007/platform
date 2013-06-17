/**
 * Project: napoli.client
 * 
 * File Created at 2009-6-15
 * $Id: NapoliClientDemo.java 154477 2012-03-13 03:57:55Z haihua.chenhh $ 
 * 
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.demo;

import java.io.Serializable;

import com.alibaba.napoli.receiver.Receiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import com.alibaba.napoli.client.async.AsyncSender;
import com.alibaba.napoli.client.async.AsyncWorker;

/**
 * 在这个Demo中，给出Napoli Client的使用方法。
 * 
 * @author guolin.zhuanggl
 */
public class NapoliClientDemo {
    private static final Log log               = LogFactory.getLog(NapoliClientDemo.class);
    private static final int DEFAULT_MSG_COUNT = 10;

    BeanFactory              beanFactory;

    //AsyncSender              vSender;
    AsyncSender              qSender;
   //AsyncSender              vFilterSender;

    Receiver            receiver0;
    //AsyncReceiver            receiver1;
    //AsyncReceiver            receiver2;

    public void init() {
        beanFactory = new XmlBeanFactory(new ClassPathResource("mq.xml"));

        //vSender = (AsyncSender) beanFactory.getBean("vSender");

        qSender = (AsyncSender) beanFactory.getBean("qSender");
        //vFilterSender = (AsyncSender) beanFactory.getBean("vFilterSender");

        receiver0 = (Receiver) beanFactory.getBean("receiver0");
        //receiver1 = (AsyncReceiver) beanFactory.getBean("receiver1");
        //receiver2 = (AsyncReceiver) beanFactory.getBean("receiver2");

        log.info("init finished.");
    }

    public static void main(String[] args) throws Exception {
        log.info("demo start.");
        NapoliClientDemo demo = new NapoliClientDemo();
        demo.init();

        demo.receiver0.setWorker(new ShowMessageWorker("receiver0"));
        //demo.receiver1.setWorker(new ShowMessageWorker("receiver1"));
        //demo.receiver2.setWorker(new ShowMessageWorker("receiver2"));

        demo.receiver0.start();
        //demo.receiver1.start();
        //demo.receiver2.start();
        log.info("receiver started.");

        log.info("start send message");
        //topic路由queue
        //demo.vFilterSender.send("a");
        
        for (int i = 0; i < DEFAULT_MSG_COUNT; i++) {
            try {
                // 发一条消息给Topic
               // demo.vSender.send(new Person("庄国林vt", 33, "奥林花园23404--22"));
                //log.info("Send to Topic: 庄国林vt33奥林花园23404--22");
                // 发一条消息给Queue
                demo.qSender.send(new Person("庄国林queue", 33, "奥林花园23404--22"));
                log.info("Send to Queue: 庄国林queue33奥林花园23404--22");

                Thread.sleep(300);
            } catch (Exception e) {
                log.error("Send Error: " + e);
            }
        }
    }

    public static class ShowMessageWorker implements AsyncWorker {
        private Log    log = LogFactory.getLog(ShowMessageWorker.class);

        private String destinationName;

        public ShowMessageWorker(String destinationName) {
            this.destinationName = destinationName;
        }

        public boolean doWork(Serializable msg) {
            //log.info("Receive a message From " + destinationName + ":");
            //log.info(msg);
        	System.out.println("Receive a message From " + destinationName + ":");
        	System.out.println(msg);
            return true;
        }
    }

    public static class Person implements Serializable {
        private static final long serialVersionUID = 1L;

        public Person(String name, int age, String address) {
            super();
            this.name = name;
            this.age = age;
            this.address = address;
        }

        String name    = "庄国林";
        int    age     = 35;
        String address = "奥林皮卡花园1206#203";
    }
}
