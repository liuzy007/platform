//Copyright(c) Taobao.com
package com.taobao.tddl.tddl_sample.hint;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
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
 * @date 2011-5-27下午01:15:01
 */
public class TDDLHintSample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 JdbcTemplate tddlJT;
	        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:hint/spring-context.xml"});
	        tddlJT = (JdbcTemplate)context.getBean("tddlDS");
	        TDDLHintSample tddlHintSample=new TDDLHintSample();
	        try {
	        	tddlHintSample.executeByDB(tddlJT);
			} catch (DataAccessException e) {
				e.printStackTrace();
			}
			
	        try {
	        	tddlHintSample.executeByDBAndTab(tddlJT);
			} catch (DataAccessException e) {
				e.printStackTrace();
			}
			
	        try {
	        	tddlHintSample.executeByCondition(tddlJT);
			} catch (DataAccessException e) {
				e.printStackTrace();
			}
		    
	        System.exit(0);
	}
	
	/**
	 * 1. 指定一个group_id,将sql直接在这个group_ds上
	 *    执行掉, sql将不走解析,也不走规则.
	 *    相当于绕开了整个分库分表
	 * 2. TDDL hint必需放在sql最前方
	 * 3. TDDL hint中也可以用?号占位,ibatis用#paramName#或者$paramName$占位
	 */
	@SuppressWarnings("rawtypes")
	public void executeByDB(JdbcTemplate tddlJT){
		String deleteSql = "/*+TDDL({type:?,dbId:?})*/delete from moddbtab_0000 where pk=?";
		Object[] objs = new Object[] { "executeByDB","sample_group_0",1 };
		tddlJT.update(deleteSql, objs);

		String insertSql = "/*+TDDL({type:?,dbId:?})*/insert into moddbtab_0000 (pk,gmt_create) values (?,?)";
		String time = TddlSampleUtils.getNowTime("yyyy-MM-dd");
		Object[] objs2 = new Object[] {"executeByDB","sample_group_0",1, time };
		tddlJT.update(insertSql, objs2);

		String updateSql = "/*+TDDL({type:?,dbId:?})*/update moddbtab_0000 set gmt_create=? where pk=?";
		String nextDay = TddlSampleUtils.getNextDay("yyyy-MM-dd");
		Object[] objs3 = new Object[] { "executeByDB","sample_group_0",nextDay, 1 };
		int affectedRow = tddlJT.update(updateSql, objs3);
		Assert.assertEquals(1, affectedRow);

		/**
		 * 因为sample_group_0后面挂了2个库(主备),但未开同步(线上必开),
		 * 为了验证,所以这里手动指定在主库上执行
		 */
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
		String querySql = "/*+TDDL({type:?,dbId:?})*/select * from moddbtab_0000 where pk=?";
		Object[] objs4 = new Object[] { "executeByDB","sample_group_0",1};
		Map re = tddlJT.queryForMap(querySql, objs4);
		Assert.assertEquals(nextDay, String.valueOf(re.get("gmt_create")));
	}

	/**
	 * 1. 指定一个group_id,并且指定具体表,sql不经过解析和规则计算,但是
	 *    会进行表名替换,然后将表名替换后的sql在指定group_ds上执行掉
	 * 2. TDDL hint必需放在sql最前方
	 * 3. TDDL hint中也可以用?号占位,ibatis用#paramName#或者$paramName$占位
	 */
	@SuppressWarnings("rawtypes")
	public void executeByDBAndTab(JdbcTemplate tddlJT){
		String deleteSql = "/*+TDDL({type:?,tables:[?,?,?,?],dbId:?,virtualTableName:moddbtab})*/delete from moddbtab where pk=?";
		Object[] objs = new Object[] {"executeByDBAndTab","moddbtab_0000","moddbtab_0001","moddbtab_0002","moddbtab_0003","sample_group_1",1 };
		tddlJT.update(deleteSql, objs);

		String insertSql = "/*+TDDL({type:?,tables:[?,?,?,?],dbId:?,virtualTableName:moddbtab})*/insert into moddbtab (pk,gmt_create) values (?,?)";
		String time = TddlSampleUtils.getNowTime("yyyy-MM-dd");
		Object[] objs2 = new Object[] {"executeByDBAndTab","moddbtab_0000","moddbtab_0001","moddbtab_0002","moddbtab_0003","sample_group_1", 1, time };
		tddlJT.update(insertSql, objs2);

		String updateSql = "/*+TDDL({type:?,tables:[?,?,?,?],dbId:?,virtualTableName:moddbtab})*/update moddbtab set gmt_create=? where pk=?";
		String nextDay = TddlSampleUtils.getNextDay("yyyy-MM-dd");
		Object[] objs3 = new Object[] {"executeByDBAndTab","moddbtab_0000","moddbtab_0001","moddbtab_0002","moddbtab_0003","sample_group_1",nextDay, 1 };
		int affectedRow = tddlJT.update(updateSql, objs3);
		Assert.assertEquals(4, affectedRow);

		/**
		 * 因为sample_group_1后面挂了2个库(主备),但未开同步(线上必开),
		 * 为了验证,所以这里手动指定在主库上执行
		 */
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
		String querySql = "/*+TDDL({type:executeByDB,dbId:sample_group_1})*/select * from moddbtab_0000 where pk=?";
		Object[] objs4 = new Object[] { 1 };
		Map re = tddlJT.queryForMap(querySql, objs4);
		Assert.assertEquals(nextDay, String.valueOf(re.get("gmt_create")));

		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
		String querySql1 = "/*+TDDL({type:executeByDB,dbId:sample_group_1})*/select * from moddbtab_0001 where pk=?";
		Map re5 = tddlJT.queryForMap(querySql1, objs4);
		Assert.assertEquals(nextDay, String.valueOf(re5.get("gmt_create")));

		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
		String querySql2 = "/*+TDDL({type:executeByDB,dbId:sample_group_1})*/select * from moddbtab_0002 where pk=?";
		Map re6 = tddlJT.queryForMap(querySql2, objs4);
		Assert.assertEquals(nextDay, String.valueOf(re6.get("gmt_create")));

		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
		String querySql3 = "/*+TDDL({type:executeByDB,dbId:sample_group_1})*/select * from moddbtab_0003 where pk=?";
		Map re7 = tddlJT.queryForMap(querySql3, objs4);
		Assert.assertEquals(nextDay, String.valueOf(re7.get("gmt_create")));
	}
	
	/**
	 * 
	 * 1. 指定一个分库分表条件,sql不经过解析,但进行规则计算,这样
	 *    就能满足一种你不想把分库分表条件放到sql中,但是仍然想
	 *    按那个条件路由到指定库中
	 * 2. 相同分库分表条件一般用 'and'或者'or'连接(如示例中的pk),
	 *    不同的条件用','号隔开,即单独成为,parameters的一个元素,
	 *    如parameters:["pk>=?;int and pk<=?;int","gmt_create < ?:date"]
	 * 3. 现在参数类型后缀主要有 'int','long','string','date'4种,
	 *    也可以分别简写成'i','l','s','d'
	 * 4. TDDL hint必需放在sql最前方
	 * 5. TDDL hint中也可以用?号占位,ibatis用#paramName#或者$paramName$占位
	 *
	 */
	@SuppressWarnings("rawtypes")
	public void executeByCondition(JdbcTemplate tddlJT){
		String deleteSql="/*+TDDL({type:executeByCondition,parameters:[\"pk>=?;int and pk<=?;int\"],virtualTableName:moddbtab})*/delete from moddbtab where pk=?";
		Object[] objs=new Object[]{4,5,1};
    	tddlJT.update(deleteSql,objs);
    	
    	String insertSql = "/*+TDDL({type:executeByCondition,parameters:[\"pk>=?;int and pk<=?;int\"],virtualTableName:moddbtab})*/insert into moddbtab (pk,gmt_create) values (?,?)";
    	String time =  TddlSampleUtils.getNowTime("yyyy-MM-dd");
		Object[] objs2 = new Object[] {4,5,1,time};
		tddlJT.update(insertSql, objs2);
		
		String querySql="/*+TDDL({type:?,parameters:[\"pk>=?;int and pk<=?;int\"],virtualTableName:moddbtab})*/select * from moddbtab where pk=?";
		Object[] objs4=new Object[]{"executeByCondition",4,5,1};
		List re=tddlJT.queryForList(querySql,objs4);
		Assert.assertEquals(re.size(), 2);
	}
}
