package com.platform.tddl.web.action;

import com.platform.tddl.dao.ServiceConfigDao;
import com.platform.tddl.vo.ServiceConfig;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: tomp
 * Date: 13-7-22
 * Time: 上午12:30
 * To change this template use File | Settings | File Templates.
 */
public class GatewayAction implements Controller {

    private ServiceConfigDao serviceConfigDao;

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setId(Integer.valueOf(request.getParameter("id")));
        serviceConfig.setConverter(request.getParameter("converter"));
        serviceConfig.setName(request.getParameter("name"));
        serviceConfig.setProtocol("http");
        serviceConfig.setTemplate(request.getParameter("template"));
        serviceConfig.setUrl(request.getParameter("url"));

        serviceConfigDao.insert(serviceConfig);
        return null;
    }

    public void setServiceConfigDao(ServiceConfigDao serviceConfigDao) {
        this.serviceConfigDao = serviceConfigDao;
    }
}
