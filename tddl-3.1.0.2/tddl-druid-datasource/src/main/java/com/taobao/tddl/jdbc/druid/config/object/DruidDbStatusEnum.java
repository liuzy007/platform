package com.taobao.tddl.jdbc.druid.config.object;

import com.alibaba.common.lang.StringUtil;
import com.taobao.tddl.jdbc.druid.common.DruidConstants;

/**
 * 数据库状态枚举类型
 * @author qihao
 *
 */
public enum DruidDbStatusEnum {

	R_STATUS(DruidConstants.DB_STATUS_R), W_STATUS(DruidConstants.DB_STATUS_W), RW_STATUS(DruidConstants.DB_STATUS_RW), NA_STATUS(
			DruidConstants.DB_STATUS_NA);

	private String status;

	DruidDbStatusEnum(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public static DruidDbStatusEnum getAtomDbStatusEnumByType(String type) {
		DruidDbStatusEnum statusEnum = null;
		if (StringUtil.isNotBlank(type)) {
			String typeStr = type.toUpperCase().trim();
			if (typeStr.length() > 1) {
				if (DruidDbStatusEnum.NA_STATUS.getStatus().equals(typeStr)) {
					statusEnum = DruidDbStatusEnum.NA_STATUS;
				} else if (!StringUtil.contains(typeStr, DruidDbStatusEnum.NA_STATUS.getStatus())
								&&StringUtil.contains(typeStr, DruidDbStatusEnum.R_STATUS.getStatus())
								&& StringUtil.contains(typeStr, DruidDbStatusEnum.W_STATUS.getStatus())) {
					statusEnum = DruidDbStatusEnum.RW_STATUS;
				}
			} else {
				if (DruidDbStatusEnum.R_STATUS.getStatus().equals(typeStr)) {
					statusEnum = DruidDbStatusEnum.R_STATUS;
				} else if (DruidDbStatusEnum.W_STATUS.getStatus().equals(typeStr)) {
					statusEnum = DruidDbStatusEnum.W_STATUS;
				}
			}
		}
		return statusEnum;
	}

	public boolean isNaStatus() {
		return this == DruidDbStatusEnum.NA_STATUS;
	}

	public boolean isRstatus() {
		return this == DruidDbStatusEnum.R_STATUS || this == DruidDbStatusEnum.RW_STATUS;
	}

	public boolean isWstatus() {
		return this == DruidDbStatusEnum.W_STATUS || this == DruidDbStatusEnum.RW_STATUS;
	}

	public boolean isRWstatus() {
		return this == DruidDbStatusEnum.RW_STATUS;
	}
}
