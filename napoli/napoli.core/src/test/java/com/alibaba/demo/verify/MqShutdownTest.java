package com.alibaba.demo.verify;

import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.util.ConfigServiceHttpImpl;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.common.util.JmxUtil;
import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.receiver.impl.DefaultReceiverImpl;
import com.alibaba.napoli.sender.Sender;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: heyman Date: 3/15/12 Time: 1:54 下午
 */
public class MqShutdownTest {
    @Test
    public void testMqShutdown() throws Exception {
        final String queueName = "test_MqShutdownTest_";
        //final String queueName = "test_MqShutdownTest_" + System.currentTimeMillis();
        System.out.println("quename=" + queueName);
        final AtomicInteger receiveCount = new AtomicInteger(0);
        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName,
                NapoliTestUtil.getProperty("napoli.func.mixGroupName"));
        NapoliConnector connector = new NapoliConnector();
        connector.setAddress(NapoliTestUtil.getAddress());
        connector.setPrefetch(10);
        connector.setConnectionCheckPeriod(1000);
        connector.init();

//        String[] jmxAddresses = JmxUtil.getJmxAddress(connector.getAddress(), queueName);
//        String[] machineTypes = JmxUtil.getMachineType(connector.getConfigService(), queueName);
        ClientQueue clientQueue = (ClientQueue)(new ConfigServiceHttpImpl(connector.getAddress())).fetchDestination(queueName);
        
        for (ClientMachine machine : clientQueue.getMachineSet()) {
            JmxUtil.startMachine(JmxUtil.getJmxAddress(machine), machine.getMachineType());
        }

        DefaultAsyncSender sender = DefaultAsyncSender.createSender(connector,queueName, false);
        sender.init();

        DefaultReceiverImpl defaultReceiver = new DefaultReceiverImpl();
        defaultReceiver.setConnector(connector);
        defaultReceiver.setName(queueName);
        defaultReceiver.setStoreEnable(false);
        defaultReceiver.setInstances(5);
        defaultReceiver.setWorkerPri(new AsyncWorker() {
            public boolean doWork(Serializable message) {
                receiveCount.incrementAndGet();
                return true;
            }
        });
        NapoliConstant.MIN_HEARTBEAT_PERIOD = 10;
        defaultReceiver.setPeriod(1000);
        defaultReceiver.init();
        defaultReceiver.start();

        for (int i = 0; i < 100; i++) {
            sender.sendMessage("hello world");
        }

        for (ClientMachine machine : clientQueue.getMachineSet()) {
            JmxUtil.shutdownMachine(JmxUtil.getJmxAddress(machine), machine.getMachineType());
        }

        /*assertEquals(0, connector.getSenderConnectionSize());
       assertEquals(0, connector.getConsumerConnectionSize());*/

        for (ClientMachine machine : clientQueue.getMachineSet()) {
            JmxUtil.startMachine(JmxUtil.getJmxAddress(machine), machine.getMachineType());
        }

        
        
        Thread.sleep(1000);
        for (int i = 0; i < 100; i++) {
            sender.sendMessage("hello world");
        }

        Thread.sleep(1000 * 60 * 3);
        //int loop = 100;
        System.out.println("consumer connections :" + connector.getConsumerConnectionSize());
        /*while (receiveCount.get() != 200 && loop != 0){
            Thread.sleep(100);
            loop--;
        }*/
        Thread.sleep(1000 * 10);

        assertEquals(200, receiveCount.get());
        defaultReceiver.stop();
        sender.close();
        NapoliConstant.MIN_HEARTBEAT_PERIOD = 60 * 1000;
        //HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName);

    }


}
