package com.taobao.tddl.tddl_sample.basecrud;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.tddl.common.GroupDataSourceRouteHelper;
import com.taobao.tddl.tddl_sample.util.TddlSampleUtils;

/**
 * 
 * 经过TDDL解析器，规则引擎，合并结果3个步骤的标准流程。
 * 
 * @author junyu
 * 
 */
public class BaseCrudSampleDao 
{
    public static void main( String[] args )
    {
	    JdbcTemplate tddlJT;
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:basecrud/spring-context.xml"});
        tddlJT = (JdbcTemplate)context.getBean("tddlDS");
        BaseCrudSampleDao baseCrudSampleDao=new BaseCrudSampleDao();
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
        System.exit(0);
    }
    
    private void insertData(JdbcTemplate tddlJT) throws DataAccessException {
        String sql = "insert into moddbtab (pk,gmt_create) values (?,?)";
        Object[] arguments = new Object[]{1, TddlSampleUtils.getNowTime("yyyy-MM-dd")};
        tddlJT.update(sql, arguments);
    }
  
    public void findData(JdbcTemplate tddlJT) throws DataAccessException{
    	/**
		 * 因为sample_group_0后面挂了2个库(主备),但未开同步(线上必开),
		 * 为了验证,所以这里手动指定在主库上执行
		 */
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
        String sql = "select * from moddbtab where pk = ?";
        Object[] objs = new Object[]{1};
        System.out.println(tddlJT.queryForMap(sql,objs));
    }
 
    public void updateData(JdbcTemplate tddlJT) throws DataAccessException {
        String sql = "update moddbtab set gmt_create = ? where pk = 1";
        Object[] objs = new Object[]{TddlSampleUtils.getNextDay("yyyy-MM-dd")};
        tddlJT.update(sql,objs);
    }
    
    public void deleteData(JdbcTemplate tddlJT) throws DataAccessException{
    	String sql="delete from moddbtab where pk=?";
        Object[] objs = new Object[]{1};
    	tddlJT.update(sql,objs);
    }
}
