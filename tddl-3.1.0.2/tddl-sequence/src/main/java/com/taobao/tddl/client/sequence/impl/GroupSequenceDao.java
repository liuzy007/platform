package com.taobao.tddl.client.sequence.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.common.lang.StringUtil;
import com.taobao.tddl.client.sequence.SequenceDao;
import com.taobao.tddl.client.sequence.SequenceRange;
import com.taobao.tddl.client.sequence.exception.SequenceException;
import com.taobao.tddl.client.sequence.util.RandomSequence;
import com.taobao.tddl.common.GroupDataSourceRouteHelper;
import com.taobao.tddl.jdbc.group.TGroupDataSource;

public class GroupSequenceDao implements SequenceDao {

	private static final Log log = LogFactory.getLog(GroupSequenceDao.class);

	private static final int MIN_STEP = 1;
	private static final int MAX_STEP = 100000;

	private static final int DEFAULT_INNER_STEP = 1000;

	private static final int DEFAULT_RETRY_TIMES = 2;

	private static final String DEFAULT_TABLE_NAME = "sequence";
	private static final String DEFAULT_NAME_COLUMN_NAME = "name";
	private static final String DEFAULT_VALUE_COLUMN_NAME = "value";
	private static final String DEFAULT_GMT_MODIFIED_COLUMN_NAME = "gmt_modified";

	private static final int DEFAULT_DSCOUNT = 2;// 默认
	private static final Boolean DEFAULT_ADJUST = false;

	private static final long DELTA = 100000000L;
	// /**
	// * 数据源阵列
	// */
	// private DataSourceMatrixCreator dataSourceMatrixCreator;

	/**
	 * 应用名
	 */
	private String appName;

	/**
	 * group阵列
	 */
	private List<String> dbGroupKeys;

	/**
	 * 数据源
	 */
	private Map<String, DataSource> dataSourceMap;

	/**
	 * 自适应开关
	 */
	private boolean adjust = DEFAULT_ADJUST;
	/**
	 * 重试次数
	 */
	private int retryTimes = DEFAULT_RETRY_TIMES;

	/**
	 * 数据源个数
	 */
	private int dscount = DEFAULT_DSCOUNT;

	/**
	 * 内步长
	 */
	private int innerStep = DEFAULT_INNER_STEP;

	/**
	 * 外步长
	 */
	private int outStep = DEFAULT_INNER_STEP;

	/**
	 * 序列所在的表名
	 */
	private String tableName = DEFAULT_TABLE_NAME;

	/**
	 * 存储序列名称的列名
	 */
	private String nameColumnName = DEFAULT_NAME_COLUMN_NAME;

	/**
	 * 存储序列值的列名
	 */
	private String valueColumnName = DEFAULT_VALUE_COLUMN_NAME;

	/**
	 * 存储序列最后更新时间的列名
	 */
	private String gmtModifiedColumnName = DEFAULT_GMT_MODIFIED_COLUMN_NAME;

	private volatile String selectSql;
	private volatile String updateSql;
	private volatile String insertSql;

	/**
	 * 初试化
	 * 
	 * @throws SequenceException
	 */
	public void init() throws SequenceException {
		// 如果应用名为空，直接抛出
		if (StringUtils.isEmpty(appName)) {
			SequenceException sequenceException = new SequenceException(
					"appName is Null ");
			log.error("没有配置appName", sequenceException);
			throw sequenceException;
		}
		if (dbGroupKeys == null || dbGroupKeys.size() == 0) {
			log.error("没有配置dbgroupKeys");
			throw new SequenceException("dbgroupKeys为空！");
		}

		dataSourceMap = new HashMap<String, DataSource>();
		for (String dbGroupKey : dbGroupKeys) {
			if (dbGroupKey.toUpperCase().endsWith("-OFF")) {
				continue;
			}
			TGroupDataSource tGroupDataSource = new TGroupDataSource(
					dbGroupKey, appName);
			tGroupDataSource.init();
			dataSourceMap.put(dbGroupKey, tGroupDataSource);
		}
		if (dbGroupKeys.size() >= dscount) {
			dscount = dbGroupKeys.size();
		} else {
			for (int ii = dbGroupKeys.size(); ii < dscount; ii++) {
				dbGroupKeys.add(dscount + "-OFF");
			}
		}
		outStep = innerStep * dscount;// 计算外步长

		StringBuilder sb = new StringBuilder();
		sb.append("GroupSequenceDao初始化完成：\r\n ");
		sb.append("appName:").append(appName).append("\r\n");
		sb.append("innerStep:").append(this.innerStep).append("\r\n");
		sb.append("dataSource:").append(dscount).append("个:");
		for (String str : dbGroupKeys) {
			sb.append("[").append(str).append("]、");
		}
		sb.append("\r\n");
		sb.append("adjust：").append(adjust).append("\r\n");
		sb.append("retryTimes:").append(retryTimes).append("\r\n");
		sb.append("tableName:").append(tableName).append("\r\n");
		sb.append("nameColumnName:").append(nameColumnName).append("\r\n");
		sb.append("valueColumnName:").append(valueColumnName).append("\r\n");
		sb.append("gmtModifiedColumnName:").append(gmtModifiedColumnName)
				.append("\r\n");
		log.info(sb.toString());
	}

