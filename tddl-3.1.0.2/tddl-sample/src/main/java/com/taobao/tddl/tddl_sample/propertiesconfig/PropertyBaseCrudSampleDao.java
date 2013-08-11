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
 * 缁忚繃TDDL瑙ｆ瀽鍣紝瑙勫垯寮曟搸锛屽悎骞剁粨鏋�涓楠ょ殑鏍囧噯娴佺▼銆�
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
	 * 杩欎釜鏂规硶鍙兘浼氭姤寮傚父锛屽洜涓烘湁涓诲瀛樺湪浣嗘病鏈夋墦寮�富澶囧鍒讹紝鍙堜笉涓�畾鍘讳富搴撴煡锛�
	 * 瀵艰嚧鍙兘鍘诲搴撴煡鑰屾病鏈夋暟鎹紝map鍙栧�涓虹┖锛屾姏鍑哄紓甯搞�杩欎釜鏄甯哥幇璞°�璇存槑 涓诲啓锛屼富澶囧悓鏃舵彁渚涘啓鐨勯厤缃槸鏈夋晥鐨勩�
	 * 
	 * 姝ゅ涓轰簡娴嬭瘯姝ｅ父杩涜锛屾坊鍔燝roupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
	 * 鎸囧畾涓诲簱鏌ヨ
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
	 * 杩欎釜鏂规硶鍙兘浼氭姤寮傚父锛屽洜涓烘湁涓诲瀛樺湪浣嗘病鏈夋墦寮�富澶囧鍒讹紝鍙堜笉涓�畾鍘讳富搴撴煡锛�
	 * 瀵艰嚧鍙兘鍘诲搴撴煡鑰屾病鏈夋暟鎹紝map鍙栧�涓虹┖锛屾姏鍑哄紓甯搞�杩欎釜鏄甯哥幇璞°�璇存槑 涓诲啓锛屼富澶囧悓鏃舵彁渚涘啓鐨勯厤缃槸鏈夋晥鐨勩�
	 * 
	 * 姝ゅ涓轰簡娴嬭瘯姝ｅ父杩涜锛屾坊鍔燝roupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
	 * 鎸囧畾涓诲簱鏌ヨ
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
		 * hh 涓�2灏忔椂鍒�HH 涓�4灏忔椂鍒�
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
