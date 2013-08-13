package com.platform.tddl.dahuang;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.taobao.tddl.jdbc.group.TGroupConnection;
import com.taobao.tddl.jdbc.group.TGroupDataSource;

public class Test {

	/**
	 * @param args
	 */
	protected static TGroupDataSource tds;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		tds = new TGroupDataSource();
		tds.setAppName("CBU_ATAS_APP");
		tds.setDbGroupKey("ATAS_GROUP");
		tds.init();
		 try {
			TGroupConnection conn = tds.getConnection();
			conn.createStatement();
			ResultSet rs = conn.createStatement().executeQuery(
					"show tables  ");
			while (rs.next()) {
				System.out.println(rs.getObject(1).toString());
			}
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
