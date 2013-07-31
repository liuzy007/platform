package com.taobao.tddl.tddl_sample.propertiesconfig;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.sql.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.tddl.common.GroupDataSourceRouteHelper;

/**
 * 
 * 经过TDDL解析器，规则引擎，合并结果3个步骤的标准流程。
 * 
 * @author junyu
 * 
 */
public class PropertyBaseCrudSampleDao {
	public static void main(String[] args) {
		JdbcTemplate tddlJT;
		ApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "classpath:propertyconfig/spring-context.xml" });
		tddlJT = (JdbcTemplate) context.getBean("tddlDS");
		PropertyBaseCrudSampleDao baseCrudSampleDao = new PropertyBaseCrudSampleDao();
		try {
			baseCrudSampleDao.insertData(tddlJT);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}

		try {
			baseCrudSampleDao.updateData(tddlJT);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}

		try {
			baseCrudSampleDao.findData(tddlJT);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}

		try {
			baseCrudSampleDao.deleteData(tddlJT);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}

		try {
			baseCrudSampleDao.insertDataByGMTShard(tddlJT);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			baseCrudSampleDao.queryDataByGMTShard(tddlJT);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			baseCrudSampleDao.deleteDataByGMTShard(tddlJT);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	private Date gmt = Date.valueOf((getNowTime("yyyy-MM-dd")));

	private void deleteDataByGMTShard(JdbcTemplate tddlJT) {
		String sql = "delete from gmttab where gmt_create=?";
		Object[] objs = new Object[] { gmt };
		tddlJT.update(sql, objs);
	}

	private void insertDataByGMTShard(JdbcTemplate tddlJT) {
		String sql = "insert into gmttab (pk,gmt_create) values (?,?)";
		System.out.println(gmt);
		Object[] arguments = new Object[] { 1, gmt };
		tddlJT.update(sql, arguments);
	}

	/**
	 * 这个方法可能会报异常，因为有主备存在但没有打开主备复制，又不一定去主库查，
	 * 导致可能去备库查而没有数据，map取值为空，抛出异常。这个是正常现象。说明 主写，主备同时提供写的配置是有效的。
	 * 
	 * 此处为了测试正常进行，添加GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
	 * 指定主库查询
	 * 
	 * @param tddlJT
	 */
	private void queryDataByGMTShard(JdbcTemplate tddlJT) {
		String sql = "select * from gmttab where gmt_create = ?";
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
		Object[] objs = new Object[] { gmt };
		System.out.println(tddlJT.queryForMap(sql, objs));
	}

	private void insertData(JdbcTemplate tddlJT) throws DataAccessException {
		String sql = "insert into moddbtab (pk,gmt_create) values (?,?)";
		Object[] arguments = new Object[] { 1, getNowTime() };
		tddlJT.update(sql, arguments);
	}

	/**
	 * 这个方法可能会报异常，因为有主备存在但没有打开主备复制，又不一定去主库查，
	 * 导致可能去备库查而没有数据，map取值为空，抛出异常。这个是正常现象。说明 主写，主备同时提供写的配置是有效的。
	 * 
	 * 此处为了测试正常进行，添加GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
	 * 指定主库查询
	 * 
	 * @param tddlJT
	 * @throws DataAccessException
	 */
	public void findData(JdbcTemplate tddlJT) throws DataAccessException {
		String sql = "select * from moddbtab where pk = ?";
		Object[] objs = new Object[] { 1 };
		System.out.println(tddlJT.queryForMap(sql, objs));
	}

	public void updateData(JdbcTemplate tddlJT) throws DataAccessException {
		String sql = "update moddbtab set gmt_create = ? where pk = 1";
		Object[] objs = new Object[] { this.getNextDay() };
		tddlJT.update(sql, objs);
	}

	public void deleteData(JdbcTemplate tddlJT) throws DataAccessException {
		String sql = "delete from moddbtab where pk=?";
		Object[] objs = new Object[] { 1 };
		tddlJT.update(sql, objs);
	}

	private String getNowTime() {
		return this.getNowTime(null);
	}

	private String getNowTime(String format) {
		Calendar cal = Calendar.getInstance();
		java.util.Date now = cal.getTime();

		return this.format(now, format);
	}

	private String getNextDay() {
		return this.getNextDay(null);
	}

	private String getNextDay(String format) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		java.util.Date next = cal.getTime();
		return this.format(next, format);
	}

	private String format(java.util.Date date, String format) {
		/**
		 * hh 为12小时制 HH 为24小时制
		 * 
		 */
		SimpleDateFormat sdf;
		if (null == format) {
			sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		} else {
			sdf = new SimpleDateFormat(format);
		}
		return sdf.format(date);
	}
}
