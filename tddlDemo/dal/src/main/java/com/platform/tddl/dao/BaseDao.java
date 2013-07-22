package com.platform.tddl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tomp
 * Date: 13-5-31
 * Time: 下午3:28
 * To change this template use File | Settings | File Templates.
 */
public class BaseDao<T> extends SqlMapClientDaoSupport {

//    protected final Log logger = LogFactory.getLog(this.getClass());

    protected T get(String sqlName, Object params) {
        return (T) this.getSqlMapClientTemplate().queryForObject(prefix(sqlName), params);
    }

    protected List<T> list(String sqlName, Object params) {
        return (List<T>) this.getSqlMapClientTemplate().queryForList(prefix(sqlName), params);
    }

    public void insert(T t) {
        this.getSqlMapClientTemplate().insert(prefix("insert"), t);
    }

    public void update(T t) {
        this.getSqlMapClientTemplate().update(prefix("update"), t);
    }

    private String prefix(String sqlName) {
        return new StringBuilder().append(this.getClass().getSimpleName()).append(".").append(sqlName).toString();
    }

}
