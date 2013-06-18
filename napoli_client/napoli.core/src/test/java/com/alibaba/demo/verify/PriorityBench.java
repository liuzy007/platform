package com.alibaba.demo.verify;

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.ext.AsyncWorkerEx;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;


/**
 * TODO 测试消息优先级
 * @author munch.wangr
 *
 */
public class PriorityBench {
	NapoliConnector connector;
	DefaultAsyncSender prioritySender;
	DefaultAsyncReceiver receiver;

	public void init() throws NapoliClientException{
		connector = new NapoliConnector();
		connector.setAddress("10.20.189.62:8080");
		connector.setStorePath("./target/napoli");
		connector.init();
		prioritySender = new DefaultAsyncSender();
		prioritySender.setConnector(connector);
		prioritySender.setName("TEST_PRIORITY");
		prioritySender.init();
		receiver = new DefaultAsyncReceiver();
		receiver.setConnector(connector);
		receiver.setName("TEST_PRIORITY");
		receiver.init();
		receiver.setExWorker(new AsyncWorkerEx() {
			
			public boolean doWork(NapoliMessage message) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(message); 
				return true;
			}
		});
	}
	

	public void testPriority() throws Exception{
		String msg = "msg ---";
		for (int i = 0; i < 100; i++) {
			NapoliMessage message = new NapoliMessage(msg+" 000000000000 ---");
			message.setPriority(0);
			prioritySender.send(message);
		}
		
		for (int i = 10; i < 20; i++) {
			NapoliMessage message = new NapoliMessage(msg+i+" ---");
			message.setPriority(5);
			prioritySender.send(message);			
		}
		

		for (int i = 20; i < 30; i++) {
			NapoliMessage message = new NapoliMessage(msg+i+" ---");
			message.setPriority(9);
			prioritySender.send(message);			
		}
		
		Thread.sleep(10000);
		
		receiver.start();		

		Thread.sleep(10000);
	}
}
