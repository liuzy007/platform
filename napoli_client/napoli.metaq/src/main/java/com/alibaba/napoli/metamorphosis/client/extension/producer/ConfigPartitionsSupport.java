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
package com.alibaba.napoli.metamorphosis.client.extension.producer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.alibaba.napoli.metamorphosis.client.producer.PartitionSelector;
import com.alibaba.napoli.metamorphosis.cluster.Partition;

/**
 * 支持获取某topic分区总数的Selector
 * 
 * @author 无花
 * @since 2011-8-2 下午02:49:27
 */
public abstract class ConfigPartitionsSupport implements PartitionSelector,
		ConfigPartitionsAware {

	private Map<String, List<Partition>> partitionsNumMap;

	@Override
	synchronized public void setConfigPartitions(
			final Map<String/* topic */, List<Partition>> map) {
		this.partitionsNumMap = map;
	}

	@Override
	public synchronized List<Partition> getConfigPartitions(final String topic) {
		final List<Partition> partitions = this.partitionsNumMap != null ? this.partitionsNumMap
				.get(topic) : null;
		return partitions != null ? partitions
				: (List<Partition>) Collections.EMPTY_LIST;
	}
}