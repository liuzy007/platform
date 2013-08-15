package com.alifi.mizar.common.vo;

import java.io.Serializable;
import java.util.Date;

public class GatewayServiceInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3323918939039527640L;

	/**
	 * 服务id
	 */
	private int id;
	
	/**
	 * 服务名称
	 */
	private String serviceName;
	/**
	 * 访问的URL
	 * 用scheme表示API调用协议 host+port+path表示访问地址
	 * userInfo表示登录认证信息
	 * 如：
	 * 本地调用(new class instance and call) class://com.alibaba.foo.Bar 
	 * 从SpringBeanFactory中获取bean执行调用 beanfactory://fooBar
	 * ws远程调用 webservices://192.168.0.1:81/foo/bar
	 * hessian远程调用 hessian://192.168.0.1:82/foo
	 * rmi远程调用 rmi://192.168.0.1/foo
	 */
	private String url;
	
	/**
	 * 调用接口名
	 * 用来表示调用的Object Inteface Name,
	 * 只有调用协议是RMI等需要调用接口的协议时才需要制定,
	 * 格式如下: com.alibaba.test.rmi.TestInteface	 * 
	 */
    private String invokeInterface;
    
    /**
     * 调用方法名描述符,用来表示调用的方法名, 与tb_api_registry.name不一定相同
     */
    private String invokeMethod;
    
    /**
     * 版本号使用无符号整数标记，从1开始，每次加1 
     */
    private String version;
    
    /**
     * 状态：O --  正常  N--未起用 S--暂停  I -- 过期
     */
    private String status;
    
    /**
     * 是否验证输入签名
     */
    private boolean isValidateSignIn;
    
    /**
     * 是否加签
     */
    private boolean isEndorseSignOut;
    
    /**
     * 创建时间
     */
    private Date gmtCreated;
    /**
     * 修改时间
     */
    private Date gmtModified;
    
    /**
     * 创建人
     */
    private String created;
    
    /**
     * 修改人
     */
    private String modified;
    
    /**
     * 是否公开，公开的接口不用验证用户权限
     */
    private boolean isPublic;
	
    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getInvokeInterface() {
		return invokeInterface;
	}

	public void setInvokeInterface(String invokeInterface) {
		this.invokeInterface = invokeInterface;
	}

	public String getInvokeMethod() {
		return invokeMethod;
	}

	public void setInvokeMethod(String invokeMethod) {
		this.invokeMethod = invokeMethod;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isValidateSignIn() {
		return isValidateSignIn;
	}
	
    public void setValidateSignIn(boolean isValidateSignIn) {
        this.isValidateSignIn = isValidateSignIn;
    }

    public boolean isEndorseSignOut() {
        return isEndorseSignOut;
    }

    public void setEndorseSignOut(boolean isEndorseSignOut) {
        this.isEndorseSignOut = isEndorseSignOut;
    }

	public Date getGmtCreated() {
		return gmtCreated;
	}

	public void setGmtCreated(Date gmtCreated) {
		this.gmtCreated = gmtCreated;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getModified() {
		return modified;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}
