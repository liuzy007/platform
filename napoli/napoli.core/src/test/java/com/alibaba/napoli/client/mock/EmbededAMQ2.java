package com.alibaba.napoli.client.mock;

import java.util.Timer;
import java.util.TimerTask;

import com.alibaba.napoli.common.util.TransactionSupport;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.Message;

public class EmbededAMQ2 {
	private String address;
	private final BrokerService broker;
	private String dataDir;
	private BrokerPlugin[] plugins;
	private Timer logTimer = new Timer("Embeded MQ Log Timer", true);

	public EmbededAMQ2(String address, BrokerPlugin[] plugins, String dataDir) {
		super();
		this.address = address;
		this.dataDir = dataDir;
		try {
			broker = createBrokerService(address, plugins);
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
		logTimer.schedule(new TimerTask() {

			
			public void run() {
				ActiveMQDestination[] ds;
				try {
					ds = broker.getBroker().getDestinations();
					for (ActiveMQDestination d : ds) {
						Destination rd = broker.getBroker().getDestinationMap().get(d);
						if (d!= null && rd != null && rd.getMessageStore() != null) {
							StringBuilder buf = new StringBuilder();
							for(Message m : rd.browse()){
								buf.append(" | "+m.getMessageId()+"---"+m.getProperty(TransactionSupport.NAPOLI_MSG_PRO_KEY_TX_STATE)+"["+(m.isExpired()?"Expire":"Active")+"]");
							}
							System.out.println("Queue[ "+d.getPhysicalName()+" ]'s contents is "+buf);
						}
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 5000, 5000);
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

	public void close() {
		try {
			logTimer.cancel();
			broker.stop();
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
		plugins = null;
		address = null;
	}

	public final BrokerService createBrokerService(String url, BrokerPlugin[] plugins) throws Exception {
		BrokerService embeded = new BrokerService();
		embeded.addConnector("tcp://" + url);
		embeded.setUseJmx(false);
		embeded.setPlugins(plugins);
		embeded.setPersistent(true);
		embeded.setDeleteAllMessagesOnStartup(true);
		embeded.setDataDirectory(dataDir);
		embeded.start();
		return embeded;
	}

}
