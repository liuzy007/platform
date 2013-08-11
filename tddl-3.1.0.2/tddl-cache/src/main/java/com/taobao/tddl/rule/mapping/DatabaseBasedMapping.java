package com.taobao.tddl.rule.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * 数据库为依托的映射规则。 核心是</br> <blockquote>
 * 
 * <pre>
 * TargetColumn      SourceColumn 
 * ------------ <---  --------------- 
 *    ?              SourceValue
 * </pre>
 * 
 * </blockquote>
 * 
 * sourceColumn 是从advancedParameters里获得。</br> sourceValue 是从sql中获得 targetColumn
 * 是从targetRule中分析获得 求?
 * 
 * @author shenxun
 * 
 */
public class DatabaseBasedMapping {
	static final Log logger = LogFactory.getLog(DatabaseBasedMapping.class);
	Map<String/* target key */, TypeHandlerEntry> typeHandlerMap;

	public enum TARGET_VALUE_TYPE {
		INTEGER, LONG, STRING;
	}

	private String sourceColumn;

	protected String[] columns;

	private final Log log = LogFactory.getLog(DatabaseBasedMapping.class);
	/**
	 * datasource
	 */
	private DataSource routeDatasource;
	/**
	 * 表名 select column from [tableName] where ...
	 */
	private String routeTable;

	/**
	 * jdbc包装
	 */
	private JdbcTemplate jdbcTemplate = null;

	/**
	 * 根据sourceValue去查询路由表，返回路由表中列名为sourceKey的列的值与sourceValue相等的记录,
	 * 将记录中列名为targetKey的列值返回
	 * 
	 * @param targetKey
	 *            指定要获取的是路由表中的哪一列
	 * @param sourceKey
	 *            指定将路由表中的哪一列作为key列
	 * @param sourceValue
	 *            原始值。
	 * @return
	 */
	protected Object get(String targetKey, String sourceKey, Object sourceValue) {

		Object value = getFromDatabase(routeTable, targetKey, sourceKey,
				sourceValue);

		return value;
	}

	/**
	 * 根据sourceValue去查询路由表（路由表中作为key的列与sourceValue比较）得到整条记录,
	 * 将记录中列名为targetKey的列值返回
	 * 
	 * @param targetKey
	 *            指定要获取的是路由表中的哪一列
	 * @param sourceValue
	 *            原始值。
	 * @return
	 */
	public Object get(String targetKey, Object sourceValue) {
		if (sourceColumn == null) {
			throw new IllegalArgumentException(
					"sourceColumn should not be null;");
		}
		return get(targetKey, sourceColumn, sourceValue);
	}

