package com.alifi.mizar.test.helper;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.alifi.mizar.common.vo.GatewayInParam;
import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.manager.PartnerPermissionManager;
import com.alifi.mizar.manager.ServiceInputParamManager;
import com.alifi.mizar.manager.ServiceManager;
import com.alifi.mizar.util.Constants;


public class GatewayManagerHelper {
    
    private static ServiceManager serviceManager;
    private static ServiceInputParamManager serviceInputParamManager;
    private static PartnerPermissionManager partnerPermissionManager;
    
    public static ServiceManager createServicemanager() {
        if (serviceManager == null) {
            serviceManager = mock(ServiceManager.class);
            when(serviceManager.queryClassName(isA(String.class))).thenAnswer(new Answer<GatewayServiceInfo>() {
                public GatewayServiceInfo answer(InvocationOnMock invocation) throws Throwable {
                    String service = (String) invocation.getArguments()[0];
                    if (service.equals("exist service")) {
                        return GatewayServiceInfoHelper.create();
                    }
                    if (service.equals("private service")) {
                        return GatewayServiceInfoHelper.createPrivateInfo();
                    }
                    if (service.equals("signed service")) {
                        return GatewayServiceInfoHelper.createSigedInfo();
                    }
                    return null;
                }
            });
        }
        return serviceManager;
    }
    
    public static ServiceInputParamManager createServiceInputParamManager() {
        if (serviceInputParamManager == null) {
            serviceInputParamManager = mock(ServiceInputParamManager.class);
            when(serviceInputParamManager.listInputParamByServiceId(anyInt())).thenAnswer(new Answer<List<GatewayInParam>>() {
                public List<GatewayInParam> answer(InvocationOnMock invocation) throws Throwable {
                    int serviceId = (Integer) invocation.getArguments()[0];
                    List<GatewayInParam> inputParams = new ArrayList<GatewayInParam>();
                    GatewayInParam inputParam = new GatewayInParam();
                    if (serviceId == 1) {
                        inputParam.setIndex(1);
                        inputParam.setNullable(false);
                        inputParam.setParamName("taobaoId");
                        inputParam.setParamType(Constants.LONG);
                    }
                    inputParams.add(inputParam);
                    return inputParams;
                }
            });
        }
        return serviceInputParamManager;
    }
    
    public static PartnerPermissionManager createPartnerPermissionManager() {
        if (partnerPermissionManager == null) {
            partnerPermissionManager = mock(PartnerPermissionManager.class);
            when(partnerPermissionManager.hasPermission(anyInt(), anyInt())).thenAnswer(new Answer<Boolean>() {
                public Boolean answer(InvocationOnMock invocation) throws Throwable {
                    int partnerId = (Integer) invocation.getArguments()[0];
                    return partnerId != 0;
                }
            });
        }
        return partnerPermissionManager;
    }

}
