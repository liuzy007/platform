package com.taobao.tddl.jdbc.group.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetIntHandler implements ParameterHandler {
	public void setParameter(PreparedStatement stmt, Object[] args)
			throws SQLException {
		stmt.setInt((Integer) args[0], (Integer) args[1]);
	}
}
