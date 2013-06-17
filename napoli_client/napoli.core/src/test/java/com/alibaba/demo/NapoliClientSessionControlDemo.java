package com.alibaba.demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;

public class NapoliClientSessionControlDemo{
//    private static final Log     log = LogFactory.getLog(NapoliClientSessionControlDemo.class);

    private static NapoliConnector      connector;

    private static DefaultAsyncSender   queueProducer;

    private static DefaultAsyncReceiver consumer0;

    public static final void main(String[] args) throws NapoliClientException, InterruptedException{
    	connector = new NapoliConnector();
        connector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath"));
        connector.setAddress("10.20.132.18:8080");
//        connector.setPoolSize(5);
        connector.setSendTimeout(400);
        connector.setSendSessionControl(true);
        connector.init();

        queueProducer = new DefaultAsyncSender(){
        	public boolean send(Serializable msg){
        		long cost = System.currentTimeMillis();
        		super.send(msg);
        		System.out.println(System.currentTimeMillis()-cost); 
				return true;
        	}
        };
        queueProducer.setName("SessionContolTest");
        queueProducer.setConnector(connector);
        queueProducer.setInstances(5);
        queueProducer.init();
        List<Thread> thl = new ArrayList<Thread>();
        int tc = 100;
        for(int i = 0;i < tc;i++){
        	Thread t = new StressThread(i);
        	thl.add(t);
        	t.start(); 
        }
        Thread.sleep(50*1000);
        consumer0 = new DefaultAsyncReceiver();
        consumer0.setName("SessionContolTest");
        consumer0.setConnector(connector);
        consumer0.setInstances(5);
        AsyncWorker worker = new DirrectShowWorker();
        consumer0.setWorker(worker);
        consumer0.init();
        consumer0.start();
        Thread.sleep(50*1000);
    }
       
    static class StressThread extends Thread{
    	int index;
    	public StressThread(int index){
    		this.index = index;
    	}
    	public void run(){
    		while(true){
    			queueProducer.send(""+index);
    		}
    	}
    }
    

    private static class DirrectShowWorker implements AsyncWorker {
        public boolean doWork(Serializable msg) {
            return true;
        }

    }
}
