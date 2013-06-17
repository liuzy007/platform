package com.alibaba.napoli.common.util;

public interface TransactionConstants {
	public static final String NAPOLI_MSG_PRO_KEY_TX_ID = "NAPOLI_MSG_PRO_KEY_TX_ID";
	public static final String NAPOLI_MSG_PRO_KEY_TX_STATE = "NAPOLI_MSG_PRO_KEY_TX_STATE";
//	public static final String NAPOLI_MSG_PRO_KEY_TX_EXPIRATION = "NAPOLI_MSG_PRO_KEY_TX_EXPIRATION";
	public static final String NAPOLI_MSG_PRO_VAL_TX_STATE_COMMIT = "Commit";
	public static final String NAPOLI_MSG_PRO_VAL_TX_STATE_ROLLBACK = "Rollback";
//	public static final String NAPOLI_MSG_PRO_VAL_TX_STATE_CONSUMABLE = "Consumable";
	public static final String NAPOLI_MSG_PRO_VAL_TX_STATE_HALF = "Half";
	public static final String NAPOLI_MSG_PRO_VAL_TX_STATE_PENDING = "Pending";
	

//	public static final String NAPOLI_MSG_PRO_KEY_TX_FLAG = "NAPOLI_MSG_PRO_KEY_TX_FLAG";
//	public static final String NAPOLI_MSG_PRO_VAL_TX_FLAG_PENDING = "Pending";
//	public static final String NAPOLI_MSG_PRO_VAL_TX_FLAG_HALF = "Half";
	
	
	public static final String SELECTOR_KEY_ROLLBACK = TransactionSupport.genRollbackSelector();
	public static final String SELECTOR_KEY_PENDING = TransactionSupport.genPendingSelector();
	
	public static final long DEFAULT_TRANSACTION_EXPIRATION = 5000l;
	
}
