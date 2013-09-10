package com.pktech.oal.job;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.frontend.ServerFactoryBean;

import com.alifi.mizar.common.vo.GatewayInParam;
import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.dao.ServiceDao;
import com.alifi.mizar.dao.ServiceInputParamDao;
import com.pktech.oal.ServiceContextManager;
import com.pktech.oal.compile.DynamicEngine;
import com.pktech.oal.freemarker.FreeMarkerUtil;
import com.pktech.oal.freemarker.Interface;
import com.pktech.oal.freemarker.Method;
import com.pktech.oal.freemarker.Parameter;
import com.pktech.oal.util.CommonUtil;
import com.pktech.oal.webservice.WebServiceHelper;

public class MonitorWebService {
    private static final transient Log log = LogFactory.getLog(MonitorWebService.class);

    public static final String PACKAGENAME = "mizar";

    private final String JAVA_CLASS_PATH = "java.class.path";
    private final String WEBAPP_ROOT = "webapp.root";
    private final String USER_DIR = "user.dir";
    private final String WEBCLASSPATH = "WEB-INF/classes";
    private final String JETCLASSPATH1 = "/target/classes";
    private final String JETCLASSPATH2 = "\\target\\classes";
    private ServiceDao serviceDao;
    private ServiceInputParamDao serviceInputParamDao;
    private Boolean firstStart = true;
    private String currentClassPath;

    private Interface getInterfaceByInterface(List<GatewayServiceInfo> gatewayServiceInfos, String interfaceName) {
        Interface tmpInterface = new Interface();
        tmpInterface.setInterfaceName(CommonUtil.getSimpleClassName(interfaceName));
        List<Method> methods = new ArrayList<Method>();
        for (GatewayServiceInfo gatewayServiceInfo : gatewayServiceInfos) {
            if (interfaceName.equals(gatewayServiceInfo.getWebserviceInterface())) {
                Method method = new Method();
                method.setName(gatewayServiceInfo.getWebserviceMethod());
                method.setReturnType(gatewayServiceInfo.getReturnType());
                List<GatewayInParam> params = serviceInputParamDao.listByServiceId(gatewayServiceInfo.getId());
                if (null != params && params.size() > 0) {
                    List<Parameter> parameters = new ArrayList<Parameter>();
                    for (GatewayInParam gatewayInParam : params) {
                        parameters.add(new Parameter(gatewayInParam.getParamType(), gatewayInParam.getParamName()));
                    }
                    method.setParameters(parameters);
                }
                methods.add(method);
            }
        }
        tmpInterface.setMethods(methods);
        return tmpInterface;
    }

    private void clearTmpFile(String filePath) {
        File tmpFile = new File(CommonUtil.classPathParser(filePath) + PACKAGENAME);
        if (!tmpFile.isDirectory()) {
            return;
        }
        // 清除历史文件
        File[] files = tmpFile.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }

    }

    private void initClassPath() {
        String classpath = System.getProperty(JAVA_CLASS_PATH);
        System.out.print("classpath------------------" + classpath);
        if (null != classpath && !classpath.isEmpty()) {
            String[] classpaths = classpath.split(";");
            for (String value : classpaths) {
                if (value.indexOf(this.JETCLASSPATH1) > 0 || value.indexOf(this.JETCLASSPATH2) > 0) {
                    currentClassPath = value;
                    return;
                }
            }
        }
        String webroot = System.getProperty(this.WEBAPP_ROOT);
        System.out.print("webroot------------------" + webroot);
        currentClassPath = CommonUtil.classPathParser(webroot) + this.WEBCLASSPATH;
        System.out.print("currentClassPath------------------" + currentClassPath);
    }

    public void monitor() {
        if (firstStart) {
            initClassPath();
            clearTmpFile(currentClassPath);
            firstStart = false;
        }
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
                String className = PACKAGENAME + "." + CommonUtil.getSimpleClassName(gatewayServiceInfo.getWebserviceInterface());
                try {
                    // clazz =
                    // Class.forName(gatewayServiceInfo.getWebserviceInterface());
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    // 目前只能放在类路径下否则无法通过Class.forName 获取
                    // 由于开发路径和发布路径的classpath不一致,要特殊处理一下
                    //
                    Interface tmpInterface = getInterfaceByInterface(gatewayServiceInfos, gatewayServiceInfo.getWebserviceInterface());
                    String srcString = FreeMarkerUtil.processPath(Interface.class, "generateInterfaceTemplate.ftl", tmpInterface);
                    clazz = DynamicEngine.getInstance().compile(className, currentClassPath, srcString);
                    if (null == clazz) {
                        log.error("Can not class name =" + className);
                        continue;
                    }
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

    public ServiceInputParamDao getServiceInputParamDao() {
        return serviceInputParamDao;
    }

    public void setServiceInputParamDao(ServiceInputParamDao serviceInputParamDao) {
        this.serviceInputParamDao = serviceInputParamDao;
    }

}
