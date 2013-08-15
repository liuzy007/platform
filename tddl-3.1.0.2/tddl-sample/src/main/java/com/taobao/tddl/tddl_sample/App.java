package com.taobao.tddl.tddl_sample;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
	JdbcTemplate tddlJT;
        JdbcTemplate realJT1;
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"spring-context.xml"});
	tddlJT = (JdbcTemplate)context.getBean("Tmaster");
        realJT1 = (JdbcTemplate)context.getBean("Treal1");
	insertMaster(tddlJT);
        findTargetByRealJdbcTemplate(realJT1);
        updateMaster(tddlJT);
        findTargetByRealJdbcTemplate(realJT1);
        findTargetByTDDL(tddlJT);
        deleteMaster(tddlJT);
        System.exit(0);

    }
    
    private static void deleteMaster(JdbcTemplate tddlJT) throws DataAccessException {
        String sql = "delete from master where pk = 1";
        tddlJT.update(sql);
    }
    public static void findTargetByTDDL(JdbcTemplate tddlJT) throws DataAccessException{
        String sql = "select * from master where pk = ?";
        System.out.println("this is a query from tddl proxy jdbc");
        Object[] objs = new Object[]{1};
        System.out.println(tddlJT.queryForMap(sql,objs));
    }

    public static void findTargetByRealJdbcTemplate(JdbcTemplate realJT1) throws DataAccessException{
        String sql = "select * from master_1";
        System.out.println("this is a query from real jdbc");
        System.out.println(realJT1.queryForMap(sql));
    }
    private static void updateMaster(JdbcTemplate tddlJT) throws DataAccessException {
        String sql = "update master set ch = 'char new' where pk = 1";
        tddlJT.update(sql);
    }

    private static void insertMaster(JdbcTemplate tddlJT) throws DataAccessException {
        String sql = "insert into master (pk,ch,bigD,bl,da) values (?,?,?,?,?)";
        byte[] bytes = "this is the bytes".getBytes();
        BigDecimal bd = BigDecimal.valueOf(1239999999999999999l);
        Object[] arguments = new Object[]{1, "char", bd, bytes, new Date(109, 02, 22)};
        tddlJT.update(sql, arguments);
    }
}
