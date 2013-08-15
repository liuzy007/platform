package com.alifi.mizar.chain;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import com.alibaba.common.lang.StringUtil;
import com.alifi.galaxy.biz.exception.GatewayException;
import com.alifi.mizar.common.util.GatewayErrors;
import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.manager.PartnerPermissionManager;
import com.alifi.mizar.util.GatewayContext;

/**
 * 验证客户是否有调用接口的权限
 * @author tongpeng.chentp
 *
 */
public class ValidatePartnerPermissionCommand implements Command {
    
    public ValidatePartnerPermissionCommand() {}
    
    public ValidatePartnerPermissionCommand(PartnerPermissionManager partnerPermissionManager) {
        this.partnerPermissionManager = partnerPermissionManager;
    }
    
    private static final Log logger = LogFactory.getLog(ValidatePartnerPermissionCommand.class);
    
    private PartnerPermissionManager partnerPermissionManager;

    public boolean execute(Context context) throws Exception {
        GatewayContext gatewayContext = (GatewayContext) context;
        GatewayServiceInfo serviceInfo = (GatewayServiceInfo) gatewayContext.get("gateway_service_info");
        if (serviceInfo.isPublic()) {
            return false;
        }

        String partner = gatewayContext.getInputParam("partner");
        if (!StringUtil.isNumeric(partner)) {
            throw new GatewayException(GatewayErrors.WRONG_PARTNER_TYPE);
        }
        
        try {
            if (!partnerPermissionManager.hasPermission(Integer.parseInt(partner), serviceInfo.getId())) {
                throw new GatewayException(GatewayErrors.HAS_NO_PRIVILEGE);
            }
        } catch (DataAccessException ex) {
            logger.error("got exception when query partner permission", ex);
            throw new GatewayException(GatewayErrors.SYSTEM_ERROR, ex);
        }
        
        return false;
    }

    
    public void setPartnerPermissionManager(PartnerPermissionManager partnerPermissionManager) {
        this.partnerPermissionManager = partnerPermissionManager;
    }
}
