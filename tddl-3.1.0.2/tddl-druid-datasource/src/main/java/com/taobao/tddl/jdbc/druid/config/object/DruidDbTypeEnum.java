package com.taobao.tddl.jdbc.druid.config.object;

import com.taobao.tddl.jdbc.druid.common.DruidConstants;

/**
 * 数据库类型枚举类型
 * 
 * @author qihao
 *
 */
public enum DruidDbTypeEnum {

	ORACLE(DruidConstants.DEFAULT_ORACLE_DRIVER_CLASS, DruidConstants.DEFAULT_DRUID_ORACLE_SORTER_CLASS),

	MYSQL(DruidConstants.DEFAULT_MYSQL_DRIVER_CLASS, DruidConstants.DEFAULT_DRUID_MYSQL_SORTER_CLASS);

	private String driverClass;
	private String sorterClass;

	DruidDbTypeEnum(String driverClass, String sorterClass) {
		this.driverClass = driverClass;
		this.sorterClass = sorterClass;
	}

	public static DruidDbTypeEnum getAtomDbTypeEnumByType(String type) {
		/*
		if (StringUtil.isNotBlank(type)) {
			for (AtomDbTypeEnum typeEnum : AtomDbTypeEnum.values()) {
				if (typeEnum.getType().equals(type.toUpperCase().trim())) {
					return typeEnum;
				}
			}
		}
		return null;
		*/
		try {
			return DruidDbTypeEnum.valueOf(type.trim().toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}

	public String getDriverClass() {
		return driverClass;
	}

	public String getSorterClass() {
		return sorterClass;
	}

	/*public String getType() {
		return type;
	}*/
}
