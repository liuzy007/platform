package com.alibaba.napoli.domain.client;

import java.util.ArrayList;
import java.util.List;

/**
 * User: heyman
 * Date: 6/4/12
 * Time: 2:45 PM
 */
public class ClientVirtualTopic extends ClientDestination{
    private static final long serialVersionUID = -2989318308737491163L;
    
    private List<ClientQueue> clientQueueList = new ArrayList<ClientQueue>();

    public List<ClientQueue> getClientQueueList() {
        return clientQueueList;
    }

    public void setClientQueueList(List<ClientQueue> clientQueueList) {
        this.clientQueueList = clientQueueList;
    }
}