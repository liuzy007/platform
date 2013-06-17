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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.alibaba.napoli.gecko.service.exception.NotifyRemotingException;
import com.alibaba.napoli.metamorphosis.client.RemotingClientWrapper;
import com.alibaba.napoli.metamorphosis.client.ZkClientChangedListener;
import com.alibaba.napoli.metamorphosis.client.consumer.storage.OffsetStorage;
import com.alibaba.napoli.metamorphosis.cluster.Broker;
import com.alibaba.napoli.metamorphosis.cluster.Cluster;
import com.alibaba.napoli.metamorphosis.cluster.Partition;
import com.alibaba.napoli.metamorphosis.exception.MetaClientException;
import com.alibaba.napoli.metamorphosis.network.RemotingUtils;
import com.alibaba.napoli.metamorphosis.utils.MetaZookeeper;
import com.alibaba.napoli.metamorphosis.utils.MetaZookeeper.ZKGroupDirs;
import com.alibaba.napoli.metamorphosis.utils.MetaZookeeper.ZKGroupTopicDirs;
import com.alibaba.napoli.metamorphosis.utils.MetaZookeeper.ZKTopicRightDirs;
import com.alibaba.napoli.metamorphosis.utils.MetaZookeeperHelper;
import com.alibaba.napoli.metamorphosis.utils.ZkUtils;
import com.alibaba.napoli.metamorphosis.utils.ZkUtils.ZKConfig;

/**
 * Consumer与Zookeeper交互
 * 
 * @author boyan
 * @Date 2011-4-26
 * @author wuhua
 * @Date 2011-6-26
 */
public class ConsumerZooKeeper implements ZkClientChangedListener {
	protected ZkClient zkClient;
	protected final ConcurrentHashMap<FetchManager, FutureTask<ZKLoadRebalanceListener>> consumerLoadBalanceListeners = new ConcurrentHashMap<FetchManager, FutureTask<ZKLoadRebalanceListener>>();
	private final RemotingClientWrapper remotingClient;
	private final ZKConfig zkConfig;
	protected final MetaZookeeper metaZookeeper;

	public ConsumerZooKeeper(final MetaZookeeper metaZookeeper,
			final RemotingClientWrapper remotingClient,
			final ZkClient zkClient, final ZKConfig zkConfig) {
		super();
		this.metaZookeeper = metaZookeeper;
		this.zkClient = zkClient;
		this.remotingClient = remotingClient;
		this.zkConfig = zkConfig;
	}

	public void commitOffsets(final FetchManager fetchManager) {
		final ZKLoadRebalanceListener listener = this
				.getBrokerConnectionListener(fetchManager);
		if (listener != null) {
			listener.commitOffsets();
		}
	}

	ZKLoadRebalanceListener getBrokerConnectionListener(
			final FetchManager fetchManager) {
		final FutureTask<ZKLoadRebalanceListener> task = this.consumerLoadBalanceListeners
				.get(fetchManager);
		if (task != null) {
			try {
				return task.get();
			} catch (final Exception e) {
				log.error("获取ZKLoadRebalanceListener失败", e);
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * 取消注册consumer
	 * 
	 * @param fetchManager
	 */
	public void unRegisterConsumer(final FetchManager fetchManager) {
		try {
			final FutureTask<ZKLoadRebalanceListener> futureTask = this.consumerLoadBalanceListeners
					.remove(fetchManager);
			if (futureTask != null) {
				final ZKLoadRebalanceListener listener = futureTask.get();
				if (listener != null) {
					// 提交offsets
					listener.commitOffsets();
					this.zkClient
							.unsubscribeStateChanges(new ZKSessionExpireListenner(
									listener));
					final ZKGroupDirs dirs = this.metaZookeeper.new ZKGroupDirs(
							listener.consumerConfig.getGroup());
					this.zkClient.unsubscribeChildChanges(
							dirs.consumerRegistryDir, listener);
					log.info("unsubscribeChildChanges:"
							+ dirs.consumerRegistryDir);
					// 移除监视订阅topic的分区变化
					for (final String topic : listener.topicSubcriberRegistry
							.keySet()) {
						final String partitionPath = this.metaZookeeper.brokerTopicsPath
								+ "/" + topic;
						this.zkClient.unsubscribeChildChanges(partitionPath,
								listener);
						log.info("unsubscribeChildChanges:" + partitionPath);
					}
					// 删除ownership
					listener.releaseAllPartitionOwnership();
					// 删除临时节点
					ZkUtils.deletePath(this.zkClient,
							listener.dirs.consumerRegistryDir + "/"
									+ listener.consumerIdString);
				}
			}
		} catch (final InterruptedException e) {
			Thread.interrupted();
			log.error("Interrupted when unRegisterConsumer", e);
		} catch (final Exception e) {
			log.error(
					"Error in unRegisterConsumer,maybe error when registerConsumer",
					e);
		}
	}

	/**
	 * 注册订阅者
	 * 
	 * @throws Exception
	 */
	public void registerConsumer(
			final ConsumerConfig consumerConfig,
			final FetchManager fetchManager,
			final ConcurrentHashMap<String/* topic */, SubscriberInfo> topicSubcriberRegistry,
			final OffsetStorage offsetStorage,
			final LoadBalanceStrategy loadBalanceStrategy) throws Exception {

		final FutureTask<ZKLoadRebalanceListener> task = new FutureTask<ZKLoadRebalanceListener>(
				new Callable<ZKLoadRebalanceListener>() {

					@Override
					public ZKLoadRebalanceListener call() throws Exception {
						final ZKGroupDirs dirs = ConsumerZooKeeper.this.metaZookeeper.new ZKGroupDirs(
								consumerConfig.getGroup());
						final String consumerUUID = ConsumerZooKeeper.this
								.getConsumerUUID(consumerConfig);
						final String consumerUUIDString = consumerConfig
								.getGroup() + "_" + consumerUUID;
						final ZKLoadRebalanceListener loadBalanceListener = new ZKLoadRebalanceListener(
								fetchManager, dirs, consumerUUIDString,
								consumerConfig, offsetStorage,
								topicSubcriberRegistry, loadBalanceStrategy);
						return ConsumerZooKeeper.this
								.registerConsumerInternal(loadBalanceListener);
					}

				});
		final FutureTask<ZKLoadRebalanceListener> existsTask = this.consumerLoadBalanceListeners
				.putIfAbsent(fetchManager, task);
		if (existsTask == null) {
			task.run();
		} else {
			throw new MetaClientException("Consumer has been already registed");
		}

	}

	protected ZKLoadRebalanceListener registerConsumerInternal(
			final ZKLoadRebalanceListener loadBalanceListener)
			throws UnknownHostException, InterruptedException, Exception {
		final ZKGroupDirs dirs = this.metaZookeeper.new ZKGroupDirs(
				loadBalanceListener.consumerConfig.getGroup());

		final String topicString = this
				.getTopicsString(loadBalanceListener.topicSubcriberRegistry);

		if (this.zkClient == null) {
			// 直连模式
			loadBalanceListener.fetchManager.stopFetchRunner();
			loadBalanceListener.fetchManager.resetFetchState();
			// zkClient为null，使用配置项并发起fetch请求
			for (final String topic : loadBalanceListener.topicSubcriberRegistry
					.keySet()) {
				final SubscriberInfo subInfo = loadBalanceListener.topicSubcriberRegistry
						.get(topic);
				ConcurrentHashMap<Partition, TopicPartitionRegInfo> topicPartRegInfoMap = loadBalanceListener.topicRegistry
						.get(topic);
				if (topicPartRegInfoMap == null) {
					topicPartRegInfoMap = new ConcurrentHashMap<Partition, TopicPartitionRegInfo>();
					loadBalanceListener.topicRegistry.put(topic,
							topicPartRegInfoMap);
				}
				final Partition partition = new Partition(
						loadBalanceListener.consumerConfig.getPartition());
				final long offset = loadBalanceListener.consumerConfig
						.getOffset();
				final TopicPartitionRegInfo regInfo = new TopicPartitionRegInfo(
						topic, partition, offset);
				topicPartRegInfoMap.put(partition, regInfo);
				loadBalanceListener.fetchManager
						.addFetchRequest(new FetchRequest(new Broker(0,
								loadBalanceListener.consumerConfig
										.getServerUrl()), 0L, regInfo, subInfo
								.getMaxSize()));
			}
			loadBalanceListener.fetchManager.startFetchRunner();
		} else {

			// 注册consumer id
			ZkUtils.createEphemeralPathExpectConflict(this.zkClient,
					dirs.consumerRegistryDir + "/"
							+ loadBalanceListener.consumerIdString, topicString);
			// 监视同一个分组的consumer列表是否有变化
			this.zkClient.subscribeChildChanges(dirs.consumerRegistryDir,
					loadBalanceListener);

			// 监视订阅topic的分区是否有变化
			for (final String topic : loadBalanceListener.topicSubcriberRegistry
					.keySet()) {
				final String partitionPath = this.metaZookeeper.brokerTopicsPath
						+ "/" + topic;
				ZkUtils.makeSurePersistentPathExists(this.zkClient,
						partitionPath);
				this.zkClient.subscribeChildChanges(partitionPath,
						loadBalanceListener);
				ZKTopicRightDirs rightDirs = this.metaZookeeper.new ZKTopicRightDirs(topic);
				this.zkClient.subscribeChildChanges(rightDirs.infoTopicReadRightDir , loadBalanceListener);

			}
			this.zkClient.subscribeStateChanges(new ZKSessionExpireListenner(
					loadBalanceListener));

			// 第一次，需要明确触发balance
			loadBalanceListener.syncedRebalance(false);
		}
		return loadBalanceListener;
	}

	private String getTopicsString(
			final ConcurrentHashMap<String/* topic */, SubscriberInfo> topicSubcriberRegistry) {
		final StringBuilder topicSb = new StringBuilder();
		boolean wasFirst = true;
		for (final String topic : topicSubcriberRegistry.keySet()) {
			if (wasFirst) {
				wasFirst = false;
				topicSb.append(topic);
			} else {
				topicSb.append(",").append(topic);
			}
		}
		return topicSb.toString();
	}

	private final AtomicInteger counter = new AtomicInteger(0);

	protected String getConsumerUUID(final ConsumerConfig consumerConfig)
			throws Exception {
		String consumerUUID = null;
		if (consumerConfig.getConsumerId() != null) {
			consumerUUID = consumerConfig.getConsumerId();
		} else {
			consumerUUID = RemotingUtils.getLocalAddress() + "-"
					+ System.currentTimeMillis() + "-"
					+ this.counter.incrementAndGet();
		}
		return consumerUUID;
	}

	@Override
	public void onZkClientChanged(final ZkClient newClient) {
		this.zkClient = newClient;
		// 重新注册consumer
		for (final FutureTask<ZKLoadRebalanceListener> task : this.consumerLoadBalanceListeners
				.values()) {
			try {
				final ZKLoadRebalanceListener listener = task.get();
				// 要清空已有的注册信息，防止在注册consumer失败的时候还提交offset，导致覆盖更新的offset
				listener.topicRegistry.clear();
				log.info("re-register consumer to zk,group="
						+ listener.consumerConfig.getGroup());
				this.registerConsumerInternal(listener);
			} catch (final Exception e) {
				log.error("reRegister consumer failed", e);
			}
		}

	}

	class ZKSessionExpireListenner implements IZkStateListener {
		private final String consumerIdString;
		private final ZKLoadRebalanceListener loadBalancerListener;

		public ZKSessionExpireListenner(
				final ZKLoadRebalanceListener loadBalancerListener) {
			super();
			this.consumerIdString = loadBalancerListener.consumerIdString;
			this.loadBalancerListener = loadBalancerListener;
		}

		@Override
		public void handleNewSession() throws Exception {
			/**
			 * When we get a SessionExpired event, we lost all ephemeral nodes
			 * and zkclient has reestablished a connection for us. We need to
			 * release the ownership of the current consumer and re-register
			 * this consumer in the consumer registry and trigger a rebalance.
			 */
			;
			log.info("ZK expired; release old broker parition ownership; re-register consumer "
					+ this.consumerIdString);
			this.loadBalancerListener.resetState();
			ConsumerZooKeeper.this
					.registerConsumerInternal(this.loadBalancerListener);
			;
			// explicitly trigger load balancing for this consumer
			this.loadBalancerListener.syncedRebalance(false);

		}

		@Override
		public void handleStateChanged(final KeeperState state)
				throws Exception {
			// do nothing, since zkclient will do reconnect for us.

		}

		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof ZKSessionExpireListenner)) {
				return false;
			}
			final ZKSessionExpireListenner other = (ZKSessionExpireListenner) obj;
			return this.loadBalancerListener.equals(other.loadBalancerListener);
		}

		@Override
		public int hashCode() {
			return this.loadBalancerListener.hashCode();
		}

	}

	static final Log log = LogFactory.getLog(ConsumerZooKeeper.class);

	protected class ZKLoadRebalanceListener implements IZkChildListener {
		private final ZKGroupDirs dirs;

		private final String group;

		protected final String consumerIdString;

		static final int MAX_N_RETRIES = 5;

		private final LoadBalanceStrategy loadBalanceStrategy;

		Map<String, List<String>> oldConsumersPerTopicMap = new HashMap<String, List<String>>();

		Map<String, List<String>> oldPartitionsPerTopicMap = new HashMap<String, List<String>>();

		private boolean isBrokerChange = false;

		private final Lock rebalanceLock = new ReentrantLock();

		/**
		 * 订阅的topic对应的broker,offset等信息
		 */
		final ConcurrentHashMap<String/* topic */, ConcurrentHashMap<Partition, TopicPartitionRegInfo>> topicRegistry = new ConcurrentHashMap<String, ConcurrentHashMap<Partition, TopicPartitionRegInfo>>();

		/**
		 * 订阅信息，如最大传输大小，消息监听器等
		 */
		private final ConcurrentHashMap<String/* topic */, SubscriberInfo> topicSubcriberRegistry;

		private final ConsumerConfig consumerConfig;

		private final OffsetStorage offsetStorage;

		private final FetchManager fetchManager;

		Set<Broker> oldBrokerSet = new HashSet<Broker>();
		private Cluster oldCluster = new Cluster();

		public ZKLoadRebalanceListener(
				final FetchManager fetchManager,
				final ZKGroupDirs dirs,
				final String consumerIdString,
				final ConsumerConfig consumerConfig,
				final OffsetStorage offsetStorage,
				final ConcurrentHashMap<String/* topic */, SubscriberInfo> topicSubcriberRegistry,
				final LoadBalanceStrategy loadBalanceStrategy) {
			super();
			this.fetchManager = fetchManager;
			this.dirs = dirs;
			this.consumerIdString = consumerIdString;
			this.group = consumerConfig.getGroup();
			this.consumerConfig = consumerConfig;
			this.offsetStorage = offsetStorage;
			this.topicSubcriberRegistry = topicSubcriberRegistry;
			this.loadBalanceStrategy = loadBalanceStrategy;
		}

		/**
		 * 更新offset到zk
		 */
		private void commitOffsets() {
			this.offsetStorage.commitOffset(this.consumerConfig.getGroup(),
					this.getTopicPartitionRegInfos());
		}

		private TopicPartitionRegInfo initTopicPartitionRegInfo(
				final String topic, final String group,
				final Partition partition, final long offset) {
			this.offsetStorage.initOffset(topic, group, partition, offset);
			return new TopicPartitionRegInfo(topic, partition, offset);
		}

		List<TopicPartitionRegInfo> getTopicPartitionRegInfos() {
			final List<TopicPartitionRegInfo> rt = new ArrayList<TopicPartitionRegInfo>();
			for (final ConcurrentHashMap<Partition, TopicPartitionRegInfo> subMap : this.topicRegistry
					.values()) {
				final Collection<TopicPartitionRegInfo> values = subMap
						.values();
				if (values != null) {
					rt.addAll(values);
				}
			}
			return rt;
		}

		/**
		 * 加载offset信息
		 * 
		 * @param topic
		 * @param partition
		 * @return
		 */
		private TopicPartitionRegInfo loadTopicPartitionRegInfo(
				final String topic, final Partition partition) {
			return this.offsetStorage.load(topic,
					this.consumerConfig.getGroup(), partition);
		}

		@Override
		public void handleChildChange(final String parentPath,
				final List<String> currentChilds) throws Exception {
			log.info("consumer watch the path["+parentPath+"] has changed");
			for (final String topic : this.topicSubcriberRegistry
					.keySet()){
				if(StringUtils.endsWith(parentPath, "/"+topic)){
                    log.info("ZZZZZZZZZZZZZZZZZZZZZZZZ"+parentPath);
					this.syncedRebalance(true);
					return;
				}
			}
				
			this.syncedRebalance(false);
		}

		void syncedRebalance(boolean isBrokerChange) throws Exception {
			this.rebalanceLock.lock();
			this.isBrokerChange = isBrokerChange;
			try {
				for (int i = 0; i < MAX_N_RETRIES; i++) {
					log.info("begin rebalancing consumer "
							+ this.consumerIdString + " try #" + i);
					boolean done;
					try {
						done = this.rebalance();
					} catch (final Throwable e) {
						// 发生了预料之外的异常,都重试一下,
						// 有可能是多个机器consumer在同时rebalance造成的读取zk数据不一致,-- wuhua
						log.warn(
								"unexpected exception occured while try rebalancing",
								e);
						done = false;
					}
					log.info("end rebalancing consumer "
							+ this.consumerIdString + " try #" + i);

					if (done) {
						log.info("rebalance success.");
						return;
					} else {
						log.warn("rebalance failed,try #" + i);
					}

					// release all partitions, reset state and retry
					this.releaseAllPartitionOwnership();
					this.resetState();
					// 等待zk数据同步
					Thread.sleep(ConsumerZooKeeper.this.zkConfig.zkSyncTimeMs);
				}
				log.error("rebalance failed,finally");
			} finally {
				this.rebalanceLock.unlock();
			}
		}

		private void resetState() {
			this.topicRegistry.clear();
			this.oldConsumersPerTopicMap.clear();
			this.oldPartitionsPerTopicMap.clear();
		}

		/**
		 * 更新fetch线程
		 * 
		 * @param cluster
		 */
		protected void updateFetchRunner(final Cluster cluster)
				throws Exception {
			this.fetchManager.resetFetchState();
			final Set<Broker> changedBrokers = new HashSet<Broker>();
			for (final Map.Entry<String/* topic */, ConcurrentHashMap<Partition, TopicPartitionRegInfo>> entry : this.topicRegistry
					.entrySet()) {
				final String topic = entry.getKey();
				for (final Map.Entry<Partition, TopicPartitionRegInfo> partEntry : entry
						.getValue().entrySet()) {
					final Partition partition = partEntry.getKey();
					final TopicPartitionRegInfo info = partEntry.getValue();
					// 随机取master或slave的一个读,wuhua
					final Broker broker = cluster.getBrokerRandom(partition
							.getBrokerId());
					if (broker != null) {
						changedBrokers.add(broker);
						final SubscriberInfo subscriberInfo = this.topicSubcriberRegistry
								.get(topic);
						// 添加fetch请求
						this.fetchManager.addFetchRequest(new FetchRequest(
								broker, 0L, info, subscriberInfo.getMaxSize()));
					}
				}
			}

			// 建立连接
			for (final Broker broker : changedBrokers) {
				if (!this.oldBrokerSet.contains(broker)) {
					try {
						ConsumerZooKeeper.this.remotingClient.connect(broker
								.getZKString());
						ConsumerZooKeeper.this.remotingClient
								.awaitReadyInterrupt(broker.getZKString());
						log.info("Connect to " + broker.getZKString());
					} catch (final NotifyRemotingException e) {
						log.error("Connect to " + broker.getZKString()
								+ " failed", e);
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
			// 关闭连接
			for (final Broker broker : this.oldBrokerSet) {
				if (!changedBrokers.contains(broker)) {
					try {
						ConsumerZooKeeper.this.remotingClient.close(
								broker.getZKString(), false);
						log.info("Closing " + broker.getZKString());
					} catch (final NotifyRemotingException e) {
						log.error("Connect to " + broker.getZKString()
								+ " failed", e);
					}
				}
			}
			// 重新启动fetch线程
			log.info("Starting fetch runners");
			this.oldBrokerSet = changedBrokers;
			this.fetchManager.startFetchRunner();
		}

		private Map<String,List<String>> filterPartitionMap(Map<String, List<String>> oldPartitionsPerTopicMap){
			Map<String,List<String>> partitionsPerTopicMap = new HashMap<String,List<String>>();
			if(oldPartitionsPerTopicMap == null || oldPartitionsPerTopicMap.size() == 0)
				return partitionsPerTopicMap;
			MetaZookeeperHelper helper = new MetaZookeeperHelper(metaZookeeper);
			for(String topic:oldPartitionsPerTopicMap.keySet()){
				List<String> brokerIds = helper.getAllReadRightBrokers(topic);
				partitionsPerTopicMap.put(topic, filterPartition(brokerIds,oldPartitionsPerTopicMap.get(topic)));
			}
			return partitionsPerTopicMap;
		}
		
		private List<String> filterPartition(List<String> brokerIds,List<String> oldPartitions){
			
			List<String> partitions = new ArrayList<String>();
			if(brokerIds == null || brokerIds.size() == 0 || oldPartitions == null || oldPartitions.size() == 0)
				return partitions;
			for(String partition:oldPartitions){
				for(String brokerId:brokerIds){
					if(StringUtils.equals(partition, brokerId) || StringUtils.startsWith(partition, brokerId+"-")){
						partitions.add(partition);
						break;
					}
				}
			}
			return partitions;
		}
		
		boolean rebalance() throws Exception {

			final Map<String/* topic */, String/* consumerId */> myConsumerPerTopicMap = this
					.getConsumerPerTopic(this.consumerIdString);
			final Cluster cluster = ConsumerZooKeeper.this.metaZookeeper
					.getCluster();
			Map<String/* topic */, List<String>/* consumer list */> consumersPerTopicMap = null;
			try {
				consumersPerTopicMap = this.getConsumersPerTopic(this.group);
			} catch (final NoNodeException e) {
				// 多个consumer同时在负载均衡时,可能会到达这里 -- wuhua
				log.warn("maybe other consumer is rebalancing now,"
						+ e.getMessage());
				return false;
			}

			Map<String, List<String>> partitionsPerTopicMap = this
					.getPartitionStringsForTopics(myConsumerPerTopicMap);
			partitionsPerTopicMap = filterPartitionMap(partitionsPerTopicMap);

			final Map<String/* topic */, String/* consumer id */> relevantTopicConsumerIdMap = this
					.getRelevantTopicMap(myConsumerPerTopicMap,
							partitionsPerTopicMap,
							this.oldPartitionsPerTopicMap,
							consumersPerTopicMap, this.oldConsumersPerTopicMap);
			// 没有变更，无需平衡
			if (relevantTopicConsumerIdMap.size() <= 0) {
				// 处理主备情况,topic分区和消费者没有变化,但主备的其中一台挂了,
				// 导致partitionsPerTopicMap可能是没有变化的,
				// 所以要检查集群的变化并重新连接
				if (this.checkClusterChange(cluster)) {
					log.info("Stopping fetch runners,maybe master or slave changed");
					this.fetchManager.stopFetchRunner();
					this.updateFetchRunner(cluster);
					this.oldCluster = cluster;
				} else {
					log.info("Consumer " + this.consumerIdString + " with "
							+ consumersPerTopicMap
							+ " doesn't need to be rebalanced.");
				}
				return true;
			}
			log.info("Stopping fetch runners");
			this.fetchManager.stopFetchRunner();
			log.info("Comitting all offsets");
			this.commitOffsets();

			for (final Map.Entry<String, String> entry : relevantTopicConsumerIdMap
					.entrySet()) {
				final String topic = entry.getKey();
				final String consumerId = entry.getValue();

				final ZKGroupTopicDirs topicDirs = ConsumerZooKeeper.this.metaZookeeper.new ZKGroupTopicDirs(
						topic, this.group);
				// 当前该topic的订阅者
				final List<String> curConsumers = consumersPerTopicMap
						.get(topic);
				// 当前该topic的分区
				final List<String> curPartitions = partitionsPerTopicMap
						.get(topic);

				if (curConsumers == null) {
					log.info("Releasing partition ownerships for topic:"
							+ topic);
					this.releasePartitionOwnership(topic);
					this.topicRegistry.remove(topic);
					log.info("There are no consumers subscribe topic " + topic);
					continue;
				}
				if (curPartitions == null) {
					log.info("Releasing partition ownerships for topic:"
							+ topic);
					this.releasePartitionOwnership(topic);
					this.topicRegistry.remove(topic);
					log.info("There are no partitions under topic " + topic);
					continue;
				}

				// 根据负载均衡策略获取这个consumer对应的partition列表
				final List<String> newParts = this.loadBalanceStrategy
						.getPartitions(topic, consumerId, curConsumers,
								curPartitions);

				// 查看当前这个topic的分区列表，查看是否有变更
				ConcurrentHashMap<Partition, TopicPartitionRegInfo> partRegInfos = this.topicRegistry
						.get(topic);
				if (partRegInfos == null) {
					partRegInfos = new ConcurrentHashMap<Partition, TopicPartitionRegInfo>();
					this.topicRegistry
							.put(topic,
									new ConcurrentHashMap<Partition, TopicPartitionRegInfo>());
				}
				final Set<Partition> currentParts = partRegInfos.keySet();

				for (final Partition partition : currentParts) {
					// 新的分区列表中不存在的分区，需要释放ownerShip，也就是老的有，新的没有
					if (!newParts.contains(partition.toString())) {
						log.info("Releasing partition ownerships for partition:"
								+ partition);
						partRegInfos.remove(partition);
						this.releasePartitionOwnership(topic, partition);
					}
				}

				for (final String partition : newParts) {
					// 当前没有的分区，挂载上去，也就是新的有，老的没有
					if (!currentParts.contains(new Partition(partition))) {
						log.info(consumerId + " attempting to claim partition "
								+ partition);
						// 注册分区owner关系
						if (!this.processPartition(topicDirs, partition, topic,
								consumerId)) {
							return false;
						}
					}
				}

			}
			this.updateFetchRunner(cluster);
			this.oldPartitionsPerTopicMap = partitionsPerTopicMap;
			this.oldConsumersPerTopicMap = consumersPerTopicMap;
			this.oldCluster = cluster;

			return true;
		}

		protected boolean checkClusterChange(final Cluster cluster) {
			return !this.oldCluster.equals(cluster);
		}

		protected Map<String, List<String>> getPartitionStringsForTopics(
				final Map<String, String> myConsumerPerTopicMap) {
			return ConsumerZooKeeper.this.metaZookeeper
					.getPartitionStringsForTopics(myConsumerPerTopicMap
							.keySet());
		}

		/**
		 * 添加分区的owner关系
		 * 
		 * @param topicDirs
		 * @param partition
		 * @param topic
		 * @param consumerThreadId
		 * @return
		 */
		private boolean processPartition(final ZKGroupTopicDirs topicDirs,
				final String partition, final String topic,
				final String consumerThreadId) throws Exception {
			final String partitionOwnerPath = topicDirs.consumerOwnerDir + "/"
					+ partition;
			try {
				ZkUtils.createEphemeralPathExpectConflict(
						ConsumerZooKeeper.this.zkClient, partitionOwnerPath,
						consumerThreadId);
			} catch (final ZkNodeExistsException e) {
				// 原始的关系应该已经删除，所以稍候再重试
				log.info("waiting for the partition ownership to be deleted: "
						+ partition);
				return false;

			} catch (final Exception e) {
				throw e;
			}
			this.addPartitionTopicInfo(topicDirs, partition, topic,
					consumerThreadId);
			return true;
		}

		// 获取offset信息并保存到本地
		private void addPartitionTopicInfo(final ZKGroupTopicDirs topicDirs,
				final String partitionString, final String topic,
				final String consumerThreadId) {
			final Partition partition = new Partition(partitionString);
			final ConcurrentHashMap<Partition, TopicPartitionRegInfo> partitionTopicInfo = this.topicRegistry
					.get(topic);
			TopicPartitionRegInfo existsTopicPartitionRegInfo = this
					.loadTopicPartitionRegInfo(topic, partition);
			if (existsTopicPartitionRegInfo == null) {
				// 初始化的时候默认使用0,TODO 可能采用其他
				if (isBrokerChange) {
					existsTopicPartitionRegInfo = this
					.initTopicPartitionRegInfo(topic, consumerThreadId,
							partition, 0);
				} else {
					existsTopicPartitionRegInfo = this
							.initTopicPartitionRegInfo(topic, consumerThreadId,
									partition, this.consumerConfig.getOffset());// Long.MAX_VALUE
				}
			}
			partitionTopicInfo.put(partition, existsTopicPartitionRegInfo);
		}

		/**
		 * 释放分区所有权
		 */
		private void releaseAllPartitionOwnership() {
			for (final Map.Entry<String, ConcurrentHashMap<Partition, TopicPartitionRegInfo>> entry : this.topicRegistry
					.entrySet()) {
				final String topic = entry.getKey();
				final ZKGroupTopicDirs topicDirs = ConsumerZooKeeper.this.metaZookeeper.new ZKGroupTopicDirs(
						topic, this.consumerConfig.getGroup());
				for (final Partition partition : entry.getValue().keySet()) {
					final String znode = topicDirs.consumerOwnerDir + "/"
							+ partition;
					this.deleteOwnership(znode);
				}
			}
		}

		/**
		 * 释放指定分区的ownership
		 * 
		 * @param topic
		 * @param partition
		 */
		private void releasePartitionOwnership(final String topic,
				final Partition partition) {
			final ZKGroupTopicDirs topicDirs = ConsumerZooKeeper.this.metaZookeeper.new ZKGroupTopicDirs(
					topic, this.consumerConfig.getGroup());
			final String znode = topicDirs.consumerOwnerDir + "/" + partition;
			this.deleteOwnership(znode);
		}

		private void deleteOwnership(final String znode) {
			try {
				ZkUtils.deletePath(ConsumerZooKeeper.this.zkClient, znode);
			} catch (final Throwable t) {
				log.error("exception during releasePartitionOwnership", t);
			}
			if (log.isDebugEnabled()) {
				log.debug("Consumer " + this.consumerIdString + " releasing "
						+ znode);
			}
		}

		/**
		 * 释放指定topic关联分区的ownership
		 * 
		 * @param topic
		 * @param partition
		 */
		private void releasePartitionOwnership(final String topic) {
			final ZKGroupTopicDirs topicDirs = ConsumerZooKeeper.this.metaZookeeper.new ZKGroupTopicDirs(
					topic, this.consumerConfig.getGroup());
			final ConcurrentHashMap<Partition, TopicPartitionRegInfo> partInfos = this.topicRegistry
					.get(topic);
			if (partInfos != null) {
				for (final Partition partition : partInfos.keySet()) {
					final String znode = topicDirs.consumerOwnerDir + "/"
							+ partition;
					this.deleteOwnership(znode);
				}
			}
		}

		/**
		 * 返回有变更的topic跟consumer集合
		 * 
		 * @param myConsumerPerTopicMap
		 * @param newPartMap
		 * @param oldPartMap
		 * @param newConsumerMap
		 * @param oldConsumerMap
		 * @return
		 */
		private Map<String, String> getRelevantTopicMap(
				final Map<String, String> myConsumerPerTopicMap,
				final Map<String, List<String>> newPartMap,
				final Map<String, List<String>> oldPartMap,
				final Map<String, List<String>> newConsumerMap,
				final Map<String, List<String>> oldConsumerMap) {
			final Map<String, String> relevantTopicThreadIdsMap = new HashMap<String, String>();
			for (final Map.Entry<String, String> entry : myConsumerPerTopicMap
					.entrySet()) {
				final String topic = entry.getKey();
				final String consumerId = entry.getValue();
				// 判断分区变更或者订阅者列表是否变更
				if (!this.listEquals(oldPartMap.get(topic),
						newPartMap.get(topic))
						|| !this.listEquals(oldConsumerMap.get(topic),
								newConsumerMap.get(topic))) {
					relevantTopicThreadIdsMap.put(topic, consumerId);
				}
			}
			return relevantTopicThreadIdsMap;
		}

		private boolean listEquals(final List<String> list1,
				final List<String> list2) {
			if (list1 == null && list2 != null) {
				return false;
			}
			if (list1 != null && list2 == null) {
				return false;
			}
			if (list1 == null && list2 == null) {
				return true;
			}
			return list1.equals(list2);
		}

		/**
		 * 获取某个分组订阅的topic到订阅者之间的映射map
		 * 
		 * @param group
		 * @return
		 * @throws Exception
		 * @throws NoNodeException
		 *             多个consumer同时在负载均衡时,可能会抛出NoNodeException
		 */
		protected Map<String, List<String>> getConsumersPerTopic(
				final String group) throws Exception, NoNodeException {
			final List<String> consumers = ZkUtils.getChildren(
					ConsumerZooKeeper.this.zkClient,
					this.dirs.consumerRegistryDir);
			final Map<String, List<String>> consumersPerTopicMap = new HashMap<String, List<String>>();
			for (final String consumer : consumers) {
				final List<String> topics = this.getTopics(consumer);// 多个consumer同时在负载均衡时,这里可能会抛出NoNodeException，--wuhua
				for (final String topic : topics) {
					if (consumersPerTopicMap.get(topic) == null) {
						final List<String> list = new ArrayList<String>();
						list.add(consumer);
						consumersPerTopicMap.put(topic, list);
					} else {
						consumersPerTopicMap.get(topic).add(consumer);
					}
				}

			}
			// 订阅者排序
			for (final Map.Entry<String, List<String>> entry : consumersPerTopicMap
					.entrySet()) {
				Collections.sort(entry.getValue());
			}
			return consumersPerTopicMap;
		}

		public Map<String, String> getConsumerPerTopic(final String consumerId)
				throws Exception {
			final List<String> topics = this.getTopics(consumerId);
			final Map<String/* topic */, String/* consumerId */> rt = new HashMap<String, String>();
			for (final String topic : topics) {
				rt.put(topic, consumerId);
			}
			return rt;
		}

		/**
		 * 根据consumerId获取订阅的topic列表
		 * 
		 * @param consumerId
		 * @return
		 * @throws Exception
		 */
		protected List<String> getTopics(final String consumerId)
				throws Exception {
			final String topicsString = ZkUtils.readData(
					ConsumerZooKeeper.this.zkClient,
					this.dirs.consumerRegistryDir + "/" + consumerId);
			final String[] topics = topicsString.split(",");
			final List<String> rt = new ArrayList<String>(topics.length);
			for (final String topic : topics) {
				rt.add(topic);
			}
			return rt;
		}

		// @Override
		// public void handleDataChange(String arg0, Object arg1) throws
		// Exception {
		// this.syncedRebalance();
		//
		// }
		//
		// @Override
		// public void handleDataDeleted(String arg0) throws Exception {
		// this.syncedRebalance();
		//
		// }
	}
}