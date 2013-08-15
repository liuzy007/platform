package com.alifi.mizar.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

public interface BaseDao {	

    /**
     * 根据主键获得实体
     * 
     * @param sqlName
     * @param params
     * @return
     * @throws DataAccessException
     */
    public Object get(String sqlName, Object params) throws DataAccessException;

    /**
     * 查询列表.带参数.
     * 
     * @param sqlName
     * @param params
     * @return
     * @throws DataAccessException
     */
	public List<?> getList(String sqlName, Object params) throws DataAccessException;
    
	/**
	 * 插入新记录
	 * @param sqlName
	 * @param params
	 * @return 新记录的ID
	 * @throws DataAccessException
	 */
	public int insert(String sqlName, Object params) throws DataAccessException;
	
	/**
	 * 删除记录
	 * @param sqlName
	 * @param params
	 * @return 删除的记录条数
	 * @throws DataAccessException
	 */
	public int delete(String sqlName, Object params) throws DataAccessException;
	
	
	//以下为新增方法
	
	/**
	 * 更新记录
	 * @param sqlName
	 * @param params
	 * @return 更新的记录条数
	 * @throws DataAccessException
	 */
	public int update(String sqlName, Object params) throws DataAccessException;
}
