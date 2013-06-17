package com.alibaba.napoli.common.constants;

/**
 * User: heyman
 * Date: 3/23/12
 * Time: 5:02 下午
 */
public enum MachineType {
    HornetQ("hornetQ"), Activemq("activemq");

    private final String type;

    private MachineType(String type) {
        this.type = type;
    }
    
    public String getType(){
        return type;
    }
}
