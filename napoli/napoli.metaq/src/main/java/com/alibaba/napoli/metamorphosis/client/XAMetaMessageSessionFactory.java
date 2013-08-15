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
package com.alibaba.napoli.metamorphosis.client;

import com.alibaba.napoli.metamorphosis.client.producer.PartitionSelector;
import com.alibaba.napoli.metamorphosis.client.producer.RoundRobinPartitionSelector;
import com.alibaba.napoli.metamorphosis.client.producer.SimpleXAMessageProducer;
import com.alibaba.napoli.metamorphosis.client.producer.XAMessageProducer;
import com.alibaba.napoli.metamorphosis.exception.MetaClientException;

/**
 * 支持事务的XA消息工厂
 * 
 * @author boyan(boyan@taobao.com)
 * @date 2011-8-17
 * 
 */
public class XAMetaMessageSessionFactory extends MetaMessageSessionFactory
		implements XAMessageSessionFactory {

	public XAMetaMessageSessionFactory(final MetaClientConfig metaClientConfig)
			throws MetaClientException {
		super(metaClientConfig);

	}

	@Override
	public XAMessageProducer createXAProducer(
			final PartitionSelector partitionSelector) {
		if (partitionSelector == null) {
			throw new IllegalArgumentException("Null partitionSelector");
		}
		return this.addChild(new SimpleXAMessageProducer(this,
				this.remotingClient, partitionSelector, this.producerZooKeeper,
				this.sessionIdGenerator.generateId()));
	}

	@Override
	public XAMessageProducer createXAProducer() {
		return this.createXAProducer(new RoundRobinPartitionSelector());
	}

}