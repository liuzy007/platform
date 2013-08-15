package com.taobao.tddl.jdbc.druid.listener;

import com.taobao.tddl.jdbc.druid.config.object.DruidDbStatusEnum;

/**数据库状态变化监听器
 * 
 * @author qihao
 *
 */
public interface DruidDbStatusListener {

	void handleData(DruidDbStatusEnum oldStatus, DruidDbStatusEnum newStatus);
}
