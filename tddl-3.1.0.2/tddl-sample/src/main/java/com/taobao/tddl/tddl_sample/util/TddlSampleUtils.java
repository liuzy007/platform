//Copyright(c) Taobao.com
package com.taobao.tddl.tddl_sample.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a> 
 * @version 1.0
 * @since 1.6
 * @date 2011-5-27涓嬪崍01:28:24
 */
public class TddlSampleUtils {
	/**
	 * 寰楀埌鐜板湪鏃堕棿
	 * @return
	 */
    public static String getNowTime(String format) {
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		
		return format(now,format);
	}
    
    /**
     * 寰楀埌浠婂ぉ寰�悗涓�ぉ鐨勮繖涓椂闂�
     * @return
     */
    public static String getNextDay(String format){
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);
    	Date next=cal.getTime();
    	return format(next,format);
    }
    
    public static String format(Date date,String format){
    	/**
		 * hh 涓�2灏忔椂鍒�HH 涓�4灏忔椂鍒�
		 *
		 */
    	SimpleDateFormat sdf;
    	if(null!=format){
    	    sdf = new SimpleDateFormat(format);
    	}else{
    		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	}
    	return sdf.format(date);
    }
}
