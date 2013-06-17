package com.alibaba.napoli.metamorphosis.client.consumer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 */

public class SubscribeInfoManagerAccessor {

	public static ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriberInfo>> getGroupTopicSubcriberRegistry(
			final SubscribeInfoManager subscribeInfoManager) {
		return subscribeInfoManager.getGroupTopicSubcriberRegistry();
	}
}