/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Authors:
 *   wuhua <wq163@163.com> , boyan <killme2008@gmail.com>
 */
package com.alibaba.napoli.metamorphosis.client.consumer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.gecko.core.command.ResponseCommand;
import com.alibaba.napoli.gecko.core.util.OpaqueGenerator;
import com.alibaba.napoli.metamorphosis.Message;
import com.alibaba.napoli.metamorphosis.client.MetaMessageSessionFactory;
import com.alibaba.napoli.metamorphosis.client.RemotingClientWrapper;
import com.alibaba.napoli.metamorphosis.client.consumer.SimpleFetchManager.FetchRequestRunner;
import com.alibaba.napoli.metamorphosis.client.consumer.storage.OffsetStorage;
import com.alibaba.napoli.metamorphosis.client.producer.ProducerZooKeeper;
import com.alibaba.napoli.metamorphosis.cluster.Broker;
import com.alibaba.napoli.metamorphosis.cluster.Partition;
import com.alibaba.napoli.metamorphosis.exception.MetaClientException;
import com.alibaba.napoli.metamorphosis.exception.MetaOpeartionTimeoutException;
import com.alibaba.napoli.metamorphosis.filter.Filter;
import com.alibaba.napoli.metamorphosis.filter.FilterImpl;
import com.alibaba.napoli.metamorphosis.network.BooleanCommand;
import com.alibaba.napoli.metamorphosis.network.DataCommand;
import com.alibaba.napoli.metamorphosis.network.GetCommand;
import com.alibaba.napoli.metamorphosis.network.HttpStatus;
import com.alibaba.napoli.metamorphosis.network.OffsetCommand;
import com.alibaba.napoli.metamorphosis.network.PrioritizeMessageCommand;
import com.alibaba.napoli.metamorphosis.utils.MetaStatLog;
import com.alibaba.napoli.metamorphosis.utils.StatConstants;

/**
 * 消息消费者基类
 * 
 * @author boyan
 * @Date 2011-4-23
 * @author wuhua
 * @Date 2011-6-26
 * 
 */
public class SimpleMessageConsumer implements MessageConsumer, InnerConsumer {
	private static final int DEFAULT_OP_TIMEOUT = 10000;

	static final Log log = LogFactory.getLog(FetchRequestRunner.class);

	private final RemotingClientWrapper remotingClient;

	private final ConsumerConfig consumerConfig;

	private final ConsumerZooKeeper consumerZooKeeper;

	private final MetaMessageSessionFactory messageSessionFactory;

	private final OffsetStorage offsetStorage;

	private final LoadBalanceStrategy loadBalanceStrategy;

	private final ProducerZooKeeper producerZooKeeper;

	private final ScheduledExecutorService scheduledExecutorService;

	private final SubscribeInfoManager subscribeInfoManager;

	private final RecoverManager recoverStorageManager;

	private final ConcurrentHashMap<String/* topic */, SubscriberInfo> topicSubcriberRegistry = new ConcurrentHashMap<String, SubscriberInfo>();

	private FetchManager fetchManager;
	
	private Filter filter;

