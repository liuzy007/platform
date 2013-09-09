package com.pktech.oal.job;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.frontend.ServerFactoryBean;

import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.dao.ServiceDao;
import com.pktech.oal.ServiceContextManager;
import com.pktech.oal.webservice.WebServiceHelper;

public class MonitorWebService {
    private static final transient Log log = LogFactory.getLog(MonitorWebService.class);

    private ServiceDao serviceDao;

    public void monitor() {
        List<GatewayServiceInfo> gatewayServiceInfos = serviceDao.listValid();
        if (null == gatewayServiceInfos || gatewayServiceInfos.isEmpty()) {
            // 停止已经发布的webservice
            ServiceContextManager.unregisterAll();
            return;
        }
        // 注册多余的
        for (GatewayServiceInfo gatewayServiceInfo : gatewayServiceInfos) {
            if (!ServiceContextManager.isExistsWebService(gatewayServiceInfo.getWebserviceInterface())) {
                Class<?> clazz = null;

                try {
                    clazz = Class.forName(gatewayServiceInfo.getWebserviceInterface());
                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage());
                    continue;
                }
                ServerFactoryBean serverFactoryBean = WebServiceHelper.deploy(clazz, gatewayServiceInfo.getUrl());

                ServiceContextManager.register(gatewayServiceInfo.getWebserviceInterface(), serverFactoryBean);

            }
        }

        Set<String> keys = ServiceContextManager.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            boolean find = false;
            for (GatewayServiceInfo gatewayServiceInfo : gatewayServiceInfos) {
                if (key.equals(gatewayServiceInfo.getWebserviceInterface())) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                ServiceContextManager.unregister(key);
            }
        }

    }

    public ServiceDao getServiceDao() {
        return serviceDao;
    }

    public void setServiceDao(ServiceDao serviceDao) {
        this.serviceDao = serviceDao;
    }

}
