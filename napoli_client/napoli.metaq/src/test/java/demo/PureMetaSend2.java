package demo;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.napoli.metamorphosis.Message;
import com.alibaba.napoli.metamorphosis.client.MessageSessionFactory;
import com.alibaba.napoli.metamorphosis.client.MetaClientConfig;
import com.alibaba.napoli.metamorphosis.client.MetaMessageSessionFactory;
import com.alibaba.napoli.metamorphosis.client.producer.MessageProducer;
import com.alibaba.napoli.metamorphosis.client.producer.SendResult;
import com.alibaba.napoli.metamorphosis.utils.ZkUtils.ZKConfig;
import com.alibaba.napoli.metamorphosis.utils.test.ConcurrentTestCase;
import com.alibaba.napoli.metamorphosis.utils.test.ConcurrentTestTask;

public class PureMetaSend2 {
	
	private static void sendMessage(final AtomicLong timeout, final byte[] data, final String topic,
            final MessageProducer producer, final AtomicLong sent, final AtomicLong failed) {
        try {
            final SendResult result =
                    producer.sendMessage(new Message(topic, data, String.valueOf(System.currentTimeMillis())));
            if (!result.isSuccess()) {
                failed.incrementAndGet();
            }
            else {
//            	System.out.println("result.getMessage()======="+result.getMessage());
            	sent.incrementAndGet();
            }
        }
        catch (final Throwable t) {
            t.printStackTrace();
            failed.incrementAndGet();
        }
    }
	
	
	 public static void main(final String[] args) throws Exception {
	        
	        MetaClientConfig metaClientConfig = new MetaClientConfig();
			ZKConfig zk = new ZKConfig();
			
			zk.zkConnect = "10.33.145.22:11220";
			metaClientConfig.setZkConfig(zk);

	        // 每个线程发送消息数
	        int repeats = 100000;
	        // 线程数
	        int threads = 20;
	        // 发送超时
	        final AtomicLong timeout = new AtomicLong(10000L);
	        int dataSize = 1024 ;
	        final byte[] data = getData(dataSize);

	        final String topic = "a";
	        final MessageSessionFactory sessionFactory = new MetaMessageSessionFactory(
					metaClientConfig);
			// create producer
			final MessageProducer producer = sessionFactory.createProducer();
	        producer.publish(topic);

	        final AtomicLong sent = new AtomicLong(0);
	        final AtomicLong failed = new AtomicLong(0);
	        final ConcurrentTestCase testCase = new ConcurrentTestCase(threads, repeats, new ConcurrentTestTask() {

	            public void run(final int index, final int times) throws Exception {
	                sendMessage(timeout, data, topic, producer, sent, failed);
	            }

	        });
	        final AtomicBoolean shutdown = new AtomicBoolean(false);
	        final long start = System.currentTimeMillis();
	        final Thread printThread = new Thread() {
	            @Override
	            public void run() {
	                while (!shutdown.get()) {
	                    try {
	                        final long duration = (System.currentTimeMillis() - start) / 1000;
	                        final long count = sent.get();
	                        System.out.println("Sent:" + count + "                       Failed:" + failed.get()
	                                + "                   TPS:" + count / duration);
	                        Thread.sleep(1000);
	                    }
	                    catch (final Throwable t) {

	                    }
	                }
	            }
	        };
	        printThread.start();
	        testCase.start();
	        shutdown.set(true);
	        printThread.interrupt();
	        printThread.join();
	        System.out.println("耗费时间，单位毫秒：" + testCase.getDurationInMillis());
	    }

	 public static byte[] getData(int size) {
	        byte[] data = new byte[size];
	        for (int i = 0; i < size; i++) {
	            data[i] = (byte) (i % 127);
	        }
	        return data;

	    }
}
