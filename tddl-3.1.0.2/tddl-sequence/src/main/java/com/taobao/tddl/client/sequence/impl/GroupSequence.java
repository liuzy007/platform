package com.taobao.tddl.client.sequence.impl;

import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.taobao.tddl.client.sequence.Sequence;
import com.taobao.tddl.client.sequence.SequenceDao;
import com.taobao.tddl.client.sequence.SequenceRange;
import com.taobao.tddl.client.sequence.exception.SequenceException;

public class GroupSequence implements Sequence {
	private final Lock lock = new ReentrantLock();
	private SequenceDao sequenceDao;

	private String name;
	private volatile SequenceRange currentRange;

	/**
	 * 初始化一下，如果name不存在，则给其初始值
	 * 
	 * @throws SequenceException
	 * @throws SQLException 
	 */
	public void init() throws SequenceException, SQLException {
		if (!(sequenceDao instanceof GroupSequenceDao)) {
			throw new SequenceException("please use  GroupSequenceDao!");
		}
		GroupSequenceDao groupSequenceDao=(GroupSequenceDao)sequenceDao;
		synchronized(this)  //为了保证安全，
		{
			groupSequenceDao.adjust(name);
		}
	}

	public long nextValue() throws SequenceException {
		if (currentRange == null) {
			lock.lock();
			try {
				if (currentRange == null) {
					currentRange = sequenceDao.nextRange(name);
				}
			} finally {
				lock.unlock();
			}
		}

		long value = currentRange.getAndIncrement();
		if (value == -1) {
			lock.lock();
			try {
				for (;;) {
					if (currentRange.isOver()) {
						currentRange = sequenceDao.nextRange(name);
					}

					value = currentRange.getAndIncrement();
					if (value == -1) {
						continue;
					}

					break;
				}
			} finally {
				lock.unlock();
			}
		}

		if (value < 0) {
			throw new SequenceException("Sequence value overflow, value = "
					+ value);
		}

		return value;
	}

	public SequenceDao getSequenceDao() {
		return sequenceDao;
	}

	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
