package com.alibaba.napoli.client.async;

import java.io.Serializable;

import com.alibaba.napoli.client.NapoliClientException;

public interface AsyncSenderEx extends AsyncSender{
	/**
	 * 发送消息根据指定的消息优先级，注意：JMS prioiry非严格保证，只在消费堵塞的情况下有用。<br>
	 * @since 1.3.1
	 * @param message 消息内容
	 * @param priority  消息优先级，在0-9之间，缺省为5，小于4为低优先级，大于4为高优先级，非严格保证。
	 * @return success or fail
	 */
	boolean send(Serializable message,int priority);
	
	/**
	 * 发送负责消息，可以附带用户自定义属性，例如，唯一id
	 * @since 1.3.1
	 * @param message 带属性的消息
	 * @return 发送结果，如果发送失败，可能还包含异常
	 */
	SendResult send(NapoliMessage message);
	
	/**
	 * 提供发送消息和业务调用的最终一致性保证,必须结合PendingNofitier使用。<br>
	 * 在这种情况下，消息存在4种状态，half,pending,commit和dead,在bizCallback执行前,<br>
	 * 消息处于half状态，如果half消息发送失败，方法抛出异常以便业务事务回滚<br>
	 * 如果业务执行成功并成功发送commit消息，消息处于commit状态并可以供消费端消费<br>
	 * 如果业务执行失败并发送rollback消息，消息处于dead状态并被删除。<br>
	 * 无论bizCallback成功或者失败，如果后续的commit或者rollback消息丢失（客户端挂/网络异常等等）,<br>
	 * 消息都会处于pending状态，并定时调用客户端的PendingNofitier接口通知客户端，客户端根据业务是否处理成功<br>
	 * 来决定commit还是rollback。
	 * @since 1.4.0
	 * @param message 消息体，可以附加额外属性
	 * @param bizCallback 业务回调接口
	 * @throws NapoliClientException when send message failed or bizCallBack failed
	 */
	void send(NapoliMessage message, Runnable bizCallback) throws NapoliClientException,BizInvokationException;
	
	/**
	 * 提供发送消息和业务调用的最终一致性保证,必须结合PendingNofitier使用。<br>
	 * 在这种情况下，消息存在4种状态，half,pending,commit和dead,在bizCallback执行前,<br>
	 * 消息处于half状态，如果half消息发送失败，方法抛出异常以便业务事务回滚<br>
	 * 如果业务执行成功并成功发送commit消息，消息处于commit状态并可以供消费端消费<br>
	 * 如果业务执行失败并发送rollback消息，消息处于dead状态并被删除，这个方法会抛出异常通知业务事务回滚。<br>
	 * 无论bizCallback成功或者失败，如果后续的commit或者rollback消息丢失（客户端挂/网络异常等等）,<br>
	 * 消息都会处于pending状态，并定时调用客户端的PendingNofitier接口通知客户端，客户端根据业务是否处理成功<br>
	 * 来决定commit还是rollback。 
	 * @since 1.4.0
	 * @param message 消息体
	 * @param bizCallback 业务回调接口
	 * @throws NapoliClientException when send message failed or bizCallBack failed
	 */
	void send(Serializable message, Runnable bizCallback) throws NapoliClientException,BizInvokationException;
	
	/**
	 * 设置一个PendingNotify接口，在有pending消息时这个接口被调用并保证最终一致性。<br>
	 * @since 1.4.0
	 * @param pendingNotifier 
	 * @throws NapoliClientException when send message failed or bizCallBack failed
	 */
	public void setPendingNotifier(PendingNotify pendingNotifier) ;
    
}
