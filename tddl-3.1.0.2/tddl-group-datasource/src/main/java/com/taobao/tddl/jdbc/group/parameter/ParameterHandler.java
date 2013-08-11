package com.taobao.tddl.jdbc.group.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ParameterHandler {
	void setParameter(PreparedStatement stmt, Object[] args) throws SQLException;
}