	/**
	 * 
	 * @param index
	 *            gourp内的序号，从0开始
	 * @param value
	 *            当前取的值
	 * @return
	 */
	private boolean check(int index, long value) {
		return (value % outStep) == (index * innerStep);
	}

	/**
	 * 检查并初试某个sequence 1、如果sequece不处在，插入值，并初始化值 2、如果已经存在，但有重叠，重新生成
	 * 3、如果已经存在，且无重叠。
	 * 
	 * @throws SequenceException
	 */
	public void adjust(String name) throws SequenceException, SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		for (int i = 0; i < dbGroupKeys.size(); i++) {
			if (dbGroupKeys.get(i).toUpperCase().endsWith("-OFF"))// 已经关掉，不处理
			{
				continue;
			}
			TGroupDataSource tGroupDataSource = (TGroupDataSource) dataSourceMap
					.get(dbGroupKeys.get(i));
			try {
				conn = tGroupDataSource.getConnection();
				stmt = conn.prepareStatement(getSelectSql());
				stmt.setString(1, name);
				GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
				rs = stmt.executeQuery();
				int item = 0;
				while (rs.next()) {
					item++;
					long val = rs.getLong(this.getValueColumnName());
					if (!check(i, val)) // 检验初值
					{
						if (this.isAdjust()) {
							this.adjustUpdate(i, val, name);
						} else {
							log.error("数据库中配置的初值出错！请调整你的数据库，或者启动adjust开关");
							throw new SequenceException(
									"数据库中配置的初值出错！请调整你的数据库，或者启动adjust开关");
						}
					}
				}
				if (item == 0)// 不存在,插入这条记录
				{
					if (this.isAdjust()) {
						this.adjustInsert(i, name);
					} else {
						log.error("数据库中未配置该sequence！请往数据库中插入sequence记录，或者启动adjust开关");
						throw new SequenceException(
								"数据库中未配置该sequence！请往数据库中插入sequence记录，或者启动adjust开关");
					}
				}
			} catch (SQLException e) {// 吞掉SQL异常，我们允许不可用的库存在
				log.error("初值校验和自适应过程中出错.", e);
				throw e;
			} finally {
				closeResultSet(rs);
				rs = null;
				closeStatement(stmt);
				stmt = null;
				closeConnection(conn);
				conn = null;

			}

		}
	}

	/**
	 * 更新
	 * 
	 * @param index
	 * @param value
	 * @param name
	 * @throws SequenceException
	 * @throws SQLException
	 */
	private void adjustUpdate(int index, long value, String name)
			throws SequenceException, SQLException {
		long newValue = (value - value % outStep) + outStep + index * innerStep;// 设置成新的调整值
		TGroupDataSource tGroupDataSource = (TGroupDataSource) dataSourceMap
				.get(dbGroupKeys.get(index));
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = tGroupDataSource.getConnection();
			stmt = conn.prepareStatement(getUpdateSql());
			stmt.setLong(1, newValue);
			stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			stmt.setString(3, name);
			stmt.setLong(4, value);
			GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				throw new SequenceException(
						"faild to auto adjust init value at  " + name
								+ " update affectedRow =0");
			}
			log.info(dbGroupKeys.get(index) + "更新初值成功!" + "sequence Name："
					+ name + "更新过程：" + value + "-->" + newValue);
		} catch (SQLException e) { // 吃掉SQL异常，抛Sequence异常
			log.error(
					"由于SQLException,更新初值自适应失败！dbGroupIndex:"
							+ dbGroupKeys.get(index) + "，sequence Name：" + name
							+ "更新过程：" + value + "-->" + newValue, e);
			throw new SequenceException(
					"由于SQLException,更新初值自适应失败！dbGroupIndex:"
							+ dbGroupKeys.get(index) + "，sequence Name：" + name
							+ "更新过程：" + value + "-->" + newValue, e);
		} finally {
			closeStatement(stmt);
			stmt = null;
			closeConnection(conn);
			conn = null;
		}
	}

	/**
	 * 插入新值
	 * 
	 * @param index
	 * @param name
	 * @return
	 * @throws SequenceException
	 * @throws SQLException
	 */
	private void adjustInsert(int index, String name) throws SequenceException,
			SQLException {
		TGroupDataSource tGroupDataSource = (TGroupDataSource) dataSourceMap
				.get(dbGroupKeys.get(index));
		long newValue = index * innerStep;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = tGroupDataSource.getConnection();
			stmt = conn.prepareStatement(getInsertSql());
			stmt.setString(1, name);
			stmt.setLong(2, newValue);
			stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				throw new SequenceException(
						"faild to auto adjust init value at  " + name
								+ " update affectedRow =0");
			}
			log.info(dbGroupKeys.get(index) + "   name:" + name + "插入初值:"
					+ name + "value:" + newValue);

		} catch (SQLException e) { // 吃掉SQL异常，抛sequence异常
			log.error(
					"由于SQLException,插入初值自适应失败！dbGroupIndex:"
							+ dbGroupKeys.get(index) + "，sequence Name：" + name
							+ "   value:" + newValue, e);
			throw new SequenceException(
					"由于SQLException,插入初值自适应失败！dbGroupIndex:"
							+ dbGroupKeys.get(index) + "，sequence Name：" + name
							+ "   value:" + newValue, e);
		} finally {
			closeResultSet(rs);
			rs = null;
			closeStatement(stmt);
			stmt = null;
			closeConnection(conn);
			conn = null;
		}
	}

	private ConcurrentHashMap<Integer/* ds index */, AtomicInteger/* 掠过次数 */> excludedKeyCount = new ConcurrentHashMap<Integer, AtomicInteger>(
			dscount);
	//最大略过次数后恢复
    private int maxSkipCount=10000;
    //使用慢速数据库保护
    private boolean useSlowProtect=false;
    //保护的时间
    private int protectMilliseconds=50;
   
    private ExecutorService exec = Executors.newFixedThreadPool(1);
    
	public SequenceRange nextRange(final String name) throws SequenceException {
		if (name == null) {
			log.error("序列名为空！");
			throw new IllegalArgumentException("序列名称不能为空");
		}

		long oldValue;
		long newValue;

		boolean readSuccess;
		boolean writeSuccess;

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
        
		int[] randomIntSequence = RandomSequence.randomIntSequence(dscount);
		for (int i = 0; i < retryTimes; i++) {
			for (int j = 0; j < dscount; j++) {
				readSuccess = false;
				writeSuccess = false;
				int index = randomIntSequence[j];
				if (dbGroupKeys.get(index).toUpperCase().endsWith("-OFF")) // 已经关掉，不处理
				{
					continue;// 该对象已经关闭
				}
				
				if(excludedKeyCount.get(index)!=null){
					if(excludedKeyCount.get(index).incrementAndGet()>maxSkipCount){
						excludedKeyCount.remove(index);
					    log.error(maxSkipCount+"次数已过，index为"+index+"的数据源后续重新尝试取序列");
				    }else{
				    	continue;
				    }
				}
				
				final TGroupDataSource tGroupDataSource = (TGroupDataSource) dataSourceMap
						.get(dbGroupKeys.get(index));
				// 查询，只在这里做数据库挂掉保护和慢速数据库保护
				try {
					//如果未使用超时保护或者已经只剩下了1个数据源，无论怎么样去拿
					if(!useSlowProtect||excludedKeyCount.size()>=(dscount-1)){
					    conn = tGroupDataSource.getConnection();
					    stmt = conn.prepareStatement(getSelectSql());
					    stmt.setString(1, name);
					    GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
					    rs = stmt.executeQuery();
					    rs.next();
					    oldValue = rs.getLong(1);
					}else{
						FutureTask<Long> future=new FutureTask<Long>(new Callable<Long>() {
							@Override
							public Long call() throws Exception {
								//直接抛出异常外面接，但是这里需要直接关闭链接
								Connection fconn=null;
								PreparedStatement fstmt=null;
								ResultSet frs=null;
								try{
								    fconn = tGroupDataSource.getConnection();
								    fstmt = fconn.prepareStatement(getSelectSql());
								    fstmt.setString(1, name);
								    GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
								    frs= fstmt.executeQuery();
								    frs.next();
								    return frs.getLong(1);
								}finally{
									closeResultSet(frs);
									frs = null;
									closeStatement(fstmt);
									fstmt = null;
									closeConnection(fconn);
									fconn = null;
								}
						    }
						});
						
						try {
						    exec.submit(future);
							oldValue=future.get(protectMilliseconds, TimeUnit.MILLISECONDS);
						} catch (InterruptedException e) {
							throw new SQLException("[SEQUENCE SLOW-PROTECTED MODE]:InterruptedException",e);
						} catch (ExecutionException e) {
							throw new SQLException("[SEQUENCE SLOW-PROTECTED MODE]:ExecutionException",e);
						} catch (TimeoutException e) {
							throw new SQLException("[SEQUENCE SLOW-PROTECTED MODE]:TimeoutException,当前设置超时时间为"+protectMilliseconds,e);
						}
					}
					
					if (oldValue < 0) {
						StringBuilder message = new StringBuilder();
						message.append(
								"Sequence value cannot be less than zero, value = ")
								.append(oldValue);
						message.append(", please check table ").append(
								getTableName());
						log.info(message);

						continue;
					}
					if (oldValue > Long.MAX_VALUE - DELTA) {
						StringBuilder message = new StringBuilder();
						message.append("Sequence value overflow, value = ")
								.append(oldValue);
						message.append(", please check table ").append(
								getTableName());
						log.info(message);
						continue;
					}
					
					newValue = oldValue + outStep;
					if (!check(index, newValue)) // 新算出来的值有问题
					{
						if (this.isAdjust()) {
							newValue = (newValue - newValue % outStep)
									+ outStep + index * innerStep;// 设置成新的调整值
						} else {
							SequenceException sequenceException = new SequenceException(
									dbGroupKeys.get(index)
											+ ":"
											+ name
											+ "的值得错误，覆盖到其他范围段了！请修改数据库，或者开启adjust开关！");

							log.error(dbGroupKeys.get(index) + ":" + name
									+ "的值得错误，覆盖到其他范围段了！请修改数据库，或者开启adjust开关！",
									sequenceException);
							throw sequenceException;
						}
					}
				} catch (SQLException e) {
					log.error("取范围过程中--查询出错！" + dbGroupKeys.get(index) + ":"
							+ name, e);
					//如果数据源只剩下了最后一个，就不要排除了
					if(excludedKeyCount.size()<(dscount-1)){
						excludedKeyCount.put(index, new AtomicInteger(0));
					    log.error("暂时踢除index为"+index+"的数据源，"+maxSkipCount+"次后重新尝试");
					}
					
					continue;
				} finally {
					closeResultSet(rs);
					rs = null;
					closeStatement(stmt);
					stmt = null;
					closeConnection(conn);
					conn = null;
				}
				readSuccess = true;

				try {
					conn = tGroupDataSource.getConnection();
					stmt = conn.prepareStatement(getUpdateSql());
					stmt.setLong(1, newValue);
					stmt.setTimestamp(2,
							new Timestamp(System.currentTimeMillis()));
					stmt.setString(3, name);
					stmt.setLong(4, oldValue);
					GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
					int affectedRows = stmt.executeUpdate();
					if (affectedRows == 0) {
						continue;
					}

				} catch (SQLException e) {
					log.error("取范围过程中--更新出错！" + dbGroupKeys.get(index) + ":"
							+ name, e);
					continue;
				} finally {
					closeStatement(stmt);
					stmt = null;
					closeConnection(conn);
					conn = null;
				}
				writeSuccess = true;
				if (readSuccess && writeSuccess)
					return new SequenceRange(newValue + 1, newValue + innerStep);
			}
			//当还有最后一次重试机会时,清空excludedMap,让其有最后一次机会
            if(i==(retryTimes-2)){
            	excludedKeyCount.clear();
            }
		}
		log.error("所有数据源都不可用！且重试" + this.retryTimes + "次后，仍然失败!");
		throw new SequenceException("All dataSource faild to get value!");
	}

	public int getDscount() {
		return dscount;
	}

	public void setDscount(int dscount) {
		this.dscount = dscount;
	}

	private String getInsertSql() {
		if (insertSql == null) {
			synchronized (this) {
				if (insertSql == null) {
					StringBuilder buffer = new StringBuilder();
					buffer.append("insert into ").append(getTableName())
							.append("(");
					buffer.append(getNameColumnName()).append(",");
					buffer.append(getValueColumnName()).append(",");
					buffer.append(getGmtModifiedColumnName()).append(
							") values(?,?,?);");
					insertSql = buffer.toString();
				}
			}
		}
		return insertSql;
	}

	private String getSelectSql() {
		if (selectSql == null) {
			synchronized (this) {
				if (selectSql == null) {
					StringBuilder buffer = new StringBuilder();
					buffer.append("select ").append(getValueColumnName());
					buffer.append(" from ").append(getTableName());
					buffer.append(" where ").append(getNameColumnName())
							.append(" = ?");

					selectSql = buffer.toString();
				}
			}
		}

		return selectSql;
	}

	private String getUpdateSql() {
		if (updateSql == null) {
			synchronized (this) {
				if (updateSql == null) {
					StringBuilder buffer = new StringBuilder();
					buffer.append("update ").append(getTableName());
					buffer.append(" set ").append(getValueColumnName())
							.append(" = ?, ");
					buffer.append(getGmtModifiedColumnName()).append(
							" = ? where ");
					buffer.append(getNameColumnName()).append(" = ? and ");
					buffer.append(getValueColumnName()).append(" = ?");

					updateSql = buffer.toString();
				}
			}
		}

		return updateSql;
	}

	private static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.debug("Could not close JDBC ResultSet", e);
			} catch (Throwable e) {
				log.debug("Unexpected exception on closing JDBC ResultSet", e);
			}
		}
	}

	private static void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.debug("Could not close JDBC Statement", e);
			} catch (Throwable e) {
				log.debug("Unexpected exception on closing JDBC Statement", e);
			}
		}
	}

	private static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.debug("Could not close JDBC Connection", e);
			} catch (Throwable e) {
				log.debug("Unexpected exception on closing JDBC Connection", e);
			}
		}
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public int getInnerStep() {
		return innerStep;
	}

	public void setInnerStep(int innerStep) {
		this.innerStep = innerStep;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getNameColumnName() {
		return nameColumnName;
	}

	public void setNameColumnName(String nameColumnName) {
		this.nameColumnName = nameColumnName;
	}

	public String getValueColumnName() {
		return valueColumnName;
	}

	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

	public String getGmtModifiedColumnName() {
		return gmtModifiedColumnName;
	}

	public void setGmtModifiedColumnName(String gmtModifiedColumnName) {
		this.gmtModifiedColumnName = gmtModifiedColumnName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setDbGroupKeys(List<String> dbGroupKeys) {
		this.dbGroupKeys = dbGroupKeys;
	}

	public boolean isAdjust() {
		return adjust;
	}

	public void setAdjust(boolean adjust) {
		this.adjust = adjust;
	}

	public int getMaxSkipCount() {
		return maxSkipCount;
	}

	public void setMaxSkipCount(int maxSkipCount) {
		this.maxSkipCount = maxSkipCount;
	}

	public boolean isUseSlowProtect() {
		return useSlowProtect;
	}

	public void setUseSlowProtect(boolean useSlowProtect) {
		this.useSlowProtect = useSlowProtect;
	}

	public int getProtectMilliseconds() {
		return protectMilliseconds;
	}

	public void setProtectMilliseconds(int protectMilliseconds) {
		this.protectMilliseconds = protectMilliseconds;
	}
}
