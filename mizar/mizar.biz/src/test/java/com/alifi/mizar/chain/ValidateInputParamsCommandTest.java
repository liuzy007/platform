package com.alifi.mizar.chain;

import com.alifi.galaxy.biz.exception.GatewayException;
import com.alifi.mizar.common.util.GatewayErrors;
import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.test.helper.GatewayManagerHelper;

public class ValidateInputParamsCommandTest extends CommandTest<ValidateInputParamsCommand> {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        command = new ValidateInputParamsCommand(GatewayManagerHelper.createServiceInputParamManager());
        
    }
    
    public void testInvalidParams() throws Exception {
        try {
            clear();
            GatewayServiceInfo serviceInfo = new GatewayServiceInfo();
            serviceInfo.setId(1);
            context.put("gateway_service_info", serviceInfo);
            assertTrue(command.execute(context));
        } catch (GatewayException e) {
            assertEquals(GatewayErrors.INVALID_PARAMS, e.getErrorCode());
        }
    }
    
    public void testParamConvertError() throws Exception {
        try {
            clear();
            GatewayServiceInfo serviceInfo = new GatewayServiceInfo();
            serviceInfo.setId(1);
            context.put("gateway_service_info", serviceInfo);
            inputParams.put("taobaoId", "string");
            assertTrue(command.execute(context));
        } catch (GatewayException e) {
            assertEquals(GatewayErrors.PARAM_TYPE_CONVERT_ERROR, e.getErrorCode());
        }
    }
    
    public void testValidParamsSuccess() throws Exception {
        clear();
        GatewayServiceInfo serviceInfo = new GatewayServiceInfo();
        serviceInfo.setId(1);
        context.put("gateway_service_info", serviceInfo);
        inputParams.put("taobaoId", "1");
        assertFalse(command.execute(context));
    }

}
