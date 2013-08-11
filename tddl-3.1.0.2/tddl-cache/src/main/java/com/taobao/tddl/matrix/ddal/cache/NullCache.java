/**
 * 
 */
package com.taobao.tddl.matrix.ddal.cache;

import java.io.Serializable;

/**
 * 空值
 * @author hu.weih
 *
 */
public class NullCache implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7112505730204257868L;
	
    private static NullCache m_instance=new NullCache();
    
    /**
     * 不允许直接new
     */
    private NullCache(){
    	
    }
    
    public static NullCache getInstance() {
        return m_instance;
    }

}
