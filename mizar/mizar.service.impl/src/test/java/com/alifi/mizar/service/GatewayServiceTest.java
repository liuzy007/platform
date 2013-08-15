package com.alifi.mizar.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.impl.ChainBase;

import junit.framework.TestCase;

import com.alifi.mizar.chain.CheckNecessaryParamsCommand;
import com.alifi.mizar.chain.InvokeServiceCommand;
import com.alifi.mizar.chain.ValidateInputParamsCommand;
import com.alifi.mizar.common.util.CommonResult;
import com.alifi.mizar.common.util.GatewayErrors;
import com.alifi.mizar.manager.ServiceInputParamManager;
import com.alifi.mizar.manager.ServiceManager;
import com.alifi.mizar.service.impl.GatewayServiceImpl;
import com.alifi.mizar.test.helper.GatewayManagerHelper;

@SuppressWarnings("unchecked")
public class GatewayServiceTest extends TestCase {

    private GatewayServiceImpl gatewayService;
    
    private ServiceManager serviceManager;
    
    private ServiceInputParamManager serviceInputParamManager;
    
    private Chain gatewayChain;

	@Override
    protected void setUp() throws Exception {
        super.setUp();
        
        serviceManager = GatewayManagerHelper.createServicemanager();
        serviceInputParamManager = GatewayManagerHelper.createServiceInputParamManager();
        createGatewayChain();
        createGatewayService();
    }

	/**
	 * 测试service==null
	 */
	public void testInvokeService_1() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("service", new String[] { null });
		paramsMap.put("taobaoId", new String[] { "65755239" });
		CommonResult result = gatewayService.invokeService(paramsMap);
		assertFalse(result.isSuccess());
		assertEquals(GatewayErrors.ILLEGAL_ARGUMENT, result.getErrorCode());
	}

	/**
	 * 测试service未在mizar中注册
	 */
	public void testInvokeService_2() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("service", new String[] { "error_service_name" });
		CommonResult result = gatewayService.invokeService(paramsMap);
		assertFalse(result.isSuccess());
		assertEquals(GatewayErrors.SERVICE_IS_NOT_EXIST, result.getErrorCode());
	}

	/**
	 * 测试service参数不正确
	 */
	public void testInvokeService_3() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("service", new String[] { "exist service" });
		CommonResult result = gatewayService.invokeService(paramsMap);
		assertFalse(result.isSuccess());
		assertEquals(GatewayErrors.INVALID_PARAMS, result.getErrorCode());
	}
	
    private void createGatewayChain() {
        gatewayChain = new ChainBase();

        CheckNecessaryParamsCommand checkNecessaryParamsCommand = new CheckNecessaryParamsCommand(serviceManager, "service");
        ValidateInputParamsCommand validateInputParamsCommand = new ValidateInputParamsCommand(serviceInputParamManager);
        InvokeServiceCommand invokeServiceCommand = new InvokeServiceCommand();
        
        gatewayChain.addCommand(checkNecessaryParamsCommand);
        gatewayChain.addCommand(validateInputParamsCommand);
        gatewayChain.addCommand(invokeServiceCommand);
    }
    
    private void createGatewayService() {
        gatewayService = new GatewayServiceImpl();
        gatewayService.setGatewayChain(gatewayChain);
    }
}
