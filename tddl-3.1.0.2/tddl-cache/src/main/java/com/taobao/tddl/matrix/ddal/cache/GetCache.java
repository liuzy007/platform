package com.taobao.tddl.matrix.ddal.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 获取cache
 * @author hu.weih
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GetCache {
	/**
	 * @return cache地址
	 */
	int cacheArea() default 0;
	
	/**
	 * 单位 秒
	 * 失效时间 0 或者 -1 永远不失效 
	 * <br> 默认1天
	 * @return
	 */
	int expire() default 86400;
	
	/**
	 * 是否允许存null到cache中。
	 * <br>用在 key value的数据结构上很适合
	 * @return
	 */
	boolean canCacheNull() default false;
}
