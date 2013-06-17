package com.alibaba.demo;

import com.alibaba.napoli.AbstractPerfCase;
import com.alibaba.napoli.MemoryTestUtil;
import com.alibaba.napoli.PerfCase;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.util.ConfigServiceHttpImpl;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.domain.client.ClientDestination;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import static org.junit.Assert.assertEquals;

/**
 * User: heyman Date: 2/28/12 Time: 11:00 上午
 */
public class ConfigServiceTest {
    @Test
    public void testPerf() throws Exception {
        final List<String> names = new ArrayList<String>();
        long time = System.currentTimeMillis();
        String topicName = "PerfTopic_" + time;
        String queueName1 = "PerfTopic_q1_" + time;
        String queueName2 = "PerfTopic_q2_" + time;
        String queueName3 = "PerfTopic_q3_" + time;

        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName1,
                NapoliTestUtil.getProperty("napoli.func.mixGroupName"));
        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName2,
                NapoliTestUtil.getProperty("napoli.func.mixGroupName"));
        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName3,
                NapoliTestUtil.getProperty("napoli.func.mixGroupName"));

        HttpUtil.createVtopic(NapoliTestUtil.getAddress(), topicName,
                new String[]{queueName1, queueName2, queueName3});

        names.add(queueName1);
        names.add(queueName2);
        names.add(queueName3);
        names.add(topicName);
        names.add("N/A");
        PerfCase perfCase = new AbstractPerfCase() {
            public void excute() throws Exception {
                ConfigServiceHttpImpl configService = new ConfigServiceHttpImpl(NapoliTestUtil.getAddress());
                Map<String, ClientDestination> result = configService.fetchDestination(names);
                assertEquals(5, result.size());
            }
        };
        MemoryTestUtil.perf(10, 1000, perfCase);

        PerfCase perfCase1 = new AbstractPerfCase() {
            public void excute() throws Exception {
                String url = "http://" + NapoliTestUtil.getAddress() + "/napoli/remoting/config_service_exporter";
                final HttpInvokerProxyFactoryBean factoryBean = new HttpInvokerProxyFactoryBean();
                factoryBean.setServiceUrl(url);
                factoryBean.setServiceInterface(ConfigServiceHttpImpl.class);
                factoryBean.afterPropertiesSet();
                ConfigServiceHttpImpl configService = (ConfigServiceHttpImpl) factoryBean.getObject();
                Map<String, ClientDestination> result = configService.fetchDestination(names);
                assertEquals(5, result.size());
            }
        };

        MemoryTestUtil.perf(10, 1000, perfCase1);

    }
}
