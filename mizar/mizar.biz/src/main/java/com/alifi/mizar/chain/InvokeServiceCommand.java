package com.alifi.mizar.chain;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alifi.galaxy.biz.exception.GatewayException;
import com.alifi.mizar.common.util.GatewayErrors;
import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.util.DubboUtil;
import com.alifi.mizar.util.GatewayContext;

import java.util.Map;

/**
 * 通过dubbo调用服务
 * @author tongpeng.chentp
 *
 */
public class InvokeServiceCommand implements Command {

    private static final Log logger = LogFactory.getLog(InvokeServiceCommand.class);

    public boolean execute(Context context) throws Exception {
    	GatewayContext gatewayContext = (GatewayContext) context;
        GatewayServiceInfo gatewayServiceInfo = (GatewayServiceInfo) context.get("gateway_service_info");
        String[] paramTypes = (String[]) context.get("gateway_param_types");
        Object[] paramValues = (Object[]) context.get("gateway_param_values");

        try {
        	GenericService genericService = DubboUtil.getService(gatewayServiceInfo);
            Object invokedResult = genericService.$invoke(gatewayServiceInfo.getInvokeMethod(), paramTypes, paramValues);
            if (invokedResult instanceof Map) {
                Map<String, Object> mapResult = (Map<String, Object>) invokedResult;
                Object object = getModel(mapResult);
                filter(object);
                gatewayContext.setSuccess((Boolean)mapResult.get("success"));
                gatewayContext.setObject(object);
                gatewayContext.setErrorCode((String) mapResult.get("code"));
                gatewayContext.setErrorMessage((String) mapResult.get("message"));
                return false;
            }
            gatewayContext.setSuccess(Boolean.TRUE);
            gatewayContext.setObject(invokedResult.toString());

        } catch (RpcException e) {
            logger.error("service not exist in dubbo", e);
            throw new GatewayException(GatewayErrors.SERVICE_UNABLE, e);
        } catch (GenericException e) {
        	throw new GatewayException(GatewayErrors.SYSTEM_ERROR, e);
        } catch (Throwable e) {
        	if (e instanceof GatewayException) {
        		throw (GatewayException) e;
        	}
        	if (e.getCause() != null && e.getCause() instanceof GatewayException) {
        		throw (GatewayException) e.getCause();
        	}
            throw new GatewayException(GatewayErrors.SYSTEM_ERROR, e);
        }
        return false;
    }

    private void filter(Object object) {
        if ((object instanceof Map) && ((Map)object).containsKey("class")) {
            ((Map)object).remove("class");
        }
    }

    //兼容ResultSupport的不同版本
    private Object getModel(Map<String, Object> params) {
        if (params.containsKey("dataObject")) {
            return params.get("dataObject");
        }
        if (params.containsKey("defaultModel")) {
            return params.get("defaultModel");
        }
        return null;
    }
}