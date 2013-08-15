package com.taobao.tddl.client.jdbc.executeplan;

import java.util.List;
import java.util.Map;

import com.taobao.tddl.client.dispatcher.LogicTableName;
import com.taobao.tddl.client.jdbc.RealSqlContext;
import com.taobao.tddl.client.jdbc.SqlExecuteEvent;
import com.taobao.tddl.common.jdbc.ParameterContext;
import com.taobao.tddl.sqlobjecttree.GroupFunctionType;
import com.taobao.tddl.sqlobjecttree.OrderByEle;
public class ExecutionPlanImp implements ExecutionPlan {
	/**
	 * 复制event
	 */
	private List<SqlExecuteEvent> events;
	/**
	 * group 函数的类型
	 */
	private GroupFunctionType groupFunctionType;
	/**
	 * 最大
	 */
	private int max;
	/**
	 * 最小
	 */
	private int skip;
	/**
	 * dbIndex
	 */
	private Map<String/* dbIndex */, List<RealSqlContext>/* sql+args */> sqlMap;
	/**
	 * order by
	 */
	private List<OrderByEle> orderByColumns;
	/**
	 * 源参数
	 */
	private Map<Integer, ParameterContext> originalArgs;
	/**
	 * 是否是走读库的sql
	 */
	private boolean isGoSlave;
	/**
	 * 映射规则是否返回空值，这个不需要太理解，以后估计没几个业务会用的字段。
	 */
	private boolean mappingRuleReturnNullValue;
	/**
	 * 源sql
	 */
	private String originalSql;

	/**
	 * 虚拟表名
	 */
	private LogicTableName virtualTableName;
	
	/**
	 * 本次操作是否使用并行执行
	 */
    private boolean useParallel;
    
    private int autoGeneratedKeys=-1;
    
    private int[] columnIndexes=null;
    
    private String[] columnNames=null;
    
    private List<String> distinctColumns;
	
	public List<SqlExecuteEvent> getEvents() {

		return events;
	}

	public GroupFunctionType getGroupFunctionType() {
		return groupFunctionType;
	}

	public void setGroupFunctionType(GroupFunctionType groupFunctionType) {
		this.groupFunctionType = groupFunctionType;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getSkip() {
		return skip;
	}

	public void setSkip(int skip) {
		this.skip = skip;
	}

	public Map<String, List<RealSqlContext>> getSqlMap() {
		return sqlMap;
	}

	public void setSqlMap(Map<String, List<RealSqlContext>> sqlMap) {
		this.sqlMap = sqlMap;
	}

	public List<OrderByEle> getOrderByColumns() {
		return orderByColumns;
	}

	public void setOrderByColumns(List<OrderByEle> orderByColumns) {
		this.orderByColumns = orderByColumns;
	}

	public Map<Integer, ParameterContext> getOriginalArgs() {
		return originalArgs;
	}

	public void setOriginalArgs(Map<Integer, ParameterContext> originalArgs) {
		this.originalArgs = originalArgs;
	}

	public boolean isGoSlave() {
		return isGoSlave;
	}

	public void setGoSlave(boolean isGoSlave) {
		this.isGoSlave = isGoSlave;
	}

	public void setMappingRuleReturnNullValue(boolean mappingRuleReturnNullValue) {
		this.mappingRuleReturnNullValue = mappingRuleReturnNullValue;
	}

	public void setEvents(List<SqlExecuteEvent> events) {
		this.events = events;
	}

	public boolean mappingRuleReturnNullValue() {
		return mappingRuleReturnNullValue;
	}

	public String getOriginalSql() {
		return originalSql;
	}

	public void setOriginalSql(String originalSql) {
		this.originalSql = originalSql;
	}

	public void setVirtualTableName(LogicTableName virtualTableName) {
		this.virtualTableName = virtualTableName;
	}

	public LogicTableName getVirtualTableName() {
		return virtualTableName;
	}

	private boolean isUsingRealConnection;

	/* (non-Javadoc)
	 * @see com.taobao.tddl.client.jdbc.executeplan.ExecutionPlan#isUsingRealConnection()
	 */
	public boolean isUsingRealConnection() {
		return this.isUsingRealConnection;
	}

	public void setUsingRealConnection(boolean isUsingRealConnection) {
		this.isUsingRealConnection = isUsingRealConnection;
	}

	public boolean isUseParallel() {
		return useParallel;
	}

	public void setUseParallel(boolean useParallel) {
		this.useParallel = useParallel;
	}

	public int getAutoGeneratedKeys() {
		return autoGeneratedKeys;
	}

	public void setAutoGeneratedKeys(int autoGeneratedKeys) {
		this.autoGeneratedKeys = autoGeneratedKeys;
	}

	public int[] getColumnIndexes() {
		return columnIndexes;
	}

	public void setColumnIndexes(int[] columnIndexes) {
		this.columnIndexes = columnIndexes;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public List<String> getDistinctColumns() {
		return distinctColumns;
	}

	public void setDistinctColumns(List<String> distinctColumns) {
		this.distinctColumns = distinctColumns;
	}
}
