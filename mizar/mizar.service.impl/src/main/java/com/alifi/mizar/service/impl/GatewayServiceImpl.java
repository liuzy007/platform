package com.alifi.mizar.service.impl;

import java.util.Map;

import org.apache.commons.chain.Chain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alifi.galaxy.biz.exception.GatewayException;
import com.alifi.galaxy.common.result.ResultSupport;
import com.alifi.mizar.common.util.CommonResult;
import com.alifi.mizar.common.util.GatewayErrors;
import com.alifi.mizar.service.GatewayService;
import com.alifi.mizar.util.GatewayContext;
import com.alifi.mizar.util.GatewayUtil;

/**
 * 
 * @author mark.lijj
 * 
 */
@SuppressWarnings("unchecked")
public class GatewayServiceImpl implements GatewayService {
    
    private static final Log logger = LogFactory.getLog(GatewayServiceImpl.class);

    private Chain gatewayChain;
    
    public void setGatewayChain(Chain gatewayChain) {
        this.gatewayChain = gatewayChain;
    }

	public CommonResult<?> invokeService(Map<String, String[]> paramsMap) {
		return invokeDubboServiceByClass(GatewayUtil.convertMap(paramsMap));
	}

	/**
	 * 根据服务名称调用相应服务
	 * 
	 * @param paramsMap
	 * @return
	 */
    public CommonResult<?> invokeDubboServiceByClass(Map<String, String> paramsMap) {
        logger.info("input params : " + paramsMap.toString());
		CommonResult<Object> commonResult = new CommonResult<Object>(false);
		GatewayContext context = new GatewayContext();
		context.setInputParams(paramsMap);
		try {
            gatewayChain.execute(context);
            commonResult.setSuccess(context.isSuccess());
            commonResult.setObject(context.getObject());
            commonResult.setErrorCode(context.getErrorCode());
            commonResult.setErrorMessage(context.getErrorMessage());
            if (context.containsKey("sign")) {
                commonResult.setSign((String) context.get("sign"));
            }
            if (context.containsKey("signType")) {
                commonResult.setSignType((String) context.get("signType"));
            }
            logger.info("call gateway successfully.");
        } catch (GatewayException e) {
            logger.warn("got error when call gateway : " + e.getErrorCode(), e);
            commonResult.setErrorCode(e.getErrorCode());
        } catch (Exception e) {
            logger.error("got exception when invoke service", e);
            commonResult.setErrorCode(GatewayErrors.SYSTEM_ERROR);
        }
		return commonResult;
	}
}
