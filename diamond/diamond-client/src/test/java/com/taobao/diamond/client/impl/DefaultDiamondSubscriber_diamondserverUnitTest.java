package com.taobao.diamond.client.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.taobao.diamond.client.SubscriberListener;
import com.taobao.diamond.configinfo.ConfigureInfomation;


public class DefaultDiamondSubscriber_diamondserverUnitTest {

    private DefaultDiamondSubscriber subscriber = null;
    private SubscriberListener listener = null;


    @Test
    public void test_主动请求_diamondserver发布数据或更新数据() throws Exception {
        subscriber = new DefaultDiamondSubscriber(listener);

        final java.util.concurrent.atomic.AtomicBoolean invoked = new AtomicBoolean(false);
        this.subscriber.setSubscriberListener(new SubscriberListener() {

            public void receiveConfigInfo(ConfigureInfomation configureInfomation) {
                System.out.println("接收到配置信息" + configureInfomation);
                invoked.set(true);
            }


            public Executor getExecutor() {

                return null;
            }
        });
        this.subscriber.addDataId("diamondserver", "test");

        int i = 0;
        while (!invoked.get()) {
            Thread.sleep(2000);
            System.out.println("等待通知 " + i + " 次");
            i++;
        }
    }

}
