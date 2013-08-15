package com.alifi.mizar.registry.module.screen;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.common.paging.Constants;
import com.alifi.mizar.common.paging.Paging;
import com.alifi.mizar.manager.ServiceManager;


public class Services {
    
    @Autowired
    private ServiceManager serviceManager;
    
    public void execute(
    		@Param("serviceName") String serviceName,
    		@Param("pageSize") long pageSize,
            @Param("pageNumber") long pageNumber,
    		Context context) throws Exception {
        
        Paging<GatewayServiceInfo> paging = this.getServicesByPaging(serviceName, pageSize, pageNumber);
        context.put("paging", paging);
    }
    
    
    public Paging<GatewayServiceInfo> getServicesByPaging(String serviceName, long pageSize, long pageNumber) {
    	
        // 统计
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("serviceName", serviceName);
        Paging<GatewayServiceInfo> paging = new Paging<GatewayServiceInfo>(pageSize, pageNumber, serviceManager.countServicesByMap(parameters));
        if (paging.getElementCount() == 0) {
            return paging;
        }

        // 查询
        if (!paging.isFirstPage()) {
            parameters.put(Constants.PAGE_FIRST_ELEMENT_INDEX, paging.getPageFirstElementIndex());
        }
        parameters.put(Constants.PAGE_LAST_ELEMENT_INDEX, paging.getPageLastElementIndex());

        return paging.setPageElements(serviceManager.getServicesByMap(parameters));
    }

}
