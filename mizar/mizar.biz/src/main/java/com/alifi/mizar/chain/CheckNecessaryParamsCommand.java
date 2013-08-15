package com.alifi.mizar.chain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import com.alifi.galaxy.biz.exception.GatewayException;
import com.alifi.mizar.common.util.GatewayErrors;
import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.util.GatewayContext;
import com.alifi.mizar.manager.ServiceManager;

/**
 * 检查必要的参数是否为空或不存在
 * @author tongpeng.chentp
 *
 */
@SuppressWarnings("unchecked")
public class CheckNecessaryParamsCommand implements Command {
    
    public CheckNecessaryParamsCommand() {};
    
    public CheckNecessaryParamsCommand(ServiceManager serviceManager, String... params) {
        this.serviceManager = serviceManager;
        this.necessaryParams = Arrays.asList(params);
    }
    
    private static final Log logger = LogFactory.getLog(CheckNecessaryParamsCommand.class); 
    
    private List<String> necessaryParams;
    
    private ServiceManager serviceManager;

    public boolean execute(Context context) throws Exception {
        GatewayContext gatewayContext = (GatewayContext) context;
        List<String> necessaryParamsCopy = new ArrayList<String>(necessaryParams);
        
        //获取service对应的配置，根据配置不同，必须的参数也会有不同
        if (!gatewayContext.containNotBlankInputParam("service")) {
            throw new GatewayException(GatewayErrors.ILLEGAL_ARGUMENT);
        }
        String service = gatewayContext.getInputParam("service");
        GatewayServiceInfo gatewayServiceInfo;
        try {
            gatewayServiceInfo = serviceManager.queryClassName(service);
            if (gatewayServiceInfo == null) {
                logger.warn("config for service [" + service + "] is not exist");
                throw new GatewayException(GatewayErrors.SERVICE_IS_NOT_EXIST);
            }
        } catch (DataAccessException ex) {
            logger.error("got exception when query config for service [" + service + "]", ex);
            throw new GatewayException(GatewayErrors.SYSTEM_ERROR, ex);
        }
        if (gatewayServiceInfo.isValidateSignIn()) {
            necessaryParamsCopy.add("sign");
            necessaryParamsCopy.add("signType");
        }
        if (!gatewayServiceInfo.isPublic()) {
            necessaryParamsCopy.add("partner");
        }

        for (String necessaryParam : necessaryParamsCopy) {
            if (!gatewayContext.containNotBlankInputParam(necessaryParam)) {
                logger.warn("missed necessary parameter [" + necessaryParam + "]");
                throw new GatewayException(GatewayErrors.ILLEGAL_ARGUMENT);
            }
        }
        
        context.put("gateway_service_info", gatewayServiceInfo);
        return false;
    }

    public void setNecessaryParams(List<String> necessaryParams) {
        this.necessaryParams = necessaryParams;
    }

    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }
}
