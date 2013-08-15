package com.taobao.tddl.tddl_sample.extra_parameter_context;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 鍖呭惈ExtraParameterContext鐨勪笂涓嬫枃鐨勬牱渚�
 * @author xudanhui.pt
 *2010-10-18,涓嬪崍04:07:30
 */
public class ExtraParameterContextSampleDao 
{
    public static void main( String[] args )
    {
	    JdbcTemplate tddlJT;
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:extra_parameter_context/spring-context.xml"});
        tddlJT = (JdbcTemplate)context.getBean("tddlDS");
        ExtraParameterContextSampleDao baseCrudSampleDao=new ExtraParameterContextSampleDao();
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
        Object[] arguments = new Object[]{1, getNowTime()};
        tddlJT.update(sql, arguments);
    }
  
    /**
     * 杩欎釜鏂规硶鍙兘浼氭姤寮傚父锛屽洜涓烘湁涓诲瀛樺湪浣嗘病鏈夋墦寮�富澶囧鍒讹紝鍙堜笉涓�畾鍘讳富搴撴煡锛�
     * 瀵艰嚧鍙兘鍘诲搴撴煡鑰屾病鏈夋暟鎹紝map鍙栧�涓虹┖锛屾姏鍑哄紓甯搞�杩欎釜鏄甯哥幇璞°�璇存槑
     * 涓诲啓锛屼富澶囧悓鏃舵彁渚涘啓鐨勯厤缃槸鏈夋晥鐨勩�
     * 
     * @param tddlJT
     * @throws DataAccessException
     */
    public void findData(JdbcTemplate tddlJT) throws DataAccessException{
        String sql = "select * from moddbtab where pk = ?";
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

	private String getNextDay() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		Date next = cal.getTime();
		return this.format(next);
	}

	private String format(Date date) {
		/**
		 * hh 涓�2灏忔椂鍒�HH 涓�4灏忔椂鍒�
		 * 
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return sdf.format(date);
	}
}
