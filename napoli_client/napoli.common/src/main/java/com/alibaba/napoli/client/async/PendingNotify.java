package com.alibaba.napoli.client.async;


/**
 * notify sender when a transactional message don't get commit or rollback message.<br>
 * @author zgl
 *
 */
public interface PendingNotify {
	enum PendingNotifyStateEnum{
		COMMIT,ROLLBACK
	}
	/**
	 * 当一个pending消息到达时，通知客户端做适当处理，如果客户端认为这个消息已经处理成功，<BR>
	 * 请返回COMMIT，如果需要消息回滚，请返回ROLLBACK。<BR>
	 * @param message pending message
	 * @return PendingNotifyStateEnum.COMMIT OR PendingNotifyStateEnum.ROLLBACK
	 */
	public PendingNotifyStateEnum notify(NapoliMessage message);
}
