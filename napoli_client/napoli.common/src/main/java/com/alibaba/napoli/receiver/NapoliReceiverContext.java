package com.alibaba.napoli.receiver;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.receiver.filter.Filter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: heyman Date: 2/20/12 Time: 4:03 下午
 */
public class NapoliReceiverContext {
    private NapoliWorker    worker;
    private NapoliMessage   napoliMessage;
    private boolean         storeOk;
    private ConnectionParam connectionParam;
    private List<Filter> filterList = Collections.synchronizedList(new ArrayList<Filter>());

    public NapoliReceiverContext(NapoliWorker worker, NapoliMessage napoliMessage, ConnectionParam connectionParam) {
        this.worker = worker;
        this.napoliMessage = napoliMessage;
        this.connectionParam = connectionParam;
    }

    public List<Filter> getFilterList() {
        return filterList;
    }

    public void setFilterList(List<Filter> filterList) {
        this.filterList = filterList;
    }

    public NapoliWorker getWorker() {
        return worker;
    }

    public NapoliMessage getNapoliMessage() {
        return napoliMessage;
    }

    public boolean isStoreOk() {
        return storeOk;
    }

    public void setStoreOk(boolean storeOk) {
        this.storeOk = storeOk;
    }

    public ConnectionParam getConnectionParam() {
        return connectionParam;
    }
}
