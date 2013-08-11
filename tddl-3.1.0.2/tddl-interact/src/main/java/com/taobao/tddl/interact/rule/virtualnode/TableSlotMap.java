//Copyright(c) Taobao.com
package com.taobao.tddl.interact.rule.virtualnode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.taobao.tddl.interact.monitor.TotalStatMonitor;
import com.taobao.tddl.interact.rule.util.VirturalNodeUtil;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a> 
 * @version 1.0
 * @since 1.6
 * @date 2011-6-2下午03:13:08
 */
public class TableSlotMap extends WrappedLogic implements VirtualNodeMap{
    private String logicTable;
	
    protected ConcurrentHashMap<String/*slot number*/,String/*table suffix*/> tableContext=new ConcurrentHashMap<String, String>(); 
    private Map<String/*table suffix*/,String/*slot string*/> tableSlotMap=new HashMap<String,String>();
	
    private volatile boolean isInit=false;
    public synchronized void init(){
    	if(isInit){
    		return;
    	}
    	
    	isInit=true;
    	
    	if(null!=tableSlotMap&&tableSlotMap.size()>0){
    		tableContext=(ConcurrentHashMap<String, String>) VirturalNodeUtil.extraReverseMap(tableSlotMap);
    	}else{
    		throw new IllegalArgumentException("no tableSlotMap config at all");
    	}
    }
    
    public String getValue(String key){
    	String suffix=tableContext.get(key);
    	TotalStatMonitor.virtualSlotIncrement(buildLogKey(key));
    	if(super.tableSlotKeyFormat!=null){
    		return super.wrapValue(suffix);
    	}else if(logicTable!=null){
    		StringBuilder sb=new StringBuilder();
    		sb.append(logicTable);
    		sb.append(tableSplitor);
    		sb.append(suffix);
    		return sb.toString();
    	}else{
    		throw new RuntimeException("TableRule no tableSlotKeyFormat property and logicTable is null");
    	}
    }
    
    public String buildLogKey(String key){
    	if(logicTable!=null){
    	    StringBuilder sb=new StringBuilder(logicTable);
    	    sb.append("_slot_");
    	    sb.append(key);
    	    return sb.toString();
    	}else{
    		throw new RuntimeException("TableRule no logicTable at all,can not happen!!");
    	}
    }
    
	public void setTableSlotMap(Map<String, String> tableSlotMap) {
		this.tableSlotMap = tableSlotMap;
	}

	public void setLogicTable(String logicTable) {
		this.logicTable = logicTable;
	}

}
