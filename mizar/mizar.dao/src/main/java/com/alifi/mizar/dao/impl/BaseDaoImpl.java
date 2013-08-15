package com.alifi.mizar.dao.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;


public abstract class BaseDaoImpl extends SqlMapClientDaoSupport {

	protected final static Log logger = LogFactory
			.getLog(BaseDaoImpl.class);

	
	protected Object get(String sqlName, Object params) throws DataAccessException {
		try {
			Object obj = this.getSqlMapClientTemplate().queryForObject(sqlName, params);
			return obj;
		} catch (DataAccessException ex) {
			logger.error("get error:", ex);
			throw ex;
		}
	}

	@SuppressWarnings("unchecked")
	protected List getList(String sqlName, Object params)
			throws DataAccessException {
		try {
			List list = this.getSqlMapClientTemplate().queryForList(sqlName,
					params);
			return list;
		} catch (DataAccessException ex) {
			logger.error("getList error:", ex);
			throw ex;
		}
	}
	
	protected int insert(String sqlName, Object params) {
	    try {
	        return (Integer) this.getSqlMapClientTemplate().insert(sqlName, params);
	    } catch (DataAccessException ex) {
	        logger.error("got error when insert:" + ex);
	        throw ex;
	    }
	}
	
	protected int delete(String sqlName, Object params) {
	    try {
            return this.getSqlMapClientTemplate().delete(sqlName, params);
        } catch (DataAccessException ex) {
            logger.error("got error when insert:" + ex);
            throw ex;
        }
	}
	
	protected int update(String sqlName, Object params) {
		try {
			return this.getSqlMapClientTemplate().update(sqlName, params);
		} catch (DataAccessException ex) {
			logger.error("got error when insert:" + ex);
            throw ex;
		}
	}
}
