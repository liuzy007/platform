package com.alibaba.demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.receiver.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;

import com.alibaba.napoli.client.mock.XStreamDecodeFilter;
import com.alibaba.napoli.client.util.NapoliTestUtil;

/**
 * 发送/接受消息的时候设置filter，对发送的消息进行过滤
 * @author munch.wangr
 *
 */
public class XMLConverterFilterDemo  {
    private static final Log     log        = LogFactory.getLog(XMLConverterFilterDemo.class);

    private NapoliConnector      connector;
    private DefaultAsyncSender   vtProducer, queueProducer;
    private DefaultAsyncReceiver consumer0, consumer1, consumer2;
    private List<Filter>         decodeList = new ArrayList<Filter>(
                                                    Arrays
                                                            .asList(new Filter[] { new XStreamDecodeFilter() }));

    protected void setUp() throws Exception {
        connector = new NapoliConnector();
        connector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath"));
        connector.setAddress(NapoliTestUtil.getAddress());//设置napoli服务器地址
        connector.init();
        vtProducer = new DefaultAsyncSender();     
        vtProducer.setName("zgl_test");
        vtProducer.setConnector(connector);
      
        vtProducer.init();

        queueProducer = new DefaultAsyncSender();     
        queueProducer.setName("zgl_test");
        queueProducer.setConnector(connector);
     
        queueProducer.init();

        consumer0 = new DefaultAsyncReceiver();
        consumer0.setName("zgl_test");
        consumer0.setFilterList(decodeList);
        consumer0.setConnector(connector);
        consumer0.setInstances(5);
        consumer0.init();

        consumer1 = new DefaultAsyncReceiver();
        consumer1.setName("zgl_test");
        consumer1.setFilterList(decodeList);
        consumer1.setConnector(connector);
        consumer1.setInstances(5);
        consumer1.init();

        consumer2 = new DefaultAsyncReceiver();
        consumer2.setName("zgl_test");
        consumer2.setFilterList(decodeList);
        consumer2.setConnector(connector);
        consumer2.setInstances(5);
        consumer2.init();

        log.info("tearDown finished!");
    }

    protected void tearDown() throws Exception {
        
        try {
            vtProducer.close();
        } catch (Exception e) {
            // TODO: handle exception
        }

        consumer0.close();
        consumer1.close();
        consumer2.close();

        connector.close();

        log.info("tearDown finished!");
    }

    public void testReceiverSend() throws Exception {
        DirrectShowWorker worker = new DirrectShowWorker();
        consumer0.setWorker(worker);
        consumer0.start();
        consumer1.setWorker(worker);
        consumer1.start();
        consumer2.setWorker(worker);
        consumer2.start();

        for (int i = 0; i < 10; i++) {
            vtProducer.send(new Person("庄国林vt", 33, "奥林花园23404--22"));
            log.info("庄国林vt33奥林花园23404--22");
            queueProducer.send(new Person("庄国林queue", 33, "奥林花园23404--22"));
        }

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
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
        String address = "奥林花园26#203";
    }

    class DirrectShowWorker implements AsyncWorker {

        public boolean doWork(Serializable msg) {
            log.info("receive message: " + msg);
            return true;
        }

    }

    public static void main(String[] args) throws Exception{
        XMLConverterFilterDemo xmlConverterFilterDemo = new XMLConverterFilterDemo();
        xmlConverterFilterDemo.setUp();
        xmlConverterFilterDemo.testReceiverSend();
        xmlConverterFilterDemo.tearDown();
    }
}
