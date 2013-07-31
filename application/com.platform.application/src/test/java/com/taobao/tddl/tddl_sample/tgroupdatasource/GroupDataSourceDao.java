package com.taobao.tddl.tddl_sample.tgroupdatasource;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 
 * @author junyu
 *
 */
public class GroupDataSourceDao {
	public static void main(String[] args) {
		JdbcTemplate tddlJT;
		JdbcTemplate fixRwTddlJT;
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:group/spring-context.xml"});
        tddlJT = (JdbcTemplate)context.getBean("tddlDS");
        fixRwTddlJT=(JdbcTemplate)context.getBean("fix_tddlDS");
        GroupDataSourceDao groupDataSourceDao=new GroupDataSourceDao();
        groupDataSourceDao.query(tddlJT);
        groupDataSourceDao.queryWithFixRW(fixRwTddlJT);
        System.exit(0);
	}
	
	public void query(JdbcTemplate tddlJT){
		String sql="select * from modDBTab_0000";
	    List re=tddlJT.queryForList(sql);
	    System.out.println(re.size());
	}
	
	public void queryWithFixRW(JdbcTemplate tddlJT){
		String sql="select * from modDBTab_0000";
	    List re=tddlJT.queryForList(sql);
	    System.out.println(re.size());
	}
}
