package com.taobao.tddl.tddl_sample.pipeline.basecrud;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.tddl.common.GroupDataSourceRouteHelper;
/**
 * 
 * 经过TDDL解析器，规则引擎，合并结果3个步骤的标准流程。
 * @author junyu
 *
 */
public class BaseCrudSampleDao 
{
    public static void main( String[] args )
    {
	    JdbcTemplate tddlJT;
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:pipeline/spring-context.xml"});
        tddlJT = (JdbcTemplate)context.getBean("tddlDS");
        BaseCrudSampleDao baseCrudSampleDao=new BaseCrudSampleDao();
        baseCrudSampleDao.insertData(tddlJT);
        baseCrudSampleDao.updateData(tddlJT);
        baseCrudSampleDao.findData(tddlJT);
        baseCrudSampleDao.deleteData(tddlJT);
        System.exit(0);
    }
    
    private void insertData(JdbcTemplate tddlJT) throws DataAccessException {
        String sql = "insert into moddbtab (pk,gmt_create) values (?,?)";
        Object[] arguments = new Object[]{1, getNowTime()};
        tddlJT.update(sql, arguments);
    }
  
    /**
     * 这个方法可能会报异常，因为有主备存在但没有打开主备复制，又不一定去主库查，
     * 导致可能去备库查而没有数据，map取值为空，抛出异常。这个是正常现象。说明
     * 主写，主备同时提供写的配置是有效的。
     * 
     * @param tddlJT
     * @throws DataAccessException
     */
    public void findData(JdbcTemplate tddlJT) throws DataAccessException{
        String sql = "select * from moddbtab where pk = ?";
        GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
        Object[] objs = new Object[]{1};
        System.out.println(tddlJT.queryForMap(sql,objs));
    }
 
    public void updateData(JdbcTemplate tddlJT) throws DataAccessException {
        String sql = "update moddbtab set gmt_create = ? where pk = 1";
        Object[] objs = new Object[]{this.getNextDay()};
        tddlJT.update(sql,objs);
    }
    
    public void deleteData(JdbcTemplate tddlJT) throws DataAccessException{
    	String sql="delete from moddbtab where pk=?";
        Object[] objs = new Object[]{1};
    	tddlJT.update(sql,objs);
    }
  
    private String getNowTime() {
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		
		return this.format(now);
	}
    
    private String getNextDay(){
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);
    	Date next=cal.getTime();
    	return this.format(next);
    }
    
    private String format(Date date){
    	/**
		 * hh 为12小时制 HH 为24小时制
		 *
		 */
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	return sdf.format(date);
    }
}
