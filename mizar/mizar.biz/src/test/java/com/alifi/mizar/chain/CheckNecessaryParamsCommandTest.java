package com.alifi.mizar.chain;

import com.alifi.galaxy.biz.exception.GatewayException;
import com.alifi.mizar.common.util.GatewayErrors;
import com.alifi.mizar.manager.ServiceManager;
import com.alifi.mizar.test.helper.GatewayManagerHelper;

public class CheckNecessaryParamsCommandTest extends CommandTest<CheckNecessaryParamsCommand> {

    private ServiceManager serviceManager;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        serviceManager = GatewayManagerHelper.createServicemanager();
        command = new CheckNecessaryParamsCommand(serviceManager, "service");
    }
    
    public void testAllNecessaryParamsExist() throws Exception {
        clear();
        inputParams.put("service", "exist service");
        assertFalse(command.execute(context));
    }
    
    public void testAllNecessaryParamsExistForPrivateService() throws Exception {
        clear();
        inputParams.put("service", "private service");
        inputParams.put("partner", "11111");
        assertFalse(command.execute(context));
    }
    
    public void testAllNecessaryParamsExistForSignedService() throws Exception {
        clear();
        inputParams.put("service", "signed service");
        inputParams.put("sign", "sign");
        inputParams.put("signType", "DSA");
        assertFalse(command.execute(context));
    }
    
    public void testNecessaryParamsNotExist() throws Exception {
        try {
            clear();
            assertTrue(command.execute(context));
        } catch (GatewayException e) {
            assertEquals(GatewayErrors.ILLEGAL_ARGUMENT, e.getErrorCode());
        }
    }
    
    public void testNecessaryParamIsNull() throws Exception {
        try {
            clear();
            inputParams.put("service", "");
            assertTrue(command.execute(context));
        } catch (GatewayException e) {
            assertEquals(GatewayErrors.ILLEGAL_ARGUMENT, e.getErrorCode());
        }
    }

}
