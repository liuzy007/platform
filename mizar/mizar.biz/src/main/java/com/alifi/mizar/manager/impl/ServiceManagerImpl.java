package com.alifi.mizar.manager.impl;

import java.util.List;
import java.util.Map;

import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.dao.PartnerPermissionDao;
import com.alifi.mizar.dao.ServiceDao;
import com.alifi.mizar.dao.ServiceInputParamDao;
import com.alifi.mizar.manager.BaseCachedManager;
import com.alifi.mizar.manager.ServiceManager;

public class ServiceManagerImpl extends BaseCachedManager implements ServiceManager {

    private ServiceDao serviceDao;
    private PartnerPermissionDao partnerPermissionDao;
    private ServiceInputParamDao serviceInputParamDao;

    public GatewayServiceInfo queryClassName(String serviceName) {
        GatewayServiceInfo serviceInfo = (GatewayServiceInfo) memCachedManager.get(addNameSpace(serviceName));
        if (serviceInfo != null) {
            return serviceInfo;
        }
        serviceInfo = serviceDao.getByServiceName(serviceName);
        if (serviceInfo != null) {
            memCachedManager.add(addNameSpace(serviceName), serviceInfo, nextDay());
        }
        return serviceInfo;
    }

    public long countServicesByMap(Map<String, Object> parameters) {
        return serviceDao.countServicesByMap(parameters);
    }

    public List<GatewayServiceInfo> getServicesByMap(Map<String, Object> parameters) {
        return serviceDao.getServicesByMap(parameters);
    }

    public List<GatewayServiceInfo> listServices() {
        return (List<GatewayServiceInfo>) serviceDao.list();
    }

    public GatewayServiceInfo addService(GatewayServiceInfo serviceInfo) {
        return serviceDao.getServiceById(serviceDao.add(serviceInfo));
    }

    public boolean updateServiceById(GatewayServiceInfo serviceInfo) {
        int flag = serviceDao.update(serviceInfo);
        if (flag > 0) {
            memCachedManager.delete(addNameSpace(serviceInfo.getServiceName()));
            return true;
        }
        return false;
    }

    public boolean deleteServiceById(int id) {
        GatewayServiceInfo serviceInfo = getServiceById(id);
        if (serviceInfo == null) {
            return false;
        }
        partnerPermissionDao.deleteByServiceId(id);
        serviceInputParamDao.delByServiceId(id);
        int flag = serviceDao.del(id);
        if (flag > 0) {
            memCachedManager.delete(addNameSpace(serviceInfo.getServiceName()));
            return true;
        }
        return false;
    }

    public GatewayServiceInfo getServiceById(int id) {
        return serviceDao.getServiceById(id);
    }

    public void setServiceDao(ServiceDao serviceDao) {
        this.serviceDao = serviceDao;
    }

    public void setPartnerPermissionDao(PartnerPermissionDao partnerPermissionDao) {
        this.partnerPermissionDao = partnerPermissionDao;
    }

    public void setServiceInputParamDao(ServiceInputParamDao serviceInputParamDao) {
        this.serviceInputParamDao = serviceInputParamDao;
    }

    @Override
    protected String addNameSpace(Object key) {
        return "mizar.service." + key;
    }

    public GatewayServiceInfo getByWebInterfaceNameAndMethodName(String interfaceName, String methodName) {
        String serviceName = interfaceName + "." + methodName;
        GatewayServiceInfo serviceInfo = (GatewayServiceInfo) memCachedManager.get(addNameSpace(serviceName));
        if (serviceInfo != null) {
            return serviceInfo;
        }
        serviceInfo = serviceDao.getByServiceName(serviceName);
        if (serviceInfo != null) {
            memCachedManager.add(addNameSpace(serviceName), serviceInfo, nextDay());
        }
        return serviceInfo;
    }

}
