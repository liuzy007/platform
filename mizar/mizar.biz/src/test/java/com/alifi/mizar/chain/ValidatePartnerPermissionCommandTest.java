package com.alifi.mizar.chain;

import com.alifi.galaxy.biz.exception.GatewayException;
import com.alifi.mizar.common.util.GatewayErrors;
import com.alifi.mizar.test.helper.GatewayManagerHelper;
import com.alifi.mizar.test.helper.GatewayServiceInfoHelper;

public class ValidatePartnerPermissionCommandTest extends CommandTest<ValidatePartnerPermissionCommand> {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        command = new ValidatePartnerPermissionCommand(GatewayManagerHelper.createPartnerPermissionManager());
    }
    
    public void testWrongPartnerType() throws Exception {
        clear();
        context.put("gateway_service_info", GatewayServiceInfoHelper.createPrivateInfo());
        inputParams.put("partner", "string");
        try {
            assertTrue(command.execute(context));
        } catch (GatewayException e) {
            assertEquals(GatewayErrors.WRONG_PARTNER_TYPE, e.getErrorCode());
        }
    }

    public void testPublicService() throws Exception {
        clear();
        context.put("gateway_service_info", GatewayServiceInfoHelper.create());
        assertFalse(command.execute(context));
    }
    
    public void testHasNoPrivilege() throws Exception {
        clear();
        context.put("gateway_service_info", GatewayServiceInfoHelper.createPrivateInfo());
        inputParams.put("partner", "0");
        try {
            assertTrue(command.execute(context));
        } catch (GatewayException e) {
            assertEquals(GatewayErrors.HAS_NO_PRIVILEGE, e.getErrorCode());
        }
    }
    
    public void testValidateSuccessfully() throws Exception {
        clear();
        context.put("gateway_service_info", GatewayServiceInfoHelper.createPrivateInfo());
        inputParams.put("partner", "1");
        assertFalse(command.execute(context));
    }
}
