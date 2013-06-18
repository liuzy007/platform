/*(C) 2007-2012 Alibaba Group Holding Limited.	

import java.io.PrintWriter;
public class TDataSourceWrapper implements DataSource,SnapshotValuesOutputCallBack{
	private static Log logger = LogFactory.getLog(TDataSourceWrapper.class);
	private final DataSource targetDataSource;
	/**
	 * ��ǰ�̵߳�threadCountֵ,����������л��� ��ôʹ�õ��ǲ�ͬ��Datasource��װ�࣬�����໥Ӱ�졣
	 * threadCount������л����������Ǹ�ʱ���ܷ�Ӧ׼ȷ��ֵ��
	 * ����Ϊ�ɵı�����ǰҲ���ã��������ڴ���ά�������ݲ�ͬ��TDataSourceWrapper. ����̼߳�������������ӡ�
	 */
	final AtomicInteger threadCount = new AtomicInteger();//��Ȩ��
	final AtomicInteger threadCountReject = new AtomicInteger();//��Ȩ��
	final AtomicInteger concurrentReadCount = new AtomicInteger(); //��Ȩ��
	final AtomicInteger concurrentWriteCount = new AtomicInteger(); //��Ȩ��
	volatile TimesliceFlowControl writeFlowControl; //��Ȩ��
	volatile TimesliceFlowControl readFlowControl; //��Ȩ��

	/**
	 * д����
	 */
	final AtomicInteger writeTimesReject = new AtomicInteger();//��Ȩ��

	/**
	 * ������
	 */
	final AtomicInteger readTimesReject = new AtomicInteger();//��Ȩ��
	volatile ConnectionProperties connectionProperties = new ConnectionProperties(); //��Ȩ��

	protected TAtomDsConfDO runTimeConf;
	private static final Map<String, ExceptionSorter> exceptionSorters = new HashMap<String, ExceptionSorter>(2);
	static {
		exceptionSorters.put(AtomDbTypeEnum.ORACLE.name(), new OracleExceptionSorter());
		exceptionSorters.put(AtomDbTypeEnum.MYSQL.name(), new MySQLExceptionSorter());
	}
	private final ReentrantLock lock = new ReentrantLock();
	//private volatile boolean isNotAvailable = false; //�Ƿ񲻿���
	private volatile SmoothValve smoothValve = new SmoothValve(20);
	private volatile CountPunisher timeOutPunisher = new CountPunisher(new SmoothValve(20), 3000, 300);//3����֮�ڳ�ʱ300����ͷ��������ܵķ�ֵ���൱�ڹر���

	private static final int default_retryBadDbInterval = 2000; //milliseconds
	protected static int retryBadDbInterval; //milliseconds
	static {
		int interval = default_retryBadDbInterval;
		String propvalue = System.getProperty("com.taobao.tddl.DBSelector.retryBadDbInterval");
		if (propvalue != null) {
			try {
				interval = Integer.valueOf(propvalue.trim());
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		retryBadDbInterval = interval;
	}

	public AtomDbStatusEnum getDbStatus() {
		return connectionProperties.dbStatus;
	}

	public void setDbStatus(AtomDbStatusEnum dbStatus) {
		this.connectionProperties.dbStatus = dbStatus;
	}

	public static class ConnectionProperties {
		public volatile AtomDbStatusEnum dbStatus;
		/**
		 * ��ǰ���ݿ������
		 */
		public volatile String datasourceName;
		/**
		 * �߳�count���ƣ�0Ϊ������
		 */
		public volatile int threadCountRestriction;

		/**
		 * ��������������������0Ϊ������
		 */
		public volatile int maxConcurrentReadRestrict;

		/**
		 * ��������д����������0Ϊ������
		 */
		public volatile int maxConcurrentWriteRestrict;
	}

	public TDataSourceWrapper(DataSource targetDataSource, TAtomDsConfDO runTimeConf) {
		this.runTimeConf = runTimeConf;
		this.targetDataSource = targetDataSource;

		Monitor.addSnapshotValuesCallbask(this);

		this.readFlowControl = new TimesliceFlowControl("������", runTimeConf.getTimeSliceInMillis(), runTimeConf
				.getReadRestrictTimes());
		this.writeFlowControl = new TimesliceFlowControl("д����", runTimeConf.getTimeSliceInMillis(), runTimeConf
				.getWriteRestrictTimes());

		logger.warn("set thread count restrict " + runTimeConf.getThreadCountRestrict());
		this.connectionProperties.threadCountRestriction = runTimeConf.getThreadCountRestrict();

		logger.warn("set maxConcurrentReadRestrict " + runTimeConf.getMaxConcurrentReadRestrict());
		this.connectionProperties.maxConcurrentReadRestrict = runTimeConf.getMaxConcurrentReadRestrict();

		logger.warn("set maxConcurrentWriteRestrict " + runTimeConf.getMaxConcurrentWriteRestrict());
		this.connectionProperties.maxConcurrentWriteRestrict = runTimeConf.getMaxConcurrentWriteRestrict();
	}

	//��Ȩ�ޣ������ζ������
	void countTimeOut() {
		timeOutPunisher.count();
	}

	private volatile long lastRetryTime = 0;

	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

	/**
	 * ����ֻ����tryLock���ӳ��ԣ��������߼�ί�ɸ�getConnection0
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		SmoothValve valve = smoothValve;
		try {
			if (valve.isNotAvailable()) {
				boolean toTry = System.currentTimeMillis() - lastRetryTime > retryBadDbInterval;
				if (toTry && lock.tryLock()) {
					try {
						Connection t = this.getConnection0(username, password); //ͬһ��ʱ��ֻ����һ���̼߳���ʹ���������Դ��
						valve.setAvailable(); //��һ���߳����ԣ�ִ�гɹ�����Ϊ���ã��Զ��ָ�
						return t;
					} finally {
						lastRetryTime = System.currentTimeMillis();
						lock.unlock();
					}
				} else {
					throw new AtomNotAvailableException(this.runTimeConf.getDbName() + " isNotAvailable"); //�����߳�fail-fast
				}
			} else {
				if (valve.smoothThroughOnInitial()) {
					return this.getConnection0(username, password);
				} else {
					throw new AtomNotAvailableException(this.runTimeConf.getDbName()
							+ " squeezeThrough rejected on fatal reset"); //δͨ����λʱ����������
				}
			}
		} catch (SQLException e) {
			ExceptionSorter exceptionSorter = exceptionSorters
					.get(dbType);
			if (exceptionSorter.isExceptionFatal(e)) {
				NagiosUtils.addNagiosLog(NagiosUtils.KEY_DB_NOT_AVAILABLE + "|" + this.runTimeConf.getDbName(), e
						.getMessage());
				valve.setNotAvailable();
			}
			throw e;
		}
	}

	private Connection getConnection0(String username, String password) throws SQLException {
		TConnectionWrapper tconnectionWrapper;
		try {
			recordThreadCount();
			tconnectionWrapper = new TConnectionWrapper(getConnectionByTargetDataSource(username, password), this);
		} catch (SQLException e) {
			threadCount.decrementAndGet();
			throw e;
		} catch (RuntimeException e) {
			threadCount.decrementAndGet();
			throw e;
		}
		return tconnectionWrapper;
	}

	private Connection getConnectionByTargetDataSource(String username, String password) throws SQLException {
		if (username == null && password == null) {
			return targetDataSource.getConnection();
		} else {
			return targetDataSource.getConnection(username, password);
		}
	}

	private void recordThreadCount() throws SQLException {
		int threadCountRestriction = connectionProperties.threadCountRestriction;
		int currentThreadCount = threadCount.incrementAndGet();
		if (threadCountRestriction != 0) {
			if (currentThreadCount > threadCountRestriction) {
				threadCountReject.incrementAndGet();
				throw new SQLException("max thread count : " + currentThreadCount);
			}
		}
	}

	/**
	 * ����
	 *
	 * @param datasourceName
	 */
	public synchronized void setDatasourceName(String datasourceName) {
		this.connectionProperties.datasourceName = datasourceName;
	}

	/**
	 * ����ʱ��Ƭ�������ʱ��Ҫ�����ƶ��ƻ��� bug fix : ��ǰû�������ƶ�schedule.���������������Ч��
	 *
	 * @param timeSliceInMillis
	 */
	public synchronized void setTimeSliceInMillis(int timeSliceInMillis) {
		if (timeSliceInMillis == 0) {
			logger.warn("timeSliceInMills is 0,return ");
		}
		this.readFlowControl = new TimesliceFlowControl("������", timeSliceInMillis, runTimeConf.getReadRestrictTimes());
		this.writeFlowControl = new TimesliceFlowControl("д����", timeSliceInMillis, runTimeConf.getWriteRestrictTimes());
	}

	/* ========================================================================
	 * ===== jdbc�ӿڷ�������ί�ɸ�targetDataSource
	 * ======================================================================*/

	public PrintWriter getLogWriter() throws SQLException {
		return targetDataSource.getLogWriter();
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		targetDataSource.setLogWriter(out);
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		targetDataSource.setLoginTimeout(seconds);
	}

	public int getLoginTimeout() throws SQLException {
		return targetDataSource.getLoginTimeout();
	}

	/**
	 * jdk1.6 �����ӿ�
	 */
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (isWrapperFor(iface)) {
			return (T) this;
		} else {
			throw new SQLException("not a wrapper for " + iface);
		}
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return TDataSourceWrapper.class.isAssignableFrom(iface);
	}

	@Override
	public ConcurrentHashMap<String, Values> getValues() {
		ConcurrentHashMap<String, Values> concurrentHashMap = new ConcurrentHashMap<String, Values>();
		String prefix = connectionProperties.datasourceName + "_";

		// ����threadCount
		Values threadCountValues = new Values();
		threadCountValues.value1.set(threadCount.longValue());
		threadCountValues.value2.set(connectionProperties.threadCountRestriction);
		concurrentHashMap.put(prefix + Key.THREAD_COUNT, threadCountValues);

		//���Ӷ�д�ܾ�����
		Values rejectCountValues = new Values();
		rejectCountValues.value1.set(readTimesReject.longValue() + this.readFlowControl.getTotalRejectCount());
		rejectCountValues.value2.set(writeTimesReject.longValue() + this.writeFlowControl.getTotalRejectCount());
		concurrentHashMap.put(prefix + Key.READ_WRITE_TIMES_REJECT_COUNT, rejectCountValues);

		// ���Ӷ�дcount
		Values lastReadWriteSnapshot = new Values();
		lastReadWriteSnapshot.value1.set(this.readFlowControl.getCurrentCount());
		lastReadWriteSnapshot.value2.set(this.writeFlowControl.getCurrentCount());
		concurrentHashMap.put(prefix + Key.READ_WRITE_TIMES, lastReadWriteSnapshot);

		//���Ӷ�д��������
		Values rwConcurrent = new Values();
		rwConcurrent.value1.set(this.concurrentReadCount.longValue());
		rwConcurrent.value2.set(this.concurrentWriteCount.longValue());
		concurrentHashMap.put(prefix + Key.READ_WRITE_CONCURRENT, rwConcurrent);

		return concurrentHashMap;
	}

}