package com.taobao.tddl.jdbc.group.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetDoubleHandler implements ParameterHandler {
	public void setParameter(PreparedStatement stmt, Object[] args)
			throws SQLException {
		stmt.setDouble((Integer) args[0], (Double) args[1]);
	}
}
