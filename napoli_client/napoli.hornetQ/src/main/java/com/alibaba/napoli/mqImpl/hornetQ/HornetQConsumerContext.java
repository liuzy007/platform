package com.alibaba.napoli.mqImpl.hornetQ;

import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.receiver.RedeliveryStrategy;
import com.alibaba.napoli.receiver.filter.Filter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientSession;

/**
 * User: heyman
 * Date: 4/6/12
 * Time: 11:02 上午
 */
public class HornetQConsumerContext {
    private static final Log logger = LogFactory.getLog(HornetQConsumerContext.class);
    
    private final ClientSession session;
    private final ClientConsumer messageConsumer;
    private final ConnectionParam connectionParam;

    private List<Filter> filterList = Collections.synchronizedList(new ArrayList<Filter>());

    public HornetQConsumerContext(final ClientSession session, final ClientConsumer messageConsumer,
                           final ConnectionParam connectionParam, final List<Filter> filterList) {
        this.session = session;
        this.messageConsumer = messageConsumer;
        this.connectionParam = connectionParam;
        this.filterList = filterList;
    }

    public ClientSession getSession() {
        return session;
    }

    public ClientConsumer getMessageConsumer() {
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
    
    public void close(){
        if (messageConsumer != null){
            try {
                messageConsumer.close();
            } catch (HornetQException e) {
                logger.error(e.getMessage(),e);
            }
        }
        
        if (session != null){
            try {
                session.close();
            } catch (HornetQException e) {
                logger.error(e.getMessage(),e);
            }
        }
    }
}
