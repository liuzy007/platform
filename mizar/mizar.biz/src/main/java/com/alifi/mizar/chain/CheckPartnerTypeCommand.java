package com.alifi.mizar.chain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import com.alifi.galaxy.biz.exception.GatewayException;
import com.alifi.mizar.common.util.GatewayErrors;
import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.common.vo.Partner;
import com.alifi.mizar.util.GatewayContext;
import com.alifi.mizar.manager.PartnerManager;

/**
 * 检查用户类型是否符合，集团合作和外部用户将访问不同的网关，以增加安全性
 * @author tongpeng.chentp
 *
 */
public class CheckPartnerTypeCommand implements Command {
    
    public CheckPartnerTypeCommand() {};
    
    private PartnerManager partnerManager;
    
    private List<Integer> allowedTypeList;

	public PartnerManager getPartnerManager() {
		return partnerManager;
	}

	public void setPartnerManager(PartnerManager partnerManager) {
		this.partnerManager = partnerManager;
	}

	public void setAllowedTypes(String allowedTypes) {
		allowedTypeList = new ArrayList<Integer>();
		String[] temps = allowedTypes.split(",");
		for (String temp : temps) {
			allowedTypeList.add(Integer.valueOf(temp.trim()));
		}
	}

	public boolean execute(Context context) throws Exception {
        GatewayContext gatewayContext = (GatewayContext) context;
        GatewayServiceInfo gatewayServiceInfo = (GatewayServiceInfo) gatewayContext.get("gateway_service_info");
        
        if (gatewayServiceInfo.isPublic()) {
        	return false;
        }
        
        String partnerId = gatewayContext.getInputParam("partner");
        Partner partner = partnerManager.getById(Integer.parseInt(partnerId));
        for (Integer allowedType : allowedTypeList) {
        	if (allowedType == partner.getType()) {
        		return false;
        	}
        }
        
        throw new GatewayException(GatewayErrors.ILLEGEL_ACCESS_SWITCH_SYSTEM);
    }


}
