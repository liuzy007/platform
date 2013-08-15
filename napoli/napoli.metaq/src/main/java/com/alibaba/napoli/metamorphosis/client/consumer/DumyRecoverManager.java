package com.alibaba.napoli.metamorphosis.client.consumer;

import java.io.IOException;

import com.alibaba.napoli.metamorphosis.Message;
import com.alibaba.napoli.metamorphosis.client.MetaClientConfig;
import com.alibaba.napoli.metamorphosis.exception.MetaClientException;

public class DumyRecoverManager extends AbstractRecoverManager {

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void start(MetaClientConfig metaClientConfig) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void append(String group, Message message) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() throws MetaClientException {
		// TODO Auto-generated method stub
		
	}

}
