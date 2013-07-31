package com.alibaba.napoli.mqImpl;

import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.receiver.RedeliveryStrategy;
import com.alibaba.napoli.receiver.filter.Filter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * consumer context use for Listener onMessage
 * 
 * @author sait.xuc
 */
public class ConsumerContext {
    private final Session         session;
    private final MessageConsumer messageConsumer;
    private final ConnectionParam connectionParam;

    private List<Filter>          filterList = Collections.synchronizedList(new ArrayList<Filter>());

    public ConsumerContext(final Session session, final MessageConsumer messageConsumer,
                           final ConnectionParam connectionParam, final List<Filter> filterList) {
        this.session = session;
        this.messageConsumer = messageConsumer;
        this.connectionParam = connectionParam;
        this.filterList = filterList;
    }

    public Session getSession() {
        return session;
    }

    public MessageConsumer getMessageConsumer() {
        return messageConsumer;
    }

    public String getQueueName() {
        return connectionParam.getName();
    }

    public int getAcknowledgeMode() {
        return connectionParam.getAcknowledgeMode();
    }

    public RedeliveryStrategy getRedeliveryStrategy() {
        return connectionParam.getRedeliveryStrategy();
    }

    public int getIldePeriod() {
        return connectionParam.getIdlePeriod();
    }

    public List<Filter> getFilterList() {
        return filterList;
    }

    public ConnectionParam getConnectionParam() {
        return connectionParam;
    }
}
