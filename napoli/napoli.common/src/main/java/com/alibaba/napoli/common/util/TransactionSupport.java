package com.alibaba.napoli.common.util;

import com.alibaba.napoli.client.async.NapoliMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

public class TransactionSupport implements TransactionConstants {
    public static Message buildHalfMessage(Message message, String txId, long expiration) throws JMSException {
        message.setStringProperty(NAPOLI_MSG_PRO_KEY_TX_ID, txId);
        message.setStringProperty(NAPOLI_MSG_PRO_KEY_TX_STATE, NAPOLI_MSG_PRO_VAL_TX_STATE_HALF);
        return message;
    }

    public static NapoliMessage buildHalfMessage(NapoliMessage message, String txId, long expiration) {
        message.setProperty(NAPOLI_MSG_PRO_KEY_TX_ID, txId);
        message.setProperty(NAPOLI_MSG_PRO_KEY_TX_STATE, NAPOLI_MSG_PRO_VAL_TX_STATE_HALF);
        return message;
    }

    public static Message buildCommitMessage(Message message, String txId) throws JMSException {
        clearMessageBody(message);
        message.setStringProperty(NAPOLI_MSG_PRO_KEY_TX_ID, txId);
        message.setStringProperty(NAPOLI_MSG_PRO_KEY_TX_STATE, NAPOLI_MSG_PRO_VAL_TX_STATE_COMMIT);
        return message;
    }

    public static Message buildRollbackMessage(Message message, String txId) throws JMSException {
        clearMessageBody(message);
        message.setStringProperty(NAPOLI_MSG_PRO_KEY_TX_ID, txId);
        message.setStringProperty(NAPOLI_MSG_PRO_KEY_TX_STATE, NAPOLI_MSG_PRO_VAL_TX_STATE_ROLLBACK);
        return message;
    }

    private static void clearMessageBody(Message message) throws JMSException {
        if (message instanceof TextMessage) {
            ((TextMessage) message).setText("empty");
        } else if (message instanceof ObjectMessage) {
            ((ObjectMessage) message).setObject(0);
        } else {
            message.clearBody();
        }
    }

    public static void removeTransactionProperties(NapoliMessage nm) {
        nm.removeProperty(NAPOLI_MSG_PRO_KEY_TX_ID);
        nm.removeProperty(NAPOLI_MSG_PRO_KEY_TX_STATE);
    }

//	public static final String genNormalSelector() {
//		return compositeSelector(NAPOLI_MSG_PRO_KEY_TX_STATE, NAPOLI_MSG_PRO_VAL_TX_STATE_CONSUMABLE);
//	}

    public static String genPendingSelector() {
        return compositeSelector(NAPOLI_MSG_PRO_KEY_TX_STATE, NAPOLI_MSG_PRO_VAL_TX_STATE_HALF);
    }

    public static String genRollbackSelector() {
        return compositeSelector(NAPOLI_MSG_PRO_KEY_TX_STATE, NAPOLI_MSG_PRO_VAL_TX_STATE_HALF) + " OR " + compositeSelector(NAPOLI_MSG_PRO_KEY_TX_STATE, NAPOLI_MSG_PRO_VAL_TX_STATE_ROLLBACK);
    }

    private static String compositeSelector(String key, String value) {
        return key + "=" + "'" + value + "'";
    }
}
