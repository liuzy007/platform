//Copyright(c) Taobao.com
package com.taobao.tddl.tddl_sample.newrule;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.tddl.common.GroupDataSourceRouteHelper;
import com.taobao.tddl.tddl_sample.util.TddlSampleUtils;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a>
 * @version 1.0
 * @since 1.6
 * @date 2011-5-27涓嬪崍01:15:17
 */
public class NewRuleSample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JdbcTemplate tddlJT;
		ApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "classpath:rule_2_4_4/spring-context.xml" });
		tddlJT = (JdbcTemplate) context.getBean("tddlDS");
		NewRuleSample newRuleSample = new NewRuleSample();
		try {
			newRuleSample.deleteData(tddlJT);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		
		try {
			newRuleSample.insertData(tddlJT);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}

		try {
			newRuleSample.updateData(tddlJT);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}

		try {
			newRuleSample.findData(tddlJT);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}

		try {
			newRuleSample.deleteData(tddlJT);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void insertData(JdbcTemplate tddlJT) {
		String sql = "insert into moddbtab (pk,gmt_create) values (?,?)";
        Object[] arguments = new Object[]{1, TddlSampleUtils.getNowTime("yyyy-MM-dd")};
        tddlJT.update(sql, arguments);
	}

	public void updateData(JdbcTemplate tddlJT) {
        String sql = "update moddbtab set gmt_create = ? where pk = 1";
        Object[] objs = new Object[]{TddlSampleUtils.getNextDay("yyyy-MM-dd")};
        tddlJT.update(sql,objs);
	}

	public void findData(JdbcTemplate tddlJT) {
		/**
		 * 鍥犱负sample_group_0鍚庨潰鎸備簡2涓簱(涓诲),浣嗘湭寮�悓姝�绾夸笂蹇呭紑),
		 * 涓轰簡楠岃瘉,鎵�互杩欓噷鎵嬪姩鎸囧畾鍦ㄤ富搴撲笂鎵ц
		 */
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
		String sql = "select * from moddbtab where pk = ?";
        Object[] objs = new Object[]{1};
        System.out.println(tddlJT.queryForMap(sql,objs));
	}

	public void deleteData(JdbcTemplate tddlJT) {
		String sql="delete from moddbtab where pk=?";
        Object[] objs = new Object[]{1};
    	tddlJT.update(sql,objs);
	}
}
