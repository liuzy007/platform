/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.service;

import java.sql.Timestamp;

import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.domain.Page;

/**
 * 数据库服务，提供ConfigInfo在数据库的存取服务
 * 
 * @author boyan
 * @since 1.0
 */

public interface PersistService {

	/**
	 * 新增配置信息
	 * 
	 * @param time
	 * @param configInfo
	 */
	public void addConfigInfo(final Timestamp time, final ConfigInfo configInfo);

	/**
	 * 根据dataId和group删除配置信息
	 * 
	 * @param dataId
	 * @param group
	 */
	public void removeConfigInfo(final String dataId, final String group);

	/**
	 * 根据主键ID删除配置信息
	 * 
	 * @param id
	 */
	public void removeConfigInfoByID(final long id);

	/**
	 * 更新配置数据
	 * 
	 * @param time
	 * @param configInfo
	 */
	public void updateConfigInfo(final Timestamp time,
			final ConfigInfo configInfo);

	/**
	 * 根据dataId和group查询配置信息
	 * 
	 * @param dataId
	 * @param group
	 * @return
	 */
	public ConfigInfo findConfigInfo(final String dataId, final String group);

	/**
	 * 根据主键ID查询配置信息
	 * 
	 * @param id
	 * @return
	 */
	public ConfigInfo findConfigInfoByID(long id);

	/**
	 * 分页查询所有的配置信息
	 * 
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页大小
	 * @return
	 */
	public Page<ConfigInfo> findAllConfigInfo(final int pageNo,
			final int pageSize);

	/**
	 * 根据dataId查询配置信息
	 * 
	 * @param pageNo
	 *            页数
	 * @param pageSize
	 *            每页大小
	 * @param dataId
	 *            dataId
	 * @return
	 */
	public Page<ConfigInfo> findConfigInfoByDataId(final int pageNo,
			final int pageSize, final String dataId);

	/**
	 * 根据dataId和group模糊查询配置信息
	 * 
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页大小
	 * @param dataId
	 * @param group
	 * @return
	 */
	public Page<ConfigInfo> findConfigInfoLike(final int pageNo,
			final int pageSize, final String dataId, final String group);

}
