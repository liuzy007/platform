package com.alibaba.napoli.base;

import com.alibaba.napoli.MqUtil;
import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.SendResult;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.common.util.JmxUtil;
import com.alibaba.napoli.connector.ConsoleConnector;

import com.alibaba.napoli.metamorphosis.exception.MetaClientException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * User: heyman Date: 6/12/12 Time: 3:01 PM
 */
public class MetaqTest {
    private static final Log log                  = LogFactory
            .getLog(MetaqTest.class);
	private static final String address = NapoliTestUtil.getAddress();
	private static String topicName = "metaqTest";

	@BeforeClass
	public static void setup() throws Exception {

		HttpUtil.deleteTopic(address, topicName);
		HttpUtil.createTopic(address, topicName, "metaqGroup");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConsoleConnector.closeAll();
	}

	@Test
	public void testTopic() throws Exception {

		JmxUtil.logEnabled = true;
		long sendSum = 1000;
		final AtomicLong receiveCount = new AtomicLong();

		NapoliConnector connector = new NapoliConnector(address);
		connector.init();
		AsyncWorker worker = new AsyncWorker() {
			public boolean doWork(Serializable message) {
				receiveCount.incrementAndGet();
				return true;
			}
		};
		// Thread.sleep(30000);
		String name = "MetaqTest";
		DefaultAsyncReceiver defaultReceiver = DefaultAsyncReceiver
				.createReceiver(connector, topicName, false, 6, worker, "heymanchen");
		defaultReceiver.start();
		try {
			MqUtil.Result result = MqUtil.sendMessages(address, topicName, 10,
					sendSum);
			System.out.println(result);

			JmxUtil.waitTillMetaqTopicSizeAsTarget(address, topicName,
					"heymanchen", 0, -1);
			log.info("consumer gets " + receiveCount.get());

			assertTrue("Receiver gets " + receiveCount.get()
					+ " less than success count " + result.successCount.get() + " or more than 5%",
					receiveCount.get() >= result.successCount.get() && receiveCount.get() <=result.successCount.get() * 1.05);
		} finally {
			defaultReceiver.close();
		}
	}
    
    @Test 
    public void testTwoConsumerOneGroup() throws Exception{
        //long sendSum = 1000;
        final AtomicLong receiveCount = new AtomicLong();

        NapoliConnector connector = new NapoliConnector(address);
        connector.init();
        AsyncWorker worker = new AsyncWorker() {
            public boolean doWork(Serializable message) {
                receiveCount.incrementAndGet();
                return true;
            }
        };
        // Thread.sleep(30000);

        DefaultAsyncReceiver defaultReceiver = null;
            defaultReceiver = DefaultAsyncReceiver
                    .createReceiver(connector, topicName, false, 6, worker, "heymanchen");
            defaultReceiver.start();
        

        DefaultAsyncReceiver defaultReceiver2 = null;
        try {
            defaultReceiver2 = DefaultAsyncReceiver
                    .createReceiver(connector, topicName, false, 6, worker, "heymanchen");
            defaultReceiver2.start();
            fail("should exception catch");
        } catch (NapoliClientException e) {
            
        }finally {
            assert defaultReceiver != null;
            defaultReceiver.close();
            if (defaultReceiver2 != null){
                defaultReceiver2.close();
            }
        }
        
       
    }

	@Test
	public void testTopicUrgent() throws Exception {

		int instances = 6;
		final AtomicLong receiveCount = new AtomicLong();
		final Map<String, Long> utimeMap = new HashMap<String, Long>();

		NapoliConnector connector = new NapoliConnector();
		connector.setAddress(address);
		connector.setPoolSize(5);
		connector.setPrefetch(5);

		connector.init();

		//final AtomicLong messageNo = new AtomicLong(0);
		AsyncWorker worker = new AsyncWorker() {
			public boolean doWork(Serializable message) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (utimeMap.keySet().contains(message)) {
					System.err.println("dowork " + receiveCount.incrementAndGet() + " the urgent msg="
							+ message
							+ " costTime:"
							+ (System.currentTimeMillis() - utimeMap
									.get(message)));
				}else{
					System.err.println("dowork handle " + receiveCount.incrementAndGet() + " non urgent msg=" + message);
				}
				
				return true;
			}
		};

		// final DefaultAsyncReceiver receiver = connector.createReceiver(queue,
		// true, instances, worker);
		DefaultAsyncReceiver defaultReceiver = new DefaultAsyncReceiver();
		defaultReceiver.setConnector(connector);
		defaultReceiver.setName(topicName);
		defaultReceiver.setStoreEnable(false);
		defaultReceiver.setWorkerPri(worker);
		defaultReceiver.setReprocessNum(6);
		defaultReceiver.setInstances(instances);
		defaultReceiver.init();
		// defaultReceiver.setPeriod(1);
		defaultReceiver.setGroup("MetaqTest");
		defaultReceiver.start();
		try {
			final AtomicLong successCount = new AtomicLong(0);
			final AtomicLong failCount = new AtomicLong(0);
			final DefaultAsyncSender sender = new DefaultAsyncSender();
			sender.setConnector(connector);
			sender.setName(topicName);
			sender.setStoreEnable(false);
			sender.init();

			long begin = System.currentTimeMillis();

		/*	sendMessages(sender, topicName, 1, 10, successCount, failCount,
					"u1", true, utimeMap);
			sendMessages(sender, topicName, 10, 1000, successCount, failCount,
					"u2", true, utimeMap);
			sendMessages(sender, topicName, 10, 1000, successCount, failCount,
					"u3", false, utimeMap);
			sendMessages(sender, topicName, 10, 1000, successCount, failCount,
					"u4", true, utimeMap);
			sendMessages(sender, topicName, 10, 1000, successCount, failCount,
					"u5", false, utimeMap);
*/
			sendMessages(sender, topicName, 1, 10, successCount, failCount,
					"u1", true, utimeMap);
			sendMessages(sender, topicName, 10, 55, successCount, failCount,
					"u2", true, utimeMap);
			sendMessages(sender, topicName, 5, 23, successCount, failCount,
					"u3", false, utimeMap);
			
			Map<String, Long> result = new HashMap<String, Long>();
			result.put("success", successCount.get());
			result.put("fail", failCount.get());
			result.put("cost", System.currentTimeMillis() - begin);
			System.out.println(result);

			JmxUtil.waitTillMetaqTopicSizeAsTarget(address, topicName,
					"MetaqTest", 0, -1);
			log.info("consumer gets " + receiveCount.get());

			assertTrue("Receiver gets " + receiveCount.get()
					+ " less than success count " + successCount.get() + " or more than 5%",
					receiveCount.get() >= successCount.get() && receiveCount.get() <=successCount.get() * 1.05);
		} finally {
			defaultReceiver.close();
		}
	}

	private long sendMessages(final DefaultAsyncSender sender,
			String topicName, int threadNum, final long msgSum,
			final AtomicLong successCount, final AtomicLong failCount,
			String urgentMsg, boolean head, Map<String, Long> utimeMap)
			throws Exception {
		final CountDownLatch endLatch = new CountDownLatch(threadNum);
		final NapoliMessage napoliMessage = new NapoliMessage(new byte[1024]);
		final AtomicLong msgSumCount = new AtomicLong(msgSum - 1);
		String msgId;
		long urgentTime = 0;
		successCount.incrementAndGet();
		if (head) {
			SendResult sendResult = sender.sendNapoliMessage(new NapoliMessage(
					urgentMsg));
			msgId = sendResult.getMsgId();
			NapoliMessage urgentMessage = new NapoliMessage("urgent notify 1");
			urgentMessage.setUrgentid(msgId);
			sender.sendNapoliMessage(urgentMessage);
			System.out.println("send urgentid=" + msgId);
			urgentTime = System.currentTimeMillis();
			utimeMap.put(urgentMsg, urgentTime);
		}
		for (int i = 0; i < threadNum; i++) {
			Thread thread = new Thread("topic=" + topicName + " client=" + i) {
				public void run() {
					try {
						while (msgSumCount.decrementAndGet() >= 0) {
							SendResult sendResult = sender
									.sendNapoliMessage(napoliMessage);
							if (sendResult.success()) {
								successCount.incrementAndGet();
							} else {
								failCount.incrementAndGet();
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					endLatch.countDown();
				}
			};
			thread.setDaemon(true);
			thread.start();
		}
		endLatch.await();
		if (!head) {
			SendResult sendResult = sender.sendNapoliMessage(new NapoliMessage(
					urgentMsg));
			msgId = sendResult.getMsgId();
			NapoliMessage urgentMessage = new NapoliMessage("urgent notify 2");
			urgentMessage.setUrgentid(msgId);
			sender.sendNapoliMessage(urgentMessage);
			System.out.println("send urgentid=" + msgId);
			urgentTime = System.currentTimeMillis();
			utimeMap.put(urgentMsg, urgentTime);
		}
		return urgentTime;
	}

	@Test
	public void testMsgSelector() throws Exception {
		 JmxUtil.logEnabled = true;
		// 发送100条可被消费的消息,再发送100条不可被消费的数据,间隔着发,统计接收到的消息是100条

		final AtomicLong receiveCount1 = new AtomicLong(0);
		final AtomicLong receiveCount2 = new AtomicLong(0);

		NapoliConnector connector = new NapoliConnector(address);
		connector.init();
		DefaultAsyncSender sender = DefaultAsyncSender.createSender(connector,
				topicName, false);
		sender.init();

		DefaultAsyncReceiver receiver1 = DefaultAsyncReceiver.createReceiver(
				connector, topicName, false, 6, new AsyncWorker() {
					public boolean doWork(Serializable message) {

						receiveCount1.incrementAndGet();
						/*
						 * System.out.println("yanny recever1 got message " +
						 * message);
						 * 
						 * try { Thread.sleep(100); } catch (Exception e) { }
						 */
						return true;
					}
				});
		receiver1.setMessageSelector("type='1'");
		receiver1.setGroup("MetaqTesta");
		receiver1.init();
		receiver1.start();

		DefaultAsyncReceiver receiver2 = DefaultAsyncReceiver.createReceiver(
				connector, topicName, false, 6, new AsyncWorker() {
					public boolean doWork(Serializable message) {

						receiveCount2.incrementAndGet();
						/*
						 * System.out.println("yanny recever2 got message " +
						 * message);
						 * 
						 * try { Thread.sleep(100); } catch (Exception e) { }
						 */
						return true;
					}
				});
		receiver2.setMessageSelector("type='2'");
		receiver2.setGroup("MetaqTestb");
		receiver2.init();
		receiver2.start();

		try {
			NapoliMessage napoliMessage = new NapoliMessage("hello world!");
			napoliMessage.setProperty("type", "1");
			MqUtil.sendMessages(sender, 10, 2000, napoliMessage);
			napoliMessage = new NapoliMessage("hello world2!");
			napoliMessage.setProperty("type", "2");
			MqUtil.sendMessages(sender, 10, 1500, napoliMessage);
			sender.close();
			JmxUtil.waitTillMetaqTopicSizeAsTarget(address, topicName,
					"MetaqTestb", 0, -1);

			JmxUtil.waitTillMetaqTopicSizeAsTarget(address, topicName,
					"MetaqTesta", 0, -1);

			System.out.println("receive1=" + receiveCount1.get()
					+ " receiver2=" + receiveCount2.get());
			assertTrue("receiveCount1 gets " + receiveCount1.get()
					+ " less than success count 2000 or more than 5%",
					receiveCount1.get() >= 2000 && receiveCount1.get() <=2000 * 1.05);
			assertTrue("receiveCount2 gets " + receiveCount2.get()
					+ " less than success count 1500 or more than 5%",
					receiveCount2.get() >= 1500 && receiveCount2.get() <=1500 * 1.05);
		} finally {
			receiver1.close();
			receiver2.close();
		}
	}
}
