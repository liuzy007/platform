package com.platform.tddl.dao;

import java.util.List;

import com.platform.tddl.vo.ServiceConfig;

/**
 * Created with IntelliJ IDEA.
 * User: tomp
 * Date: 13-5-31
 * Time: 下午3:27
 * To change this template use File | Settings | File Templates.
 */
public class ServiceConfigDao extends BaseDao<ServiceConfig> {

    public ServiceConfig getByName(String name) {
        return this.get("getByName", name);
    }

    public List<ServiceConfig> listServices() {
        return this.list("listServices", null);
    }
}