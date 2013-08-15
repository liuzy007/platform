//Copyright(c) Taobao.com
package com.taobao.tddl.tddl_sample.usediffconfig;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.taobao.tddl.client.jdbc.TDataSource;
import com.taobao.tddl.util.RouteHelper;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a>
 * @version 1.0
 * @since 1.6
 * @date 2011-1-13涓嬪崍04:07:35
 */
public class DataHandlerTest {
	protected static JdbcTemplate tddlJT;
	protected static TDataSource tds;
	public static final String appName = "tddl_sample_real";

	public void init() {
		tds = new TDataSource();
		tds.setAppName(appName);
		tds.setAppRuleFile("classpath:usediffconfig/tddl-rule.xml");
		tds.init();

		tddlJT = new JdbcTemplate(tds);

		RouteHelper.executeByDB("tddl_group_0");
		tddlJT.update("delete from soccerplayer_0000");

		RouteHelper.executeByDB("tddl_group_0");
		tddlJT.update("delete from soccerplayer_0001");

		RouteHelper.executeByDB("tddl_group_1");
		tddlJT.update("delete from soccerplayer_0002");

		RouteHelper.executeByDB("tddl_group_1");
		tddlJT.update("delete from soccerplayer_0003");

		String sql = "INSERT INTO soccerplayer (pk,name,bigdecimal,floatx,boolean,doublex,longx,integerx,bytex,datex,timestampx,timex) VALUES(?,?,?,?,?,?,?,?,?,?,?,?);";
		Object[] obj = new Object[] { 1, "c.ronaldor", 1111111.11111, 1111.11,
				true, 1111111.11, 11111111, 111, new Byte[] { 123 },
				"2010-11-11", "2010-11-11 11:11:11", "11:11:11" };
		tddlJT.update(sql, obj);

		String sql2 = "INSERT INTO soccerplayer (pk,name,bigdecimal,floatx,boolean,doublex,longx,integerx,bytex,datex,timestampx,timex) VALUES(?,?,?,?,?,?,?,?,?,?,?,?);";
		Object[] obj2 = new Object[] { 2, "messi", 2222222.22222, 2222.22, 0,
				2222222.22, 22222222, 122, new Byte[] { 111 }, "2012-12-12",
				"2012-12-12 12:12:12", "12:12:12" };
		tddlJT.update(sql2, obj2);

		String sql3 = "INSERT INTO soccerplayer (pk,name,bigdecimal,floatx,boolean,doublex,longx,integerx,bytex,datex,timestampx,timex) VALUES(?,?,?,?,?,?,?,?,?,?,?,?);";
		Object[] obj3 = new Object[] { 3, "droba", 1111110.11111, 1110.11, 1,
				1111110.11, 11111110, 110, new Byte[] { 100 }, "2010-10-10",
				"2010-10-10 10:10:10", "10:10:10" };
		tddlJT.update(sql3, obj3);

		String sql4 = "INSERT INTO soccerplayer (pk,name,bigdecimal,floatx,boolean,doublex,longx,integerx,bytex,datex,timestampx,timex) VALUES(?,?,?,?,?,?,?,?,?,?,?,?);";
		Object[] obj4 = new Object[] { 4, "kaka", 2222224.22222, 2224.22, 0,
				2222224.22, 22222224, 102, new Byte[] { 121 }, "2019-11-11",
				"2019-11-11 09:09:09", "09:09:09" };
		tddlJT.update(sql4, obj4);

		String sql5 = "INSERT INTO soccerplayer (pk,name,bigdecimal,floatx,boolean,doublex,longx,integerx,bytex,datex,timestampx,timex) VALUES(?,?,?,?,?,?,?,?,?,?,?,?);";
		Object[] obj5 = new Object[] { 5, "lampad", 1111120.11111, 1120.11, 1,
				1111120.11, 11111120, 100, new Byte[] { 102 }, "2008-08-08",
				"2008-08-08 08:08:08", "08:08:08" };
		tddlJT.update(sql5, obj5);

		String sql6 = "INSERT INTO soccerplayer (pk,name,bigdecimal,floatx,boolean,doublex,longx,integerx,bytex,datex,timestampx,timex) VALUES(?,?,?,?,?,?,?,?,?,?,?,?);";
		Object[] obj6 = new Object[] { 6, "roony", 2222214.22222, 2214.22, 0,
				2222214.22, 22222214, 112, new Byte[] { 103 }, "2020-10-10",
				"2019-11-11 09:09:09", "09:09:09" };
		tddlJT.update(sql6, obj6);

		String sql7 = "INSERT INTO soccerplayer (pk,name,bigdecimal,floatx,boolean,doublex,longx,integerx,bytex,datex,timestampx,timex) VALUES(?,?,?,?,?,?,?,?,?,?,?,?);";
		Object[] obj7 = new Object[] { 7, "snejr", 1111320.11111, 1320.11, 1,
				1113120.11, 11113120, 90, new Byte[] { 104 }, "2007-08-08",
				"2007-08-08 08:08:08", "06:08:08" };
		tddlJT.update(sql7, obj7);

		String sql8 = "INSERT INTO soccerplayer (pk,name,bigdecimal,floatx,boolean,doublex,longx,integerx,bytex,datex,timestampx,timex) VALUES(?,?,?,?,?,?,?,?,?,?,?,?);";
		Object[] obj8 = new Object[] { 8, "kaxi", 1222214.22222, 1214.22, 0,
				1222214.22, 12222214, 103, new Byte[] { 105 }, "2021-09-09",
				"2021-11-11 09:09:09", "14:09:09" };
		tddlJT.update(sql8, obj8);
	}

	public void doDbAction() {
		String sql = "select * from soccerplayer where pk in (?,?,?,?)";
		RouteHelper.executeWithParallel(true);
		tddlJT.query(sql, new Object[] { 1, 3, 4, 7 },
				new ResultSetExtractor() {
					public Object extractData(ResultSet rs)
							throws SQLException, DataAccessException {

						while (rs.next()) {
							System.out.println(rs.getString("name"));
						}

						rs.close();

						return null;
					}
				});
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataHandlerTest dht=new DataHandlerTest();
		dht.init();
		dht.doDbAction();
	}

}
