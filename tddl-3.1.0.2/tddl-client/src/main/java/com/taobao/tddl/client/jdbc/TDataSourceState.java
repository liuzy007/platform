package com.taobao.tddl.client.jdbc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import com.taobao.tddl.common.Monitor;
import com.taobao.tddl.common.monitor.SnapshotValuesOutputCallBack;
import com.taobao.tddl.parser.ParserCache;

/**
 * TDDL 内部健康状态访问接口
 * 
 * @author linxuan
 *
 */
public class TDataSourceState implements SnapshotValuesOutputCallBack {
	private ThreadPoolExecutor replicationExecutor;
	private Integer replicationQueueSize;
	private final String dataSourceName;

	public TDataSourceState(String dataSourceName) {
		this.dataSourceName = dataSourceName;
		Monitor.addSnapshotValuesCallbask(this);
	}

	public int getReplicationMinPoolSize() {
		return replicationExecutor == null ? -1 : replicationExecutor.getCorePoolSize();
	}

	public int getReplicationMaxPoolSize() {
		return replicationExecutor == null ? -1 : replicationExecutor.getMaximumPoolSize();
	}

	public int getReplicationCurrentPoolSize() {
		return replicationExecutor == null ? -1 : replicationExecutor.getPoolSize();
	}

	public int getReplicationCurrentQueueSize() {
		return replicationExecutor == null ? -1 : replicationExecutor.getQueue().size();
	}

	public int getReplicationMaxQueueSize() {
		return replicationQueueSize == null ? -1 : replicationQueueSize;
	}

	public int getParserCacheSize() {
		return ParserCache.instance().size();
	}

	@Override
	public ConcurrentHashMap<String, Values> getValues() {
		ConcurrentHashMap<String, Values> concurrentHashMap = new ConcurrentHashMap<String, Values>();
		// 复制队列长度: 当前长度/最大长度
		Values replicationQueueSize = new Values();
		replicationQueueSize.value1.set(getReplicationCurrentQueueSize());
		replicationQueueSize.value2.set(getReplicationMaxQueueSize());
		concurrentHashMap.put(dataSourceName + Key.replicationQueueSize, replicationQueueSize);

		// 复制线程池大小： 当前线程数/最大线程数
		Values replicationPoolSize = new Values();
		replicationPoolSize.value1.set(getReplicationCurrentPoolSize());
		replicationPoolSize.value2.set(getReplicationMaxPoolSize());
		concurrentHashMap.put(dataSourceName + Key.replicationPoolSize, replicationPoolSize);

		//解析缓存大小：当前大小/最大上限
		Values parserCacheSize = new Values();
		parserCacheSize.value1.set(getParserCacheSize());
		parserCacheSize.value2.set(ParserCache.instance().capacity);
		concurrentHashMap.put(dataSourceName + Key.parserCacheSize, parserCacheSize);

		return concurrentHashMap;
	}

	/**
	 * Setter
	 */
	public void setReplicationExecutor(ThreadPoolExecutor replicationExecutor) {
		this.replicationExecutor = replicationExecutor;
	}

	public void setReplicationQueueSize(Integer replicationQueueSize) {
		this.replicationQueueSize = replicationQueueSize;
	}
}
