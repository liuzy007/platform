package com.alibaba.napoli.metamorphosis.client;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.gecko.service.Connection;
import com.alibaba.napoli.gecko.service.RequestProcessor;
import com.alibaba.napoli.metamorphosis.Message;
import com.alibaba.napoli.metamorphosis.client.consumer.SimpleMessageConsumer;
import com.alibaba.napoli.metamorphosis.exception.InvalidMessageException;
import com.alibaba.napoli.metamorphosis.network.PrioritizeMessageCommand;
import com.alibaba.napoli.metamorphosis.utils.MessageUtils;
import com.alibaba.napoli.metamorphosis.utils.MessageUtils.DecodedMessage;
import com.alibaba.napoli.metamorphosis.utils.NamedThreadFactory;

public class PriorityMessageManager implements
		RequestProcessor<PrioritizeMessageCommand> {

	static final Log log = LogFactory.getLog(PriorityMessageManager.class);
	private final MetaMessageSessionFactory factory;
	
	private final  ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 60,
	        TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),new NamedThreadFactory("PriorityMessageManager"));

	public PriorityMessageManager(MetaMessageSessionFactory factory) {
		this.factory = factory;
	}

	@Override
	public void handleRequest(PrioritizeMessageCommand request, Connection conn) {
		
		String topic = request.getTopic();
		byte[] data=request.getData();
		DecodedMessage dc;
		try {
			dc = MessageUtils.decodeMessage(topic, data, 0);
			Message message = dc.message;
			if(message == null || factory == null)
				return ;
			for(Object child:factory.getChildren()){
				if(child instanceof SimpleMessageConsumer){
					SimpleMessageConsumer consumer = (SimpleMessageConsumer)child;
					if(consumer.canConsumer(request)){
						consumer.consumePriorityMessage(message);
					}
				}
			}
		} catch (InvalidMessageException e) {
			log.error(e.getMessage(),e);
		}
		
		
	}

	@Override
	public ThreadPoolExecutor getExecutor() {
		return threadPoolExecutor;
	}

}
