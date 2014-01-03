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
        // 由于接口是自动生成并发布的，直接取接口名称
        String interfaceName = methodInvocation.getMethod().getDeclaringClass().getSimpleName();

        String methodName = methodInvocation.getMethod().getName();
        Map<String, String> params = new HashMap<String, String>();
        GatewayServiceInfo gatewayServiceInfo = CacheManager.getInstance().getServiceByInterfaceNameAndMethodName(interfaceName, methodName);
        if (null == gatewayServiceInfo) {
            return "系统找不到接口=" + interfaceName + "." + methodName;
        }
        params.put(SERVICENAME, gatewayServiceInfo.getServiceName());

        if (null != methodInvocation.getArguments() && methodInvocation.getArguments().length > 0) {
            List<GatewayInParam> gatewayInParams = gatewayServiceInfo.getGatewayInParams();
            if (null == gatewayInParams || gatewayInParams.size() != methodInvocation.getArguments().length) {
                return "接口=" + interfaceName + "." + methodName + "要求的参数和传入的参数不匹配";
            }
            for (int i = 0; i < methodInvocation.getArguments().length; i++) {
                if (null == methodInvocation.getArguments()[i]) {
                    return "接口=" + interfaceName + "." + methodName + "的参数为null";
                }
                params.put(gatewayInParams.get(i).getParamName(), methodInvocation.getArguments()[i].toString());
            }
        }
        return HttpUtil.doPost(url, params, "utf-8");
    }
}
