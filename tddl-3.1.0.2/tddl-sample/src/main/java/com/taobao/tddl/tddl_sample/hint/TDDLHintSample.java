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
 * @date 2011-5-27涓嬪崍01:15:01
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
	 * 1. 鎸囧畾涓�釜group_id,灏唖ql鐩存帴鍦ㄨ繖涓猤roup_ds涓�
	 *    鎵ц鎺� sql灏嗕笉璧拌В鏋�涔熶笉璧拌鍒�
	 *    鐩稿綋浜庣粫寮�簡鏁翠釜鍒嗗簱鍒嗚〃
	 * 2. TDDL hint蹇呴渶鏀惧湪sql鏈�墠鏂�
	 * 3. TDDL hint涓篃鍙互鐢�鍙峰崰浣�ibatis鐢�paramName#鎴栬�$paramName$鍗犱綅
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
		 * 鍥犱负sample_group_0鍚庨潰鎸備簡2涓簱(涓诲),浣嗘湭寮�悓姝�绾夸笂蹇呭紑),
		 * 涓轰簡楠岃瘉,鎵�互杩欓噷鎵嬪姩鎸囧畾鍦ㄤ富搴撲笂鎵ц
		 */
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
		String querySql = "/*+TDDL({type:?,dbId:?})*/select * from moddbtab_0000 where pk=?";
		Object[] objs4 = new Object[] { "executeByDB","sample_group_0",1};
		Map re = tddlJT.queryForMap(querySql, objs4);
		Assert.assertEquals(nextDay, String.valueOf(re.get("gmt_create")));
	}

	/**
	 * 1. 鎸囧畾涓�釜group_id,骞朵笖鎸囧畾鍏蜂綋琛�sql涓嶇粡杩囪В鏋愬拰瑙勫垯璁＄畻,浣嗘槸
	 *    浼氳繘琛岃〃鍚嶆浛鎹�鐒跺悗灏嗚〃鍚嶆浛鎹㈠悗鐨剆ql鍦ㄦ寚瀹歡roup_ds涓婃墽琛屾帀
	 * 2. TDDL hint蹇呴渶鏀惧湪sql鏈�墠鏂�
	 * 3. TDDL hint涓篃鍙互鐢�鍙峰崰浣�ibatis鐢�paramName#鎴栬�$paramName$鍗犱綅
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
		 * 鍥犱负sample_group_1鍚庨潰鎸備簡2涓簱(涓诲),浣嗘湭寮�悓姝�绾夸笂蹇呭紑),
		 * 涓轰簡楠岃瘉,鎵�互杩欓噷鎵嬪姩鎸囧畾鍦ㄤ富搴撲笂鎵ц
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
	 * 1. 鎸囧畾涓�釜鍒嗗簱鍒嗚〃鏉′欢,sql涓嶇粡杩囪В鏋�浣嗚繘琛岃鍒欒绠�杩欐牱
	 *    灏辫兘婊¤冻涓�浣犱笉鎯虫妸鍒嗗簱鍒嗚〃鏉′欢鏀惧埌sql涓�浣嗘槸浠嶇劧鎯�
	 *    鎸夐偅涓潯浠惰矾鐢卞埌鎸囧畾搴撲腑
	 * 2. 鐩稿悓鍒嗗簱鍒嗚〃鏉′欢涓�埇鐢�'and'鎴栬�'or'杩炴帴(濡傜ず渚嬩腑鐨刾k),
	 *    涓嶅悓鐨勬潯浠剁敤','鍙烽殧寮�鍗冲崟鐙垚涓�parameters鐨勪竴涓厓绱�
	 *    濡俻arameters:["pk>=?;int and pk<=?;int","gmt_create < ?:date"]
	 * 3. 鐜板湪鍙傛暟绫诲瀷鍚庣紑涓昏鏈�'int','long','string','date'4绉�
	 *    涔熷彲浠ュ垎鍒畝鍐欐垚'i','l','s','d'
	 * 4. TDDL hint蹇呴渶鏀惧湪sql鏈�墠鏂�
	 * 5. TDDL hint涓篃鍙互鐢�鍙峰崰浣�ibatis鐢�paramName#鎴栬�$paramName$鍗犱綅
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
