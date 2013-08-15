package com.alifi.mizar.chain;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.chain.Command;

import com.alifi.mizar.util.GatewayContext;

import junit.framework.TestCase;


public abstract class CommandTest<T extends Command> extends TestCase {
    
    protected T command;
    
    protected GatewayContext context;
    
    protected Map<String, String> inputParams;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        context = new GatewayContext();
        inputParams = new HashMap<String, String>();
        context.setInputParams(inputParams);
    }
    
    protected void clear() {
        inputParams.clear();
        context.clear();
    }

}
