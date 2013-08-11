package com.taobao.tddl.matrix.ddal.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author hu.weih(KongWang) on 2009-9-12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InvalidMultiCache {
	
	/**
	 * @return cache地址 如果多个，以,隔开
	 */
	String cacheArea() ;
	
	/**
	 * 如果多个，以,隔开
	 * @return
	 */
	String idfield();	

}