	public SimpleMessageConsumer(
			final MetaMessageSessionFactory messageSessionFactory,
			final RemotingClientWrapper remotingClient,
			final ConsumerConfig consumerConfig,
			final ConsumerZooKeeper consumerZooKeeper,
			final ProducerZooKeeper producerZooKeeper,
			final SubscribeInfoManager subscribeInfoManager,
			final RecoverManager recoverManager,
			final OffsetStorage offsetStorage,
			final LoadBalanceStrategy loadBalanceStrategy) {
		super();
		this.messageSessionFactory = messageSessionFactory;
		this.remotingClient = remotingClient;
		this.consumerConfig = consumerConfig;
		this.producerZooKeeper = producerZooKeeper;
		this.consumerZooKeeper = consumerZooKeeper;
		this.offsetStorage = offsetStorage;
		this.subscribeInfoManager = subscribeInfoManager;
		this.recoverStorageManager = recoverManager;
		this.fetchManager = new SimpleFetchManager(consumerConfig, this);
		this.scheduledExecutorService = Executors
				.newSingleThreadScheduledExecutor();
		this.loadBalanceStrategy = loadBalanceStrategy;
		this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				SimpleMessageConsumer.this.consumerZooKeeper
						.commitOffsets(SimpleMessageConsumer.this.fetchManager);
			}
		}, consumerConfig.getCommitOffsetPeriodInMills(), consumerConfig
				.getCommitOffsetPeriodInMills(), TimeUnit.MILLISECONDS);
		createFilter(consumerConfig);
	}
	
	private void createFilter(ConsumerConfig consumerConfig){
		if(consumerConfig == null || StringUtils.isBlank(consumerConfig.getMessageSelector()))
			return;
		try {
			filter = FilterImpl.createFilter(consumerConfig.getMessageSelector());
		} catch (Throwable e) {
			log.error(e);
		}
	}

	FetchManager getFetchManager() {
		return this.fetchManager;
	}

	void setFetchManager(final FetchManager fetchManager) {
		this.fetchManager = fetchManager;
	}

	ConcurrentHashMap<String, SubscriberInfo> getTopicSubcriberRegistry() {
		return this.topicSubcriberRegistry;
	}

	@Override
	public OffsetStorage getOffsetStorage() {
		return this.offsetStorage;
	}

	@Override
	public synchronized void shutdown() throws MetaClientException {
		consumerdPriorityMessages.clear();
		if (this.fetchManager.isShutdown()) {
			return;
		}
		try {
			this.fetchManager.stopFetchRunner();
			this.consumerZooKeeper.unRegisterConsumer(this.fetchManager);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			this.scheduledExecutorService.shutdownNow();
			this.offsetStorage.close();
			// 删除本组的订阅关系
			this.subscribeInfoManager.removeGroup(this.consumerConfig
					.getGroup());
			this.messageSessionFactory.removeChild(this);
		}

	}

	@Override
	public MessageConsumer subscribe(final String topic, final int maxSize,
			final MessageListener messageListener) throws MetaClientException {
		this.checkState();
		if (StringUtils.isBlank(topic)) {
			throw new IllegalArgumentException("Blank topic");
		}
		if (messageListener == null) {
			throw new IllegalArgumentException("Null messageListener");
		}
		// 先添加到公共管理器
		this.subscribeInfoManager.subscribe(topic,
				this.consumerConfig.getGroup(), maxSize, messageListener);
		// 然后添加到自身的管理器
		SubscriberInfo info = this.topicSubcriberRegistry.get(topic);
		if (info == null) {
			info = new SubscriberInfo(messageListener, maxSize);
			final SubscriberInfo oldInfo = this.topicSubcriberRegistry
					.putIfAbsent(topic, info);
			if (oldInfo != null) {
				throw new MetaClientException("Topic=" + topic
						+ " has been subscribered");
			}
			return this;
		} else {
			throw new MetaClientException("Topic=" + topic
					+ " has been subscribered");
		}
	}

	@Override
	public void appendCouldNotProcessMessage(final Message message)
			throws IOException {
		// 目前的处理是交给本地存储管理并重试
		log.warn("Message could not process,save to local.MessageId="
				+ message.getId() + ",Topic=" + message.getTopic()
				+ ",Partition=" + message.getPartition());
		this.recoverStorageManager.append(this.consumerConfig.getGroup(),
				message);
	}

	private void checkState() {
		if (this.fetchManager.isShutdown()) {
			throw new IllegalStateException("Consumer has been shutdown");
		}
	}

	@Override
	public void completeSubscribe() throws MetaClientException {
		this.checkState();
		try {
			this.consumerZooKeeper.registerConsumer(this.consumerConfig,
					this.fetchManager, this.topicSubcriberRegistry,
					this.offsetStorage, this.loadBalanceStrategy);
		} catch (final Exception e) {
			throw new MetaClientException("注册订阅者失败", e);
		}
	}

	@Override
	public MessageListener getMessageListener(final String topic) {
		final SubscriberInfo info = this.topicSubcriberRegistry.get(topic);
		if (info == null) {
			return null;
		}
		return info.getMessageListener();
	}

	@Override
	public long offset(final FetchRequest fetchRequest)
			throws MetaClientException {
		final long start = System.currentTimeMillis();
		boolean success = false;
		try {
			final long currentOffset = fetchRequest.getOffset();
			final OffsetCommand offsetCmd = new OffsetCommand(
					fetchRequest.getTopic(), this.consumerConfig.getGroup(),
					fetchRequest.getPartition(), currentOffset,
					OpaqueGenerator.getNextOpaque());
			final String serverUrl = fetchRequest.getBroker().getZKString();
			final BooleanCommand booleanCmd = (BooleanCommand) this.remotingClient
					.invokeToGroup(serverUrl, offsetCmd,
							this.consumerConfig.getFetchTimeoutInMills(),
							TimeUnit.MILLISECONDS);
			switch (booleanCmd.getCode()) {
			case HttpStatus.Success:
				success = true;
				return Long.parseLong(booleanCmd.getErrorMsg());
			default:
				throw new MetaClientException(booleanCmd.getErrorMsg());
			}
		} catch (final MetaClientException e) {
			throw e;
		} catch (final TimeoutException e) {
			throw new MetaOpeartionTimeoutException("Send message timeout in "
					+ this.consumerConfig.getFetchTimeoutInMills() + " mills");
		} catch (final Exception e) {
			throw new MetaClientException("get offset failed,topic="
					+ fetchRequest.getTopic() + ",partition="
					+ fetchRequest.getPartition() + ",current offset="
					+ fetchRequest.getOffset(), e);
		} finally {
			final long duration = System.currentTimeMillis() - start;
			if (duration > 200) {
				MetaStatLog.addStatValue2(null, StatConstants.OFFSET_TIME_STAT,
						fetchRequest.getTopic(), duration);
			}
			if (!success) {
				MetaStatLog.addStat(null, StatConstants.OFFSET_FAILED_STAT,
						fetchRequest.getTopic());
			}
		}
	}

	@Override
	public MessageIterator fetch(final FetchRequest fetchRequest, long timeout,
			TimeUnit timeUnit) throws MetaClientException, InterruptedException {
		if (timeout <= 0 || timeUnit == null) {
			timeout = this.consumerConfig.getFetchTimeoutInMills();
			timeUnit = TimeUnit.MILLISECONDS;
		}
		final long start = System.currentTimeMillis();
		boolean success = false;
		try {
			final long currentOffset = fetchRequest.getOffset();
			final GetCommand getCmd = new GetCommand(fetchRequest.getTopic(),
					this.consumerConfig.getGroup(),
					fetchRequest.getPartition(), currentOffset,
					fetchRequest.getMaxSize(), OpaqueGenerator.getNextOpaque());
			final String serverUrl = fetchRequest.getBroker().getZKString();
			final ResponseCommand response = this.remotingClient.invokeToGroup(
					serverUrl, getCmd, timeout, timeUnit);
			if (response instanceof DataCommand) {
				final DataCommand dataCmd = (DataCommand) response;
				final byte[] data = dataCmd.getData();
				// 获取的数据严重不足的时候，缩减maxSize
				if (data.length < fetchRequest.getMaxSize() / 2) {
					fetchRequest.decreaseMaxSize();
				}
				success = true;
				return new MessageIterator(fetchRequest.getTopic(), data);
			} else {
				final BooleanCommand booleanCmd = (BooleanCommand) response;
				switch (booleanCmd.getCode()) {
				case HttpStatus.NotFound:
					success = true;
					return null;
				case HttpStatus.Forbidden:
					success = true;
					return null;
				case HttpStatus.Moved:
					success = true;
					fetchRequest.resetRetries();
					fetchRequest.setOffset(
							Long.parseLong(booleanCmd.getErrorMsg()), -1, true);
					return null;
				default:
					throw new MetaClientException(
							((BooleanCommand) response).getErrorMsg());
				}
			}

		} catch (final TimeoutException e) {
			throw new MetaOpeartionTimeoutException("Send message timeout in "
					+ this.consumerConfig.getFetchTimeoutInMills() + " mills");
		} catch (final MetaClientException e) {
			throw e;
		} catch (final InterruptedException e) {
			throw e;
		} catch (final Exception e) {
			throw new MetaClientException("get message failed,topic="
					+ fetchRequest.getTopic() + ",partition="
					+ fetchRequest.getPartition() + ",offset="
					+ fetchRequest.getOffset(), e);
		} finally {
			final long duration = System.currentTimeMillis() - start;
			if (duration > 200) {
				MetaStatLog.addStatValue2(null, StatConstants.GET_TIME_STAT,
						fetchRequest.getTopic(), duration);
			}
			if (!success) {
				MetaStatLog.addStat(null, StatConstants.GET_FAILED_STAT,
						fetchRequest.getTopic());
			}
		}
	}

	@Override
	public void setSubscriptions(final Collection<Subscription> subscriptions)
			throws MetaClientException {
		if (subscriptions == null) {
			return;
		}
		for (final Subscription subscription : subscriptions) {
			this.subscribe(subscription.getTopic(), subscription.getMaxSize(),
					subscription.getMessageListener());
		}
	}

	@Override
	public MessageIterator get(final String topic, final Partition partition,
			final long offset, final int maxSize, final long timeout,
			final TimeUnit timeUnit) throws MetaClientException,
			InterruptedException {
		this.producerZooKeeper.publishTopic(topic);
		final Broker broker = new Broker(partition.getBrokerId(),
				this.producerZooKeeper.selectBroker(topic, partition));
		final TopicPartitionRegInfo topicPartitionRegInfo = new TopicPartitionRegInfo(
				topic, partition, offset);
		return this.fetch(new FetchRequest(broker, 0, topicPartitionRegInfo,
				maxSize), timeout, timeUnit);
	}

	@Override
	public ConsumerConfig getConsumerConfig() {
		return this.consumerConfig;
	}

	@Override
	public MessageIterator get(final String topic, final Partition partition,
			final long offset, final int maxSize) throws MetaClientException,
			InterruptedException {
		return this.get(topic, partition, offset, maxSize, DEFAULT_OP_TIMEOUT,
				TimeUnit.MILLISECONDS);
	}
	
	private final Set<Long> consumerdPriorityMessages = new HashSet<Long>();
	
	public void consumePriorityMessage(Message message){
		if(message == null)
			return;
		String topic = message.getTopic();
		if(StringUtils.isBlank(topic))
			return;
		MessageListener listener = getMessageListener(topic);
		if(listener == null)
			return;
		if(consumerdPriorityMessages.contains(message.getId())){
			log.warn("Message["+message.getId()+"] already be consumed, it's a duplicate priority message! ");
			return;
		}
		listener.recieveMessages(message);
		consumerdPriorityMessages.add(message.getId());
	}
	
	public Set<Long> getConsumerdPriorityMessages() {
		return consumerdPriorityMessages;
	}

	public boolean canConsumer(PrioritizeMessageCommand request){
		String topic = request.getTopic();
		int brokerId  = request.getBrokerId();
		int partition = request.getPartition();
		return fetchManager.canConsume(topic, brokerId, partition);
	}
	
	public boolean canConsumer(Message message){
		if(message == null || StringUtils.isBlank(message.getId()+"")){
			return false;
		}
		if(getConsumerdPriorityMessages().contains(message.getId())){
			return false;
		}
		if(filter != null){
			return filter.match(message);
		}
		return true;
	}
	
	

}