package com.alibaba.napoli.mqImpl.metaq;

import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.receiver.filter.Filter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: heyman Date: 4/25/12 Time: 10:02 上午
 */
public class MetaqConsumerContext {
    private final ConnectionParam connectionParam;

    private List<Filter>          filterList = Collections.synchronizedList(new ArrayList<Filter>());

    private boolean               boardcast;
    private boolean               order;

    public MetaqConsumerContext(final ConnectionParam connectionParam, final List<Filter> filterList) {
        this.connectionParam = connectionParam;
        this.filterList = filterList;
    }

    public List<Filter> getFilterList() {
        return filterList;
    }

    public ConnectionParam getConnectionParam() {
        return connectionParam;
    }

    public boolean isBoardcast() {
        return boardcast;
    }

    public void setBoardcast(boolean boardcast) {
        this.boardcast = boardcast;
    }

    public boolean isOrder() {
        return order;
    }

    public void setOrder(boolean order) {
        this.order = order;
    }
}
