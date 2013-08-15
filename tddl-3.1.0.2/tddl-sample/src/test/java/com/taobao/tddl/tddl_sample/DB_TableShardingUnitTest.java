package com.taobao.tddl.tddl_sample;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.taobao.tddl.client.controller.OrderByMessagesImp;
import com.taobao.tddl.client.controller.TargetDBMeta;
import com.taobao.tddl.client.dispatcher.Result;
import com.taobao.tddl.interact.bean.TargetDB;
import com.taobao.tddl.rule.ruleengine.entities.retvalue.TargetDBMetaData;
import com.taobao.tddl.sqlobjecttree.GroupFunctionType;
import com.taobao.tddl.util.dispatchAnalyzer.DatasourceIntrospector;
import com.taobao.tddl.util.dispatchAnalyzer.DispatchAnalyzer;
/**
 * 鎸夌収鏌愪釜鍒楀彇妯″垎搴撳張鍒嗚〃 娴嬭瘯
 * 
 * @author shenxun
 *
 */
public class DB_TableShardingUnitTest {

	static String url ="classpath:spring-tddl-ds.xml";
	DispatchAnalyzer ana = new DispatchAnalyzer();


	static DatasourceIntrospector datasourceIntrospector;

	static ApplicationContext context ;
	
	@BeforeClass
	public static void setUp() throws Exception{
		context = new ClassPathXmlApplicationContext(new String[] { url});
		datasourceIntrospector = (DatasourceIntrospector) context.getBean("datasourceIntrospactor");
	}
	
	@Test
	public void test_鍒嗗簱鍒嗚〃column绛変簬涓�釜缁戝畾鍙橀噺
	() throws Throwable{
		String sql = "select * from mod where id = ?";
		Object[] args = new Object[]{20};
		
		TargetComparator.Bean b = new TargetComparator.Bean();
		b.index = 20 % 1024 / 64;
		b.tableSuffix = new int[]{20 % 64};
		try {
			//isWrite鍐冲畾鏄敤涓诲簱鍒嗗簱瑙勫垯锛岃繕鏄痵lave搴撶殑鍒嗗簱瑙勫垯銆�
			//readWriteRoot 涓诲簱鍒嗗簱瑙勫垯鍜宻lave搴撶殑鍒嗗簱瑙勫垯鏄竴鑷寸殑銆�
			//濡傛灉瀵逛簬涓绘槸master灏忓瀷鏈�浠庢槸slave 鍒嗗簱鍒嗚〃pc server 鐨勫満鏅紝閭ｄ箞isWrite true灏变細閫変富搴撹鍒�
			//false灏变細閫変粠搴撹鍒�
			boolean isWrite = true;
			
			Result result =datasourceIntrospector.getDatabaseChoicer(isWrite).getDBAndTables(sql,Arrays.asList(args));
			TargetDBMeta targetDBMeta = buildResult(result);
			List<TargetDB> target = targetDBMeta.getTarget();
			List<TargetDB> source = TargetComparator.buildTargetDB("dbgroup", "_", "mod", "_", 1, new TargetComparator.Bean[]{b});
			TargetComparator.compare(source, target);
		} catch (Throwable e) {
			StringBuilder sb = new StringBuilder();
			int index = 0;
			for(Object obj :args){
				sb.append(index);
				sb.append(":");
				sb.append(obj);
				sb.append("|");
			}
			System.out.println(sql+" | "+sb.toString());
			throw e;
		}
		
	}
	
	private TargetDBMeta buildResult(Result result) {
		TargetDBMetaData meta = new TargetDBMetaData(result.getVirtualTableName().toString(), result.getTarget(), false, false);
		return new TargetDBMeta(meta, result.getSkip(),result.getMax(),new OrderByMessagesImp(Collections.EMPTY_LIST),GroupFunctionType.NORMAL);
	}
	
	
}
