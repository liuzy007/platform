package com.alibaba.napoli.domain.client;

import java.util.HashSet;
import java.util.Set;

/**
 * User: heyman
 * Date: 6/4/12
 * Time: 2:28 PM
 */
public class ClientQueue extends ClientDestination {
    public static final long serialVersionUID = -2989318308737491163L;
    public static final String STRATEGY_RANDOM = "random";
    public static final String STRATEGY_ROUND_ROBIN = "round_robin";
    public static final String BALANCE_ROBIN = "balance";
    public static final String STRATEGY_WEIGHT = "weight";

    private String sendStrategy;
    private int maxRedeliveries;
    private String routerTargetUrl; //convert to schema
    private String routerTargetName;
    private int routerMaxThreads = 1;
    private Set<ClientMachine> machineSet = new HashSet<ClientMachine>();

    public String getSendStrategy() {
        return sendStrategy;
    }

    public void setSendStrategy(String sendStrategy) {
        this.sendStrategy = sendStrategy;
    }

    public Set<ClientMachine> getMachineSet() {
        return machineSet;
    }

    public void setMachineSet(Set<ClientMachine> machineSet) {
        this.machineSet = machineSet;
    }

    public String getRouterTargetUrl() {
        return routerTargetUrl;
    }

    public void setRouterTargetUrl(String routerTargetUrl) {
        this.routerTargetUrl = routerTargetUrl;
    }

    public String getRouterTargetName() {
        return routerTargetName;
    }

    public void setRouterTargetName(String routerTargetName) {
        this.routerTargetName = routerTargetName;
    }

    public int getRouterMaxThreads() {
        return routerMaxThreads;
    }

    public void setRouterMaxThreads(int routerMaxThreads) {
        this.routerMaxThreads = routerMaxThreads;
    }

    public int getMaxRedeliveries() {
        return maxRedeliveries;
    }

    public void setMaxRedeliveries(int maxRedeliveries) {
        this.maxRedeliveries = maxRedeliveries;
    }
}