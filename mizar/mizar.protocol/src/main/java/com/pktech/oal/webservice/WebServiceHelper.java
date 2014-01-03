package com.pktech.oal.webservice;

import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.springframework.aop.framework.ProxyFactory;

import com.pktech.oal.intercept.WebServiceIntercept;

public class WebServiceHelper {

    public static ServerFactoryBean deploy(Class<?> interfaceClass, String url) {
        ProxyFactory factory = new ProxyFactory();
        factory.setInterfaces(new Class[] { interfaceClass });
        WebServiceIntercept webServiceIntercept = new WebServiceIntercept();
        webServiceIntercept.setUrl(url);
        factory.addAdvice(webServiceIntercept);
        Object proxyObject = factory.getProxy();
        
        ServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
        svrFactory.setServiceClass(interfaceClass);
        svrFactory.setAddress("/" + interfaceClass.getSimpleName());
        svrFactory.setServiceBean(proxyObject);

        svrFactory.create();
        return svrFactory;
    }
}
