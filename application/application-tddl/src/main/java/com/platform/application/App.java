package com.platform.application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.taobao.tddl.jdbc.group.TGroupConnection;
import com.taobao.tddl.jdbc.group.TGroupDataSource;

public class App {/*
				 * public static void main(String[] args) throws SQLException {
				 * ApplicationContext context = new
				 * ClassPathXmlApplicationContext( "failover-beans.xml");
				 * DataSource datasource = (DataSource)
				 * context.getBean("master_tddl_ds"); ResultSet result =
				 * (ResultSet) datasource.getConnection()
				 * .createStatement().executeQuery
				 * ("select id,sid,bid from item where sid=123"); int
				 * columnCount = result.getMetaData().getColumnCount(); while
				 * (result.next()) { for (int c = 1; c <= columnCount; c++) {
				 * System.out.print(result.getObject(c)); System.out.print(",");
				 * } System.out.println(); } int rowCount = result.getRow();
				 * System.out.println("rowCount:" + rowCount + "columnCount:" +
				 * columnCount); }
				 */

	public static void _main(String[] args) throws SQLException {
		// System.out.println( "Hello World!" );
		/*
		 * ApplicationContext context = new ClassPathXmlApplicationContext(
		 * "failover-beans.xml");
		 */
		TGroupDataSource datasource = new TGroupDataSource();
		datasource.setAppName("B2B_CBUUMP_TEST_APP");
		datasource.setDbGroupKey("B2B_CBUUMP_TEST_GROUP");
		datasource.init();

		System.out.println(datasource);
		// ResultSet result = (ResultSet) datasource.getConnection()
		// .createStatement().executeUpdate("insert into tbb_order_3(seller_id,child_order_id,id,buyer_id,biz_type,parent_order_id,order_created,gmt_created,gmt_modified) values (1,1,0,123,123,456,now(),now(),now())");
		// datasource.getConnection()
		// .createStatement().executeUpdate("insert into tbb_order_3(seller_id,child_order_id,id,buyer_id,biz_type,parent_order_id,order_created,gmt_created,gmt_modified) values (1,1,0,123,123,456,now(),now(),now())");
		TGroupConnection conn = datasource.getConnection();
		// int rtnValue =
		// conn.createStatement().executeUpdate("insert into t(name) values('abc'); ");
		ResultSet rs = conn.createStatement().executeQuery(
				"show tables  ");
		while (rs.next()) {
			System.out.println(rs.getObject(1).toString());
		}
		conn.close();
		/*
		 * int columnCount = result.getMetaData().getColumnCount(); while
		 * (result.next()) { for (int c = 1; c <= columnCount; c++) {
		 * System.out.print(result.getObject(c)); System.out.print(","); }
		 * System.out.println(); } int rowCount = result.getRow();
		 * 
		 * System.out.println("rowCount:" + rowCount + "columnCount:" +
		 * columnCount);
		 */
	}

	public static void ___main(String[] args) throws SQLException, IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"failover-beans.xml");
		Object o = context.getBean("druidDS");
		DataSource datasource = (DataSource) o;
		Connection conn = datasource.getConnection();
		ResultSet rs = conn.createStatement().executeQuery(
				"select table_name from tabs ");
		while (rs.next()) {
			System.out.println(rs.getObject(1).toString());
		}
		rs.close();
		conn.close();
		System.in.read();
		System.out.println("maxWait:");
//		System.out.println( ((com.alibaba.druid.pool.DruidDataSource)o).getMaxWait() );
	}
	public static void main(String[] args) throws SQLException, IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"failover-beans.xml");
		Object o = context.getBean("master_tddl_ds");
		DataSource datasource = (DataSource) o;
		Connection conn = datasource.getConnection();
		
		String[] user_id_arr = new String[17] ; 
		user_id_arr[0] = "         2061504523";
		user_id_arr[1] = "  53011157274155008";
		user_id_arr[2] = " 410312957145302016";
		user_id_arr[3] = "1103689863703711744";
		user_id_arr[4] = "1630145568905382912";
		user_id_arr[5] = "2314173823219386368";
		user_id_arr[6] = "2525887250122893312";
		user_id_arr[7] = "2799927070518916096";
		user_id_arr[8] = "4535964459950701568";
		user_id_arr[9] = "5300497711521393664";
		user_id_arr[10] =" 5776379069747778560";
		user_id_arr[11] =" 6911716173695711232";
		user_id_arr[12] =" 7060994032839130112";
		user_id_arr[13] =" 7295629917752952832";
		user_id_arr[14] =" 8100963394616906752";
		user_id_arr[15] =" 8449098432184596480";
		user_id_arr[16] = "2040185608";

				
		//for (int user_id=0 ; user_id < 3072 ; user_id++ ) {
		/*
		for (int i=0 ; i < 17 ; i++ ) {
			ResultSet rs = conn.createStatement().executeQuery(
					"select count(*) from ATAS_USER_TASK_STATUS where user_id="+user_id_arr[i]);
			while (rs.next()) {
				System.out.println(rs.getObject(1).toString());
			}
			rs.close();
		}
		*/
		conn.createStatement().executeUpdate(" INSERT INTO ATAS_USER_TASK_STATUS(ID, USER_ID, TASK_ID, STATUS, GMT_CREATE, GMT_MODIFIED ) VALUES(0, "+"2040185608"+" , '0' , '0', NOW(), NOW() )");
		conn.close();
	}
}