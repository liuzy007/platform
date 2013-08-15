/**
 * 
 */
package com.taobao.tddl.matrix.ddal.cache;

/**
 * @author hu.weih
 *
 */
public interface CacheMethodInterceptorMBean {
	
	String  get(int namespace, String key);

	String  delete(int namespace, String key);
}
