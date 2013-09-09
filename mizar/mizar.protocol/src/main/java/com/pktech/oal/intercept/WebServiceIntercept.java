package com.pktech.oal.intercept;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.alifi.mizar.common.vo.GatewayInParam;
import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.pktech.oal.cache.CacheManager;
import com.pktech.oal.util.HttpUtil;

public class WebServiceIntercept implements MethodInterceptor {
    public final String SERVICENAME = "service";
    private String url;

    public void setUrl(String url) {
        this.url = url;
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        String interfaceName = methodInvocation.getMethod().getDeclaringClass().getName();
        String methodName = methodInvocation.getMethod().getName();
        Map<String, String> params = new HashMap<String, String>();
        GatewayServiceInfo gatewayServiceInfo = CacheManager.getInstance().getServiceByInterfaceNameAndMethodName(interfaceName, methodName);
        params.put(SERVICENAME, gatewayServiceInfo.getServiceName());
        if (null != methodInvocation.getArguments() && methodInvocation.getArguments().length > 0) {
            List<GatewayInParam> gatewayInParams = gatewayServiceInfo.getGatewayInParams();
            if (null == gatewayInParams || gatewayInParams.size() != methodInvocation.getArguments().length) {
                return "error";
            }
            for (int i = 0; i < methodInvocation.getArguments().length; i++) {
                params.put(gatewayInParams.get(i).getParamName(), methodInvocation.getArguments()[i].toString());
            }
        }
        return HttpUtil.doPost(url, params, "utf-8");
    }
}
