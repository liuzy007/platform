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
 * 缁忚繃TDDL瑙ｆ瀽鍣紝瑙勫垯寮曟搸锛屽悎骞剁粨鏋�涓楠ょ殑鏍囧噯娴佺▼銆�
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
		 * 鍥犱负sample_group_0鍚庨潰鎸備簡2涓簱(涓诲),浣嗘湭寮�悓姝�绾夸笂蹇呭紑),
		 * 涓轰簡楠岃瘉,鎵�互杩欓噷鎵嬪姩鎸囧畾鍦ㄤ富搴撲笂鎵ц
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
