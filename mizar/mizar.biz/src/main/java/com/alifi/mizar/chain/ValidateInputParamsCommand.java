package com.alifi.mizar.chain;

import java.util.List;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alifi.galaxy.biz.exception.GatewayException;
import com.alifi.mizar.common.util.GatewayErrors;
import com.alifi.mizar.common.vo.GatewayInParam;
import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.manager.ServiceInputParamManager;
import com.alifi.mizar.util.GatewayContext;
import com.alifi.mizar.util.GatewayUtil;

/**
 * 验证输入参数的类型是否符合，并进行类型转换
 * 现在只支持简单类型
 * @author tongpeng.chentp
 *
 */
@SuppressWarnings("unchecked")
public class ValidateInputParamsCommand implements Command {
    
    public ValidateInputParamsCommand() {}
    
    public ValidateInputParamsCommand(ServiceInputParamManager serviceInputParamManager) {
        this.serviceInputParamManager = serviceInputParamManager;
    }
    
    private static final Log logger = LogFactory.getLog(ValidateInputParamsCommand.class);
    
    private ServiceInputParamManager serviceInputParamManager;

    public boolean execute(Context context) throws Exception {
        GatewayContext gatewayContext = (GatewayContext) context;
        GatewayServiceInfo gatewayServiceInfo = (GatewayServiceInfo) gatewayContext.get("gateway_service_info");
        List<GatewayInParam> inputParams = serviceInputParamManager.listInputParamByServiceId(gatewayServiceInfo.getId());
        if (!GatewayUtil.validateInParams(inputParams, gatewayContext.getInputParams())) {
            logger.warn("some parameters couldn't be null");
            throw new GatewayException(GatewayErrors.INVALID_PARAMS);
        }
        try {
            int length = inputParams.size();
            String[] paramTypes = new String[length];
            Object[] paramValues = new Object[length];
            int index = 0;
            for (GatewayInParam inputParam : inputParams) {
                paramTypes[index] = inputParam.getParamType();
                paramValues[index] = GatewayUtil.convertParamType(
                        gatewayContext.getInputParam(inputParam.getParamName()), inputParam.getParamType());
                index++;
            }
            context.put("gateway_param_values", paramValues);
            context.put("gateway_param_types", paramTypes);
        } catch (Exception ex) {
            logger.warn("got exception when convert input paramters", ex);
            throw new GatewayException(GatewayErrors.PARAM_TYPE_CONVERT_ERROR, ex);
        }
        return false;
    }

    
    public void setServiceInputParamManager(ServiceInputParamManager serviceInputParamManager) {
        this.serviceInputParamManager = serviceInputParamManager;
    }

}
