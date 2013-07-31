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
 * @date 2011-5-27下午01:28:24
 */
public class TddlSampleUtils {
	/**
	 * 得到现在时间
	 * @return
	 */
    public static String getNowTime(String format) {
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		
		return format(now,format);
	}
    
    /**
     * 得到今天往后一天的这个时间
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
		 * hh 为12小时制 HH 为24小时制
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
