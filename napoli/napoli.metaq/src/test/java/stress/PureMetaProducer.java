package stress;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.napoli.metamorphosis.Message;
import com.alibaba.napoli.metamorphosis.client.MessageSessionFactory;
import com.alibaba.napoli.metamorphosis.client.MetaClientConfig;
import com.alibaba.napoli.metamorphosis.client.MetaMessageSessionFactory;
import com.alibaba.napoli.metamorphosis.client.producer.MessageProducer;
import com.alibaba.napoli.metamorphosis.client.producer.SendResult;
import com.alibaba.napoli.metamorphosis.utils.ZkUtils;
import com.alibaba.napoli.metamorphosis.utils.test.ConcurrentTestCase;
import com.alibaba.napoli.metamorphosis.utils.test.ConcurrentTestTask;

public class PureMetaProducer {

	private static int threads = 50;
	private static int repeats = 1000;
	private static String topic = "a";
	private static byte[] data = getData(4096);
	
	public static void main(String[] args) throws Exception {
		MetaClientConfig config = new MetaClientConfig();
		ZkUtils.ZKConfig zkConfig = new ZkUtils.ZKConfig();
        zkConfig.zkConnect = "10.33.145.22:11220";
        config.setZkConfig(zkConfig);
        MessageSessionFactory sessionFactory = new MetaMessageSessionFactory(config);
		final MessageProducer producer = sessionFactory.createProducer();
		producer.publish(topic);
		final AtomicLong sent = new AtomicLong(0);
        final AtomicLong failed = new AtomicLong(0);
		
		final ConcurrentTestCase testCase = new ConcurrentTestCase(threads, repeats, new ConcurrentTestTask() {

            public void run(final int index, final int times) throws Exception {
            	try {
                    final SendResult result =
                            producer.sendMessage(new Message(topic, data, String.valueOf(System.currentTimeMillis())));
                    if (!result.isSuccess()) {
                        failed.incrementAndGet();
                    }
                    else {
                        sent.incrementAndGet();
                    }
                }
                catch (final Throwable t) {
                    t.printStackTrace();
                    failed.incrementAndGet();
                }

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
	
	
	private static byte[] getData(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i % 127);
        }
        return data;

    }
}
