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
     * 涓嶈蛋瑙ｆ瀽鍣ㄥ拰璺敱瑙勫垯锛屽湪鎸囧畾鏁版嵁搴撲笂鎵ц鎸囧畾SQL.
     * @param tddlJT
     */
    public void queryByDB(JdbcTemplate tddlJT){
    	String sql="select * from modDbTab_0001";
    	RouteHelper.executeByDB("sample_group_0");
    	List x=tddlJT.queryForList(sql);
    	System.out.println(x.size());
    }
    
    /**
     * 涓嶈蛋瑙ｆ瀽鍣ㄥ拰璺敱瑙勫垯锛屽彧杩涜瀵筍QL杩涜琛ㄥ悕鏇挎崲骞舵墽琛�
     * @param tddlJT
     */
    public void queryByDBAndTab(JdbcTemplate tddlJT){
    	String sql="select * from modDbTab";
    	RouteHelper.executeByDBAndTab("sample_group_0","modDbTab","modDbTab_0000","modDbTab_0001");
    	List x=tddlJT.queryForList(sql);
    	System.out.println(x.size());
    }

    /**
     * 涓嶈蛋瑙ｆ瀽鍣紝璧拌鍒欙紝鎸夌収鎸囧畾鐨勫瓧娈靛拰鍊艰繘琛岃矾鐢便�
     * @param tddlJT
     */
    public void queryByCondition(JdbcTemplate tddlJT){
    	String sql="select * from modDbTab where pk=1";
    	RouteHelper.executeByCondition("modDbTab", "pk", 1);
    	List x=tddlJT.queryForList(sql);
    	System.out.println(x.size());
    }
    
    /**
     * 鍦ㄤ笟鍔＄‘瀹氫袱寮犺〃鍦ㄥ悓涓�釜鏁版嵁搴撲箣涓�鍙鎸囧畾涓ゅ紶琛紝鏃㈠彲浠ヨ繘琛岃仈鍚堟煡璇�
     * @param tddlJT
     */
    public void querByDBAndListTab(JdbcTemplate tddlJT){
    	String sql="select * from modDbTab a,modTab b";
        Map<String/*婧愯〃鍚�/,String/*鏇挎崲鍚庤〃鍚�/> tableMap = new HashMap<String, String>(2);
    	tableMap.put("modDbTab", "modDbTab_0000");
    	tableMap.put("modTab", "modTab_0000");
    	RouteHelper.executeByDBAndTab("sample_group_0", tableMap);
    	List x=tddlJT.queryForList(sql);
    	System.out.println(x.size());
    }
    
    /**
     * 鍙鏋勫缓濡備唬鐮佹墍绀虹殑tableList锛屽嵆鍙墽琛屽悓搴撶殑Join鎿嶄綔銆�
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
     * 鎸囧畾groupdatasource锛坰ample_group_0锛変腑鍒板簳浣跨敤鍝釜搴擄紝濡備笅浠ｇ爜涓璼ample_group_0
     * 涓湁1涓�澶囩殑鏈哄櫒锛岄�杩嘒roupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
     * 鎸囧畾锛岄偅涔圫QL鑲畾浼氬湪group閰嶇疆鐨勭浜屼釜鏁版嵁搴撲笂鎵ц
     * 锛堜竴鑸涓�釜涓轰富搴擄紝鍚庨潰璺熺潃閮戒负澶囧簱锛夈�
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
