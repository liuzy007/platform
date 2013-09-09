package com.pktech.oal.cache;

import org.apache.commons.lang.StringUtils;

import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.manager.ServiceInputParamManager;
import com.alifi.mizar.manager.ServiceManager;

public class CacheManager {

    private ServiceManager serviceManager;
    private ServiceInputParamManager serviceInputParamManager;

    private static CacheManager instance = null;

    private CacheManager() {
        CacheManager.instance = this;
    }

    public static CacheManager getInstance() {

        return CacheManager.instance;
    }

    public GatewayServiceInfo getServiceByInterfaceNameAndMethodName(String interfaceName, String methodName) {
        if (StringUtils.isBlank(interfaceName)) {
            return null;
        }
        GatewayServiceInfo gatewayServiceInfo = serviceManager.getByWebInterfaceNameAndMethodName(interfaceName, methodName);
        if (null != gatewayServiceInfo) {
            gatewayServiceInfo.setGatewayInParams(serviceInputParamManager.listInputParamByServiceId(gatewayServiceInfo.getId()));
        }
        return gatewayServiceInfo;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public ServiceInputParamManager getServiceInputParamManager() {
        return serviceInputParamManager;
    }

    public void setServiceInputParamManager(ServiceInputParamManager serviceInputParamManager) {
        this.serviceInputParamManager = serviceInputParamManager;
    }

}
