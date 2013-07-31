package com.alibaba.napoli.common;

import com.alibaba.napoli.receiver.RedeliveryStrategy;

import javax.jms.Session;

/**
 * User: heyman Date: 12/15/11 Time: 1:24 下午
 */
public class ConnectionParam {
	private String name;
    
    private String storePath;
    
	
	private boolean transacted = false;
	private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
	private String messageSelector = "";
	private RedeliveryStrategy redeliveryStrategy;

	private int prefetch = 5;

	private int sendPoolsize = 5;
	private int sendTimeout = 1000;
	private int connectionTimeout = 2000;

	private String jmsUserName = "napoli";
	private String jmsPassword = "napoli";
	private int receiverSessions = 1;

	private int idlePeriod = 5 * 60 * 1000;
    private boolean storeEnable = true;
    
    private String filterChain;

    private int reprocessNum = 0;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	

	public boolean isTransacted() {
		return transacted;
	}

	public void setTransacted(boolean transacted) {
		this.transacted = transacted;
	}

	public int getAcknowledgeMode() {
		return acknowledgeMode;
	}

	public void setAcknowledgeMode(int acknowledgeMode) {
		this.acknowledgeMode = acknowledgeMode;
	}

	public String getMessageSelector() {
		return messageSelector;
	}

	public void setMessageSelector(String messageSelector) {
		this.messageSelector = messageSelector;
	}

	public RedeliveryStrategy getRedeliveryStrategy() {
		return redeliveryStrategy;
	}

	public void setRedeliveryStrategy(RedeliveryStrategy redeliveryStrategy) {
		this.redeliveryStrategy = redeliveryStrategy;
	}

	public int getPrefetch() {
		return prefetch;
	}

	public void setPrefetch(int prefetch) {
		if (prefetch < 5) {
			this.prefetch = 5;
		} else {
			this.prefetch = prefetch;
		}
	}

	public int getSendPoolsize() {
		return sendPoolsize;
	}

	public void setSendPoolsize(int sendPoolsize) {
		this.sendPoolsize = sendPoolsize;
	}

	public int getSendTimeout() {
		return sendTimeout;
	}

	public void setSendTimeout(int sendTimeout) {
		this.sendTimeout = sendTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public String getJmsUserName() {
		return jmsUserName;
	}

	public void setJmsUserName(String jmsUserName) {
		this.jmsUserName = jmsUserName;
	}

	public String getJmsPassword() {
		return jmsPassword;
	}

	public void setJmsPassword(String jmsPassword) {
		this.jmsPassword = jmsPassword;
	}

	public int getReceiverSessions() {
		return receiverSessions;
	}

	public void setReceiverSessions(int receiverSessions) {
		this.receiverSessions = receiverSessions;
	}

	public int getIdlePeriod() {
		return idlePeriod;
	}

	public void setIdlePeriod(int idlePeriod) {
		this.idlePeriod = idlePeriod;
	}

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public boolean isStoreEnable() {
        return storeEnable;
    }

    public void setStoreEnable(boolean storeEnable) {
        this.storeEnable = storeEnable;
    }

    public String getFilterChain() {
        return filterChain;
    }

    public void setFilterChain(String filterChain) {
        this.filterChain = filterChain;
    }

    public int getReprocessNum() {
        return reprocessNum;
    }

    public void setReprocessNum(int reprocessNum) {
        this.reprocessNum = reprocessNum;
    }
}
