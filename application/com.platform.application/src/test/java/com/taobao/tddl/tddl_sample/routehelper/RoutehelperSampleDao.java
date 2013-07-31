package com.taobao.tddl.tddl_sample.routehelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.tddl.common.GroupDataSourceRouteHelper;
import com.taobao.tddl.util.RouteHelper;
/**
 * 
 * @author junyu
 *
 */
public class RoutehelperSampleDao {
    public static void main(String[] args){
    	JdbcTemplate tddlJT;
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:basecrud/spring-context.xml"});
        tddlJT = (JdbcTemplate)context.getBean("tddlDS");
    	RoutehelperSampleDao sample=new RoutehelperSampleDao();
    	sample.queryByDB(tddlJT);
    	sample.queryByDBAndTab(tddlJT);
    	sample.queryByCondition(tddlJT);
    	sample.querByDBAndListTab(tddlJT);
    	sample.querByDBAndListTabJoin(tddlJT);
    	sample.queryByDBIndexInGroupDataSource(tddlJT);
    }
    
    /**
     * 不走解析器和路由规则，在指定数据库上执行指定SQL.
     * @param tddlJT
     */
    public void queryByDB(JdbcTemplate tddlJT){
    	String sql="select * from modDbTab_0001";
    	RouteHelper.executeByDB("sample_group_0");
    	List x=tddlJT.queryForList(sql);
    	System.out.println(x.size());
    }
    
    /**
     * 不走解析器和路由规则，只进行对SQL进行表名替换并执行
     * @param tddlJT
     */
    public void queryByDBAndTab(JdbcTemplate tddlJT){
    	String sql="select * from modDbTab";
    	RouteHelper.executeByDBAndTab("sample_group_0","modDbTab","modDbTab_0000","modDbTab_0001");
    	List x=tddlJT.queryForList(sql);
    	System.out.println(x.size());
    }

    /**
     * 不走解析器，走规则，按照指定的字段和值进行路由。
     * @param tddlJT
     */
    public void queryByCondition(JdbcTemplate tddlJT){
    	String sql="select * from modDbTab where pk=1";
    	RouteHelper.executeByCondition("modDbTab", "pk", 1);
    	List x=tddlJT.queryForList(sql);
    	System.out.println(x.size());
    }
    
    /**
     * 在业务确定两张表在同一个数据库之中,只要指定两张表，既可以进行联合查询
     * @param tddlJT
     */
    public void querByDBAndListTab(JdbcTemplate tddlJT){
    	String sql="select * from modDbTab a,modTab b";
        Map<String/*源表名*/,String/*替换后表名*/> tableMap = new HashMap<String, String>(2);
    	tableMap.put("modDbTab", "modDbTab_0000");
    	tableMap.put("modTab", "modTab_0000");
    	RouteHelper.executeByDBAndTab("sample_group_0", tableMap);
    	List x=tddlJT.queryForList(sql);
    	System.out.println(x.size());
    }
    
    /**
     * 只要构建如代码所示的tableList，即可执行同库的Join操作。
     * @param tddlJT
     */
    public void querByDBAndListTabJoin(JdbcTemplate tddlJT){
    	String sql="select * from modDbTab a,modTab b where a.pk=b.pk";
    	String[] as = new String[] { "modDbTab_0000", "modDbTab_0001" };
		String[] bs = new String[] { "modTab_0000", "modTab_0001" };
		ArrayList<Map<String, String>> tableList=new ArrayList<Map<String,String>>(2);
		for (String a : as) {
			for (String b : bs) {
				Map<String, String> map = new HashMap<String, String>(2);
				map.put("modDbTab", a);
				map.put("modTab", b);
                tableList.add(map);  
			}
		}
		RouteHelper.executeByDBAndTab("sample_group_0", tableList);
		List re = tddlJT.queryForList(sql);
    	System.out.println(re.size());
    }
    
    /**
     * 指定groupdatasource（sample_group_0）中到底使用哪个库，如下代码中sample_group_0
     * 中有1主1备的机器，通过GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
     * 指定，那么SQL肯定会在group配置的第二个数据库上执行
     * （一般第一个为主库，后面跟着都为备库）。
     * @param tddlJT
     */
    public void queryByDBIndexInGroupDataSource(JdbcTemplate tddlJT){
    	GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(1);
    	String sql="select * from modDbTab_0000";
    	RouteHelper.executeByDB("sample_group_0");
    	List x=tddlJT.queryForList(sql);
    	System.out.println(x.size());
    }
}
