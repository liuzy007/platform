package com.alibaba.napoli.receiver;

import com.alibaba.napoli.client.async.RedeliveryCallback;

public class RedeliveryStrategy {
	private RedeliveryCallback 	redeliveryCallback;
	private short 				redeliveryMultiplier = 5;
	private boolean 			redeliveryExponential = false;
	private int 				maxRedeliveries = -1;
	private int 				initialRedeliveryDelay = 1000;

	public RedeliveryCallback getRedeliveryCallback() {
		return redeliveryCallback;
	}

	public void setRedeliveryCallback(RedeliveryCallback redeliveryCallback) {
		this.redeliveryCallback = redeliveryCallback;
	}

	public short getRedeliveryMultiplier() {
		return redeliveryMultiplier;
	}

	public void setRedeliveryMultiplier(short redeliveryMultiplier) {
		this.redeliveryMultiplier = redeliveryMultiplier;
	}

	public boolean isRedeliveryExponential() {
		return redeliveryExponential;
	}

	public void setRedeliveryExponential(boolean redeliveryExponential) {
		this.redeliveryExponential = redeliveryExponential;
	}

	public int getMaxRedeliveries() {
		return maxRedeliveries;
	}

	public void setMaxRedeliveries(int maxRedeliveries) {
		this.maxRedeliveries = maxRedeliveries;
	}

	public int getInitialRedeliveryDelay() {
		return initialRedeliveryDelay;
	}

	public void setInitialRedeliveryDelay(int initialRedeliveryDelay) {
		this.initialRedeliveryDelay = initialRedeliveryDelay;
	}

}
