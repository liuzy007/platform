package com.taobao.tddl.jdbc.group.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetStringHandler implements ParameterHandler {
	public void setParameter(PreparedStatement stmt, Object[] args)
			throws SQLException {
		stmt.setString((Integer) args[0], (String) args[1]);
	}
}
