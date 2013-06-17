package com.alibaba.demo;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import com.alibaba.napoli.MemoryTestUtil;
import com.alibaba.napoli.PerfCase;
import com.alibaba.napoli.common.util.ConfigServiceHttpImpl;
import com.alibaba.napoli.domain.client.ClientDestination;

/**
 * User: heyman Date: 2/9/12 Time: 4:30 下午
 */
public class ConfigServicePerfTest {
    public void testConfigSerivce() throws Exception {
        final ConfigServiceHttpImpl configService = new ConfigServiceHttpImpl("10.33.145.22");
        PerfCase perfCase = new PerfCase() {
            public void setup() throws Exception {
            }

            public void excute() throws Exception {
                configService.fetchDestination("amqtest");
            }

            public void close() {
            }
        };
        MemoryTestUtil.perf(10, 100, perfCase);
    }

    public void testConfigServiceFromSpring() throws Exception {
        //final ConfigService configService = new ConfigServiceImp("10.33.145.22");
        String address = "10.33.145.22";
        String url = "http://" + address + "/napoli/remoting/config_service_exporter";
        final HttpInvokerProxyFactoryBean factoryBean = new HttpInvokerProxyFactoryBean();
        factoryBean.setServiceUrl(url);
        factoryBean.setServiceInterface(ConfigServiceHttpImpl.class);
        factoryBean.afterPropertiesSet();
        final ConfigServiceHttpImpl configService = (ConfigServiceHttpImpl) factoryBean.getObject();

        PerfCase perfCase = new PerfCase() {
            public void setup() throws Exception {
            }

            public void excute() throws Exception {
                configService.fetchDestination("amqtest");
            }

            public void close() {
            }
        };
        MemoryTestUtil.perf(50, 1000, perfCase);

    }

    public static void main(String[] args) throws Exception {
        /*
         * ConfigServicePerfTest configServicePerfTest = new
         * ConfigServicePerfTest(); configServicePerfTest.testConfigSerivce();
         * configServicePerfTest.testConfigServiceFromSpring();
         */

        final ConfigServiceHttpImpl configService = new ConfigServiceHttpImpl("10.20.153.30");
        ClientDestination destination = configService.fetchDestination("dubbo_response_test");
        System.out.println(destination);

        /*
         * final ConfigService configService = new
         * ConfigServiceHttpImpl("10.20.153.30"); Destination destination =
         * configService.fetchDestination("liuchao_test_2");
         * System.out.println(destination);
         */
    }
}
