package com.pktech.oal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.cxf.frontend.ServerFactoryBean;

public class ServiceContextManager {
    private static Map<String, ServerFactoryBean> serverFactoryBeans = Collections.synchronizedMap(new HashMap<String, ServerFactoryBean>());

    public static boolean isExistsWebService(String interfaceName) {
        return serverFactoryBeans.containsKey(interfaceName);
    }

    public static void register(String interfaceName, ServerFactoryBean serverFactoryBean) {
        if (null != interfaceName) {
            serverFactoryBeans.put(interfaceName, serverFactoryBean);
        }
    }

    public static void unregister(String interfaceName) {
        ServerFactoryBean serverFactoryBean = serverFactoryBeans.remove(interfaceName);
        if (null != serverFactoryBean) {
            serverFactoryBean.destroy();
            serverFactoryBean = null;
        }
    }

    public static void unregisterAll() {
        for (String key : serverFactoryBeans.keySet()) {
            unregister(key);
        }
        serverFactoryBeans.clear();
    }

    public static Set<String> keySet() {
        return serverFactoryBeans.keySet();
    }

}
