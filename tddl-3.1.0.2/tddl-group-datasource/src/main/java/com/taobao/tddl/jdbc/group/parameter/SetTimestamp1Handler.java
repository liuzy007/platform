package com.taobao.tddl.jdbc.group.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SetTimestamp1Handler implements ParameterHandler {
	public void setParameter(PreparedStatement stmt, Object[] args)
			throws SQLException {
		stmt.setTimestamp((Integer) args[0], (Timestamp) args[1]);
	}
}
