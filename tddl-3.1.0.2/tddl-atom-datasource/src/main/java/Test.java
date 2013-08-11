import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.tddl.jdbc.atom.StaticTAtomDataSource;

public class Test {
	public static void main(String[] args) throws Exception {
		Log logger = LogFactory.getLog(Test.class);
		StaticTAtomDataSource dataSource = new StaticTAtomDataSource();
		dataSource.setIp("10.13.1.2");
		dataSource.setPort("1521");
		dataSource.setDbName("tddltest");
		dataSource.setDbType("oracle");
		dataSource.setUserName("tddl");
		dataSource.setPasswd("tddl");
		dataSource.setOracleConType("oci");
		dataSource.setIdleTimeout(1000000);
		dataSource.setMinPoolSize(10);
		dataSource.setMaxPoolSize(10);
		dataSource.setSorterClass("OracleExceptionSorter");
		dataSource.setPreparedStatementCacheSize(100);
		Map<String, String> connectionProperties = new HashMap<String, String>();
//		connectionProperties.put("oracle.jdbc.ReadTimeout",""+10000);
		dataSource.setConnectionProperties(connectionProperties);
		// 初始化
		dataSource.init();
		JdbcTemplate jtp = new JdbcTemplate();
		jtp.setDataSource(dataSource);
		int times = 0;
		for(;;){
			
			times++;
			if(times == 100){

				System.out.println("sleep");
				Thread.sleep(60*1000);
				System.out.println("start");
			}
			int actual = 0;
			try {
//				throw new SQLException("asdf");
				actual = jtp.update("insert into test values(?,'str')",new Object[]{1});
			} catch (Exception e) {
				logger.error("",e);
			}
			System.out.println(actual);
		}
	}
}
