/**
 * 
 */
package com.taobao.tddl.matrix.ddal.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 失效
 * @author hu.weih
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InvalidCache{
	/**
	 * @return cache地址
	 */
	int cacheArea() ;
	
	/**
	 * 如果多个，以,隔开
	 * @return
	 */
	String idfield();	
	
}
