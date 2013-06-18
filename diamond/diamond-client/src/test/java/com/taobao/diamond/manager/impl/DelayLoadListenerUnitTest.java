package com.taobao.diamond.manager.impl;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;


public class DelayLoadListenerUnitTest {

    private static class DelayLoadListenerTest extends DelayLoadListener {

        private String configInfo;


        @Override
        public void innerReceive(String configInfo) {
            this.configInfo = configInfo;
        }


        public String getConfigInfo() {
            return configInfo;
        }

    }


    @Test
    public void testGetEffectiveTime() throws Exception {
        DelayLoadListenerTest listener = new DelayLoadListenerTest();
        Method method = DelayLoadListener.class.getDeclaredMethod("getEffectiveTime", String.class);
        method.setAccessible(true);

        SimpleDateFormat format = new SimpleDateFormat(DelayLoadListener.DATE_FORMAT);
        String timeStr = format.format(new Date());
        Date expected = format.parse(timeStr);

        // 正常情况
        String config =
                "<!--diamond-config-effective-time =\"" + timeStr + "\"   -->"
                        + "<bean class=\"com.taobao.session.config.SpringConfigFactory\">"
                        + "        <property name=\"defaultConfig\">" + "<props>"
                        + "<prop key=\"storeKey\">cookie</prop>" + "<prop key=\"lifeCycle\">-1</prop>"
                        + "<prop key=\"domain\">.taobao.com</prop>" + "</props>" + "</property></bean> ";
        System.out.println(config);

        long time = (Long) method.invoke(listener, config);
        Assert.assertEquals(expected.getTime(), time);

        // 正常情况
        config =
                "#diamond-config-effective-time   = \"   " + timeStr + "  \"   -->"
                        + "<bean class=\"com.taobao.session.config.SpringConfigFactory\">"
                        + "        <property name=\"defaultConfig\">" + "<props>"
                        + "<prop key=\"storeKey\">cookie</prop>" + "<prop key=\"lifeCycle\">-1</prop>"
                        + "<prop key=\"domain\">.taobao.com</prop>" + "</props>" + "</property></bean> ";
        System.out.println(config);

        time = (Long) method.invoke(listener, config);
        Assert.assertEquals(expected.getTime(), time);

        // 不存在effective time
        config =
                "#diamond-config-effective-time_e   = \"   " + timeStr + "  \"   "
                        + "<bean class=\"com.taobao.session.config.SpringConfigFactory\">"
                        + "        <property name=\"defaultConfig\">" + "<props>"
                        + "<prop key=\"storeKey\">cookie</prop>" + "<prop key=\"lifeCycle\">-1</prop>"
                        + "<prop key=\"domain\">.taobao.com</prop>" + "</props>" + "</property></bean> ";
        System.out.println(config);

        time = (Long) method.invoke(listener, config);
        Assert.assertEquals(0, time);

        // effective time格式不对
        config =
                "#diamond-config-effective-time   = \"xxx \"   "
                        + "<bean class=\"com.taobao.session.config.SpringConfigFactory\">"
                        + "        <property name=\"defaultConfig\">" + "<props>"
                        + "<prop key=\"storeKey\">cookie</prop>" + "<prop key=\"lifeCycle\">-1</prop>"
                        + "<prop key=\"domain\">.taobao.com</prop>" + "</props>" + "</property></bean> ";
        System.out.println(config);

        time = (Long) method.invoke(listener, config);
        Assert.assertEquals(0, time);
    }


    @Test
    public void testGetDelay() throws Exception {
        DelayLoadListenerTest listener = new DelayLoadListenerTest();
        Method method = DelayLoadListener.class.getDeclaredMethod("getDelay", String.class);
        method.setAccessible(true);

        SimpleDateFormat format = new SimpleDateFormat(DelayLoadListener.DATE_FORMAT);
        String timeStr = format.format(new Date(System.currentTimeMillis() + 10000000L));

        // 正常情况
        String config =
                "<!--diamond-config-effective-time =\"" + timeStr + "\"   -->"
                        + "<bean class=\"com.taobao.session.config.SpringConfigFactory\">"
                        + "        <property name=\"defaultConfig\">" + "<props>"
                        + "<prop key=\"storeKey\">cookie</prop>" + "<prop key=\"lifeCycle\">-1</prop>"
                        + "<prop key=\"domain\">.taobao.com</prop>" + "</props>" + "</property></bean> ";
        System.out.println(config);

        long time = (Long) method.invoke(listener, config);
        Assert.assertTrue(time > 0);

        // 过期时间
        timeStr = format.format(new Date(System.currentTimeMillis() - 10000000L));
        config =
                "<!--diamond-config-effective-time =\"" + timeStr + "\"   -->"
                        + "<bean class=\"com.taobao.session.config.SpringConfigFactory\">"
                        + "        <property name=\"defaultConfig\">" + "<props>"
                        + "<prop key=\"storeKey\">cookie</prop>" + "<prop key=\"lifeCycle\">-1</prop>"
                        + "<prop key=\"domain\">.taobao.com</prop>" + "</props>" + "</property></bean> ";
        System.out.println(config);

        time = (Long) method.invoke(listener, config);
        Assert.assertEquals(0, time);

        // effective time 不存在
        config =
                "<!--diamond-config-effective-time =\"xxx\"   -->"
                        + "<bean class=\"com.taobao.session.config.SpringConfigFactory\">"
                        + "        <property name=\"defaultConfig\">" + "<props>"
                        + "<prop key=\"storeKey\">cookie</prop>" + "<prop key=\"lifeCycle\">-1</prop>"
                        + "<prop key=\"domain\">.taobao.com</prop>" + "</props>" + "</property></bean> ";
        System.out.println(config);

        time = (Long) method.invoke(listener, config);
        Assert.assertEquals(0, time);

    }


    @Test
    public void testDelayReceive() throws Exception {
        
        // 没有延迟
        DelayLoadListenerTest listener = new DelayLoadListenerTest();
        Method method = DelayLoadListener.class.getDeclaredMethod("delayReceive", String.class, long.class);
        method.setAccessible(true);
        String config = "test" + System.currentTimeMillis();
        method.invoke(listener, config, 0);        
        Assert.assertEquals(config, listener.getConfigInfo());

        // 延迟 2 秒
        listener = new DelayLoadListenerTest();
        config = "test" + System.currentTimeMillis();
        method.invoke(listener, config, 2000L);
        Thread.sleep(3000L);
        Assert.assertEquals(config, listener.getConfigInfo());
        
        // 取消前一个任务
        listener = new DelayLoadListenerTest();
        String config1 = "test" + System.currentTimeMillis();
        method.invoke(listener, config1, 2000L);
        Thread.sleep(200L);
        String config2 = "test" + System.currentTimeMillis();
        method.invoke(listener, config2, 0);
        Thread.sleep(3000L);
        Assert.assertEquals(config2, listener.getConfigInfo());
        
    }
}
