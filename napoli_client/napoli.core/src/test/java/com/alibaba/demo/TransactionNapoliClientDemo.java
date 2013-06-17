package com.alibaba.demo;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.PendingNotify;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.client.async.ext.AsyncWorkerEx;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.util.NapoliTestUtil;

/**
 * TODO 测试事务消息
 * @author munch.wangr
 *
 */
public class TransactionNapoliClientDemo{
    private static final Log     log = LogFactory.getLog(TransactionNapoliClientDemo.class);

    private NapoliConnector      connector;

    private DefaultAsyncSender   queueProducer;

    private DefaultAsyncReceiver consumer0;
    
 

    protected void setUp() throws Exception {
        connector = new NapoliConnector();
        connector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath"));
        connector.setAddress("10.20.189.62:8080");//设置napoli服务器地址
        connector.setInterval(10000);
        connector.setSendTimeout(1000);
        connector.init();

        queueProducer = new DefaultAsyncSender();
        queueProducer.setName("aa");
        queueProducer.setConnector(connector);
        queueProducer.setInstances(5);
        queueProducer.setPendingInterval(3000);
        queueProducer.setPendingNotifier(new PendingNotify(){

			public PendingNotifyStateEnum notify(NapoliMessage message) {
	            System.out.println(message); 
	            return PendingNotifyStateEnum.COMMIT;
            }
        	
        });
        queueProducer.init();

        consumer0 = new DefaultAsyncReceiver();
        consumer0.setName("aa");
        consumer0.setConnector(connector);
        consumer0.setInstances(5);
        AsyncWorkerEx worker = new DirrectShowWorker();
        consumer0.setExWorker(worker);
        consumer0.init();

    }

    public static void main(String[] args) throws Exception{
    	TransactionNapoliClientDemo demo = new TransactionNapoliClientDemo();
    	demo.setUp();
    	demo.testReceiverSend();
    	demo.tearDown(); 
    }
    
    public void testReceiverSend() throws Exception {
        ExecutorService execturor = Executors.newFixedThreadPool(10);
        execturor.execute(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				for (int i = 0; i < 10000; i++) {
			        for (int ii = 0; ii < 10; ii++) {
			        	try {

			                queueProducer.send(new NapoliMessage("zgl-zgl_new----"+i),new Runnable() {
			    				
			    				public void run() {
			    					
			    				}
			    			});
			                Thread.sleep(300);
			            } catch (Exception e) {
				           e.printStackTrace();
			            }
//			    	queueProducer.send(new WStr("zgl-zgl_new----"+i));
			        }
//			        Thread.sleep(1000);
			        }
			}
		});
        consumer0.start();
//        for (int i = 0; i < 100; i++) {
//        for (int ii = 0; ii < 1000; ii++) {
//        	try {
//
//                queueProducer.send(new NapoliMessage("zgl-zgl_new----"+i),new Runnable() {
//    				
//    				public void run() {
//    					//					
//    				}
//    			});
//            } catch (Exception e) {
//	           e.printStackTrace();
//            }
////    	queueProducer.send(new WStr("zgl-zgl_new----"+i));
//        }
////        Thread.sleep(1000);
//        }

        try {
//            Thread.sleep(100);
        } catch (Exception e) {
            log.error(e);
        }

        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            log.error(e);
        }
    }


    protected void tearDown() throws Exception {
        queueProducer.close();
        consumer0.close();
        connector.close();
    }
    static class WStr implements Serializable{
        private static final long serialVersionUID = 6813828196373213988L;
		String content;
    	public WStr(String content){
    		this.content = content;
    	}
    	public String toString(){
    		return content;
    	}
    }

    private static class DirrectShowWorker implements AsyncWorkerEx {
    	private AtomicInteger count = new AtomicInteger(0);
        public boolean doWork(NapoliMessage msg) {
        	 count.incrementAndGet();
//             System.out.println("#########################"+count+"           "+msg);
            return true;
        }

    }
}