	protected String getColumns() {
		if (columns == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		boolean firstElement = true;
		for (String col : columns) {
			if (firstElement) {
				firstElement = false;
				sb.append(col);
			} else {
				sb.append(",");
				sb.append(col);
			}
		}
		return sb.toString();
	}

	/**
	 * 映射后的值
	 * 
	 * @param namespace
	 * @param key
	 * @return
	 */
	protected Object getFromDatabase(String routeTable,
			final String targetColumn, final String originalColumn,
			final Object originalValue) {
		// 获取select sql
		Object resultValue = null;
		resultValue = null;
		Map<String, Object> target = getResultMap(originalColumn,
				originalValue, targetColumn);
		resultValue = target.get(targetColumn);
		return resultValue;
	}

	private void throwRuntimeSqlExceptionWrapper(DataAccessException e) {
		// modified by
		// shenxun:原则上规则引擎是不应该抛出sqlException的。但这里做了fix，抛出用特定异常标志的sqlException
		// 在getExecutionContext的时候应该予以捕获并换为comminuticationException抛出。
		Throwable throwable = e.getCause();
		if (throwable instanceof SQLException) {
			throw new RuntimeException((SQLException) throwable);
		} else {
			log.error("非SQLException 不符合预期,", e);
		}
	}

	/*
	 * private Object[] getInsertSql(String tableName, Map<String, Object>
	 * values, StringBuilder sb) { StringBuilder columnBuilder = new
	 * StringBuilder(); StringBuilder valuesBuilder = new StringBuilder();
	 * Object[] args = new Object[values.size()]; int index = 0; boolean
	 * isFirstElement = true; for (Entry<String, Object> entry :
	 * values.entrySet()) { if (!isFirstElement) { columnBuilder.append(",");
	 * valuesBuilder.append(","); } isFirstElement = false;
	 * columnBuilder.append(entry.getKey()); valuesBuilder.append("?");
	 * args[index] = entry.getValue(); index++; }
	 * sb.append("insert into ").append(tableName).append(" (").append(
	 * columnBuilder.toString()).append(") values (").append(
	 * valuesBuilder.toString()).append(")"); return args; }
	 */

	@SuppressWarnings("unchecked")
	protected Map<String, Object> getResultMap(final String originalColumn,
			final Object originalValue, String targetColumn) {
		String sql = getSelectKeySql(routeTable, originalColumn, targetColumn);
		Object[] args = new Object[] { originalValue };
		Map<String, Object> target = null;
		try {
			target = (Map<String, Object>) jdbcTemplate.query(sql, args,
					new ResultSetExtractor() {
						public Object extractData(ResultSet rs)
								throws SQLException, DataAccessException {

							Map<String, Object> value = new HashMap<String, Object>(
									columns.length);
							if (rs.next()) {
								int i = 1;
								for (String col : columns) {
									Object obj = rs.getObject(i);
									TypeHandlerEntry typeHandlerEntry =	typeHandlerMap.get(col);
									Object requestValue = typeHandlerEntry.typeHandler.getRequestValue(obj);
									value.put(col, requestValue);
									i++;
								}
							}
							return value;
						}
					});
		} catch (DataAccessException e) {
			throwRuntimeSqlExceptionWrapper(e);
		}

		return target;
	}

	public String getRouteTable() {
		return routeTable;
	}

	/**
	 * @param tableName
	 * @param originalColumn
	 * @param targetColumn
	 * @return
	 */
	String getSelectKeySql(String tableName, String originalColumn,
			String targetColumn) {

		StringBuilder sb = new StringBuilder();
		String columns = getColumns();
		sb.append("select ").append(columns).append(" from ").append(tableName)
				.append(" where ").append(originalColumn).append(" = ?");
		String sql = sb.toString();
		return sql;
	}

	public DataSource getRouteDatasource() {
		return routeDatasource;
	}

	public void initInternal() {
		if (routeDatasource == null) {
			throw new IllegalArgumentException("未指定datasource");
		}
		if (columns == null) {
			throw new IllegalArgumentException("未指定columns");
		}

		if (this.routeTable == null) {
			throw new IllegalArgumentException("未指定routeTable");
		}
		log.debug("put ds to jdbc template");
		jdbcTemplate = new JdbcTemplate(routeDatasource);

	}

	/*
	 * protected int put(Map<String, Object> values) { StringBuilder
	 * insertSqlStringBuilder = new StringBuilder(); Object[] args =
	 * getInsertSql(routeTable, values, insertSqlStringBuilder); return
	 * jdbcTemplate.update(insertSqlStringBuilder.toString(), args);
	 * 
	 * }
	 */

	/**
	 * 设置sql中 select [columns] from table
	 * 在只用databaseMapping的时候可以不用，在tair+database Mapping 中必须用
	 * 
	 * @param columns
	 */
	public void setColumns(String columns) {
		if (columns == null) {
			throw new IllegalArgumentException("columns is null");
		}

		String[] columnsArray = columns.split(",");
		List<String> cols = new ArrayList<String>(columnsArray.length);
		int index = 0;
		typeHandlerMap = new HashMap<String, TypeHandlerEntry>(
				columnsArray.length);
		for (String col : columnsArray) {
			// 解析column|type
			String[] columnsAndType = col.split("\\|");
			if (columnsAndType.length != 2) {
				throw new IllegalArgumentException("一个column 和他对应的type必须为两个长度");
			}
			// 添加columns到columns List里
			cols.add(columnsAndType[0]);

			String type = columnsAndType[1];
			if (type == null || type.equals("")) {
				throw new IllegalArgumentException("type 等于null");
			}
			type = type.toLowerCase();
			TypeHandlerEntry entry = new TypeHandlerEntry();
			// type解析器 因为是init的时候，所以不用担心效率
			if ("int".equals(type) || "integer".equals(type)) {

				entry.typeHandler = new IntegerTypeHandler();
			} else if ("long".equals(type)) {
				entry.typeHandler = new LongTypeHandler();
			} else if ("string".equals(type) || "str".equals(type)) {
				entry.typeHandler = new StringTypeHandler();
			} else {
				throw new IllegalArgumentException("unknow type handler");
			}
			entry.index = index;
			typeHandlerMap.put(columnsAndType[0], entry);

			index++;
		}

		// type解析器解析完成后，还需要将parent的columns项目也填好 这样才能查到所有数据
		this.columns = cols.toArray(new String[cols.size()]);

	}

	public void setRouteTable(String routeTable) {
		this.routeTable = routeTable;
	}

	public void setRouteDatasource(DataSource routeDatasource) {
		this.routeDatasource = routeDatasource;
	}

	public String getSourceColumn() {
		return sourceColumn;
	}

	public void setSourceColumn(String sourceColumn) {
		this.sourceColumn = sourceColumn;
	}

	public static class LongTypeHandler implements TypeHandler {
		public Object process(String value) {
			if (value == null) {
				return null;
			}
			return Long.valueOf(value);
		}

		public Object getRequestValue(Object source) {
			if (source instanceof String) {
				return Integer.valueOf((String) source);
			} else if (source instanceof Number) {
				return ((Number) source).longValue();
			}
			logger.warn("不支持当前值转换 ， 当前值 ：" + source
					+ " type : " + source.getClass());
			return source;
		}
	}

	/**
	 * tair要求使用简单对象，因此在tair里存放的全部都是String对象，只在取出的时候进行转换
	 * 
	 * @author shenxun
	 * 
	 */
	static interface TypeHandler {
		Object process(String value);

		Object getRequestValue(Object source);
	}

	public static class StringTypeHandler implements TypeHandler {
		public Object process(String value) {
			return value;
		}

		public Object getRequestValue(Object source) {
			return String.valueOf(source);
		}
	}

	public static class IntegerTypeHandler implements TypeHandler {
		public Object process(String value) {
			if (value == null) {
				return null;
			}
			return Integer.valueOf(value);
		}

		public Object getRequestValue(Object source) {
			if (source instanceof String) {
				return Integer.valueOf((String) source);
			} else if (source instanceof Number) {
				return ((Number) source).intValue();
			}
			logger.warn("不支持当前值转换 ， 当前值 ：" + source + " type : "
					+ source.getClass());
			return source;

		}
	}

	static class TypeHandlerEntry {
		public TypeHandler typeHandler;
		public int index;
	}

}
