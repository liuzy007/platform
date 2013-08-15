package demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.alibaba.napoli.metamorphosis.Message;
import com.alibaba.napoli.metamorphosis.client.MessageSessionFactory;
import com.alibaba.napoli.metamorphosis.client.MetaClientConfig;
import com.alibaba.napoli.metamorphosis.client.MetaMessageSessionFactory;
import com.alibaba.napoli.metamorphosis.client.producer.MessageProducer;
import com.alibaba.napoli.metamorphosis.client.producer.SendResult;
import com.alibaba.napoli.metamorphosis.utils.ZkUtils.ZKConfig;

public class PureMetaSender {
	
	public static void main(String[] args)throws Exception {
		MetaClientConfig metaClientConfig = new MetaClientConfig();
		ZKConfig zk = new ZKConfig();
		
		zk.zkConnect = "10.33.145.22:11220";
		metaClientConfig.setZkConfig(zk);
		// New session factory
		final MessageSessionFactory sessionFactory = new MetaMessageSessionFactory(
				metaClientConfig);
		// create producer
		final MessageProducer producer = sessionFactory.createProducer();
		// publish topic
		final String topic = "a";
		producer.publish(topic);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String line = null;
		while ((line = readLine(reader)) != null) {

			try {
				// send message
				final SendResult sendResult = producer.sendMessage(new Message(
						topic, line.getBytes()));
				// check result
				if (!sendResult.isSuccess()) {
					System.err.println("Send message failed,error message:"
							+ sendResult.getErrorMessage());
				} else {
					System.out.println("Send message successfully,sent to "
							+ sendResult.getPartition()+",message is ["+sendResult.getMessage()+"]");
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private static String readLine(final BufferedReader reader)
			throws IOException {
		System.out.println("Type a message to send:");
		return reader.readLine();
	}
}
