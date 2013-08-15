package com.alifi.mizar.common.vo;

import java.io.Serializable;

/**
 * 合作伙伴
 * @author tongpeng.chentp
 *
 */
public class Partner implements Serializable {

    private static final long serialVersionUID = 4426646932707897970L;
    
    /**
     * 主键
     */
    private int id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 权限 A-admin  O-operator Q-query
     */
    private String role;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 合作伙伴类型,0-集团内部合作 1-外部用户
     */
    private int type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
