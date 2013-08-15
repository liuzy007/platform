/**
 * 
 */
package com.taobao.tddl.matrix.ddal.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 适合 于方法中参数有一批id的
 * 
 * 例如 1111,2222,3333 
 * 以逗号隔开
 * @author hu.weih
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InvalidCacheByIds {
	
	/**
	 * @return cache地址
	 */
	int cacheArea() ;
	

}
