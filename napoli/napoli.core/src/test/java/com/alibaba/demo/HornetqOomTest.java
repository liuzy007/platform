package com.alibaba.demo;

import com.alibaba.napoli.ResourceConstant;
import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.common.util.JmxUtil;
import java.io.Serializable;
import org.junit.Test;

/**
 * User: heyman
 * Date: 4/5/12
 * Time: 5:45 下午
 */
public class HornetqOomTest {
    @Test
    public void testHornetqSession() throws Exception{
        String queueName = "HornetqOomTest";
        String consoleAddress = ResourceConstant.shConsole;
        HttpUtil.createQueue(consoleAddress,queueName,"one-hornetq");
        NapoliConnector connector = NapoliConnector.createConnector(ResourceConstant.shConsole);
        DefaultAsyncReceiver receiver = DefaultAsyncReceiver.createReceiver(connector,queueName,false,5,new AsyncWorker() {
            public boolean doWork(Serializable message) {
                System.out.println(message);
                return true;
            }
        });
        receiver.init();
        NapoliConstant.MIN_HEARTBEAT_PERIOD = 10;
        receiver.setPeriod(10);
        receiver.start();
        String jmxAddrss = "service:jmx:rmi:///jndi/rmi://10.33.145.22:4000/jmxrmi";
                
        JmxUtil.shutdownMachine(jmxAddrss,"hornetQ");
        Thread.sleep(50000);
        JmxUtil.startMachine(jmxAddrss,"hornetQ");
        NapoliConstant.MIN_HEARTBEAT_PERIOD = 60 * 1000;
    }
}
