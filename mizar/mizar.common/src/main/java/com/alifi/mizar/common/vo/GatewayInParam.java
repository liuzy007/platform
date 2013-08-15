package com.alifi.mizar.common.vo;

import java.io.Serializable;
import java.util.Date;

public class GatewayInParam implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2515795606613548063L;
	/**
	 * id
	 */
	private int id;
	/**
	 * 参数顺序
	 */
	private int index;
	/**
	 * 服务id
	 */
	private int serviceId;
	/**
	 * 参数名称
	 */
	private String paramName;
	/**
	 * 参数类型
	 */
	private String paramType;
	/**
	 * 是否允许为空
	 */
	private boolean nullable;
	/**
	 * 创建者
	 */
	private String creater;
	/**
	 * 创建时间
	 */
	private Date gmtCreated;
	/**
	 * 修改者
	 */
	private String modifier;
	/**
	 * 修改时间
	 */
	private Date gmtModified;
	
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getServiceId() {
		return serviceId;
	}
	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}
	public String getParamName() {
		return paramName;
	}
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}
	public String getParamType() {
		return paramType;
	}
	public void setParamType(String paramType) {
		this.paramType = paramType;
	}
    public boolean isNullable() {
        return nullable;
    }
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
    public String getCreater() {
		return creater;
	}
	public void setCreater(String creater) {
		this.creater = creater;
	}
	public Date getGmtCreated() {
		return gmtCreated;
	}
	public void setGmtCreated(Date gmtCreated) {
		this.gmtCreated = gmtCreated;
	}
	public String getModifier() {
		return modifier;
	}
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
	public Date getGmtModified() {
		return gmtModified;
	}
	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}
	
	
}
