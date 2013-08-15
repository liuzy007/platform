package com.taobao.tddl.matrix.ddal.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author yusen(lishuai)
 * @since 1.0, 2009-10-13 下午05:11:39
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CacheAreaAndFiled {
	/**
	 * 值的形式如："cacheArea1:fieldName1;cacheArea2:fieldName2;cacheArea3:fieldName3" "缓存区域：缓存的key在对象中的属性名称"
	 */
	String cacheAreaAndField();
}


