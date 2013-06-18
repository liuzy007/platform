package com.taobao.diamond.manager.impl;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;


public class PropertiesListenerUnitTest {
    private static class PropertiesListenerTest extends PropertiesListener {

        private Properties properties;


        @Override
        public void innerReceive(Properties properties) {
            this.properties = properties;
            System.out.println(this.properties);

        }


        public Properties getProperties() {
            return properties;
        }

    }


    @Test
    public void testNull() {
        String p = "";
        PropertiesListenerTest plt = new PropertiesListenerTest();
        plt.receiveConfigInfo(p);
        Assert.assertNull(plt.getProperties());
    }


    @Test
    public void testProperties() {
        String p = "notify.useJMX=true\r\n" + //
                "notify.rmi.port=9528\r\n" + //
                "notify.rmi.name=notifyServer\r\n" + //
                "store4j.useJMX=true\r\n";//
        PropertiesListenerTest plt = new PropertiesListenerTest();
        plt.receiveConfigInfo(p);
        Properties perperties = plt.getProperties();
        Assert.assertNotNull(perperties);
        Assert.assertEquals("true", perperties.getProperty("notify.useJMX"));
        Assert.assertEquals("9528", perperties.getProperty("notify.rmi.port"));
        Assert.assertEquals("notifyServer", perperties.getProperty("notify.rmi.name"));
        Assert.assertEquals("true", perperties.getProperty("store4j.useJMX"));
    }
    
}
