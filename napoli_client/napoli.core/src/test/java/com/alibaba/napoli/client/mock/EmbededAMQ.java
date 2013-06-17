package com.alibaba.napoli.client.mock;

import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;

public class EmbededAMQ {
	private String address;
	private BrokerService broker;
	private BrokerPlugin[] plugins;
	
	public EmbededAMQ(String address,BrokerPlugin[] plugins){
	    super();
	    this.address = address;
	    try {
	        broker = createBrokerService(address, plugins);
        } catch (Exception e) {
        	throw new RuntimeException("",e);
        }
    }
	
	
	
	public String getAddress() {
    	return address;
    }



	public BrokerService getBroker() {
    	return broker;
    }



	public BrokerPlugin[] getPlugins() {
    	return plugins;
    }



	public void close(){
		try {
	        broker.stop();
        } catch (Exception e) {
        	throw new RuntimeException("",e);
        }
        plugins = null;
        address = null;
	}
	
	public static final BrokerService createBrokerService(String url,
			BrokerPlugin[] plugins) throws Exception {
		BrokerService embeded = new BrokerService();
		embeded.addConnector(url);
		embeded.setUseJmx(false);
		embeded.setPlugins(plugins);
		embeded.setPersistent(true);
		embeded.setDeleteAllMessagesOnStartup(true);
		embeded.start();
		return embeded;
	}
	
}
