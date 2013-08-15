package com.taobao.tddl.tddl_sample.tatomdatasource;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 
 * @author junyu
 *
 */
public class AtomDataSourceDao {
	public static void main(String[] args) {
		JdbcTemplate tddlJT;
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:atom/spring-context.xml"});
        tddlJT = (JdbcTemplate)context.getBean("tddlDS");
        AtomDataSourceDao atomDataSourceDao=new AtomDataSourceDao();
        atomDataSourceDao.query(tddlJT);
        System.exit(0);
	}
	
	public static void query(JdbcTemplate tddlJT){
	    String sql="select * from modDBTab_0001";
	    List re=tddlJT.queryForList(sql);
	    System.out.println(re.size());
	}

}
