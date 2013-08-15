package com.alifi.mizar.test.helper;

import com.alifi.mizar.common.vo.GatewayServiceInfo;


public class GatewayServiceInfoHelper {
    
    public static GatewayServiceInfo create() {
        GatewayServiceInfo gatewayServiceInfo = new GatewayServiceInfo();
        gatewayServiceInfo.setEndorseSignOut(false);
        gatewayServiceInfo.setId(1);
        gatewayServiceInfo.setInvokeInterface("com.alifi.alcor.service.collie.TaobaoCollieService");
        gatewayServiceInfo.setInvokeMethod("queryUserInfo");
        gatewayServiceInfo.setPublic(true);
        gatewayServiceInfo.setServiceName("taobaoCollieService");
        gatewayServiceInfo.setStatus("o");
        gatewayServiceInfo.setValidateSignIn(false);
        gatewayServiceInfo.setVersion("1.0");
        return gatewayServiceInfo;
    }

    public static GatewayServiceInfo createPrivateInfo() {
        GatewayServiceInfo gatewayServiceInfo = create();
        gatewayServiceInfo.setPublic(false);
        return gatewayServiceInfo;
    }

    public static GatewayServiceInfo createSigedInfo() {
        GatewayServiceInfo gatewayServiceInfo = create();
        gatewayServiceInfo.setValidateSignIn(true);
        return gatewayServiceInfo;
    }
}
