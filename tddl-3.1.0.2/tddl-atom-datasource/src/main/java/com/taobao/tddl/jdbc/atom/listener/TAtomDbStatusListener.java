package com.taobao.tddl.jdbc.atom.listener;

import com.taobao.tddl.jdbc.atom.config.object.AtomDbStatusEnum;

/**数据库状态变化监听器
 * 
 * @author qihao
 *
 */
public interface TAtomDbStatusListener {

	void handleData(AtomDbStatusEnum oldStatus, AtomDbStatusEnum newStatus);
}
