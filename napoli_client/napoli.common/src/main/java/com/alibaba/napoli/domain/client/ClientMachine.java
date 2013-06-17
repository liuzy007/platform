package com.alibaba.napoli.domain.client;

import java.io.Serializable;

/**
 * User: heyman
 * Date: 6/4/12
 * Time: 2:32 PM
 */
public class ClientMachine implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ACTIVEMQ="activemq";
    public static final String TYPE_HORNETQ="hornetQ";
    
    private String ip;
    private int port;
    private int jmxPort;
    private String machineType;
    private int weight;
    protected boolean sendable = true;
    protected boolean receiveable = true;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public boolean isSendable() {
        return sendable;
    }

    public void setSendable(boolean sendable) {
        this.sendable = sendable;
    }

    public boolean isReceiveable() {
        return receiveable;
    }

    public void setReceiveable(boolean receiveable) {
        this.receiveable = receiveable;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
    }

    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        if (this.ip != null) {
            sb.append(this.ip);
        }
        sb.append(':');
        sb.append(this.port);
        return sb.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientMachine machine = (ClientMachine) o;

        if (port != machine.port) return false;
        if (ip != null ? !ip.equals(machine.ip) : machine.ip != null) return false;

        return true;
    }

    /*public String getJmxAddress(){
        return "service:jmx:rmi:///jndi/rmi://" +ip + ":" + jmxPort + "/jmxrmi";
    }
    
    public String getAddress(){
        return ip+":"+port;
    }*/
}