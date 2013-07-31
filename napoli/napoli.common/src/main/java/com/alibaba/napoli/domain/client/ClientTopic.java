package com.alibaba.napoli.domain.client;

import java.util.HashSet;
import java.util.Set;

/**
 * User: heyman
 * Date: 6/4/12
 * Time: 2:43 PM
 */
public class ClientTopic extends ClientDestination{
    private static final long serialVersionUID            = -2989318308737491163L;
    
    private Set<ClientMachine> machineSet = new HashSet<ClientMachine>();
    private String zks;

    public Set<ClientMachine> getMachineSet() {
        return machineSet;
    }

    public void setMachineSet(Set<ClientMachine> machineSet) {
        this.machineSet = machineSet;
    }

    public String getZks() {
        return zks;
    }

    public void setZks(String zks) {
        this.zks = zks;
    }
}