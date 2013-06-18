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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.domain.Page;
import com.taobao.diamond.md5.MD5;
import com.taobao.diamond.server.exception.ConfigServiceException;
import com.taobao.diamond.server.utils.DiamondUtils;
import com.taobao.diamond.server.utils.SystemConfig;

public class ConfigService {

	private static final Log log = LogFactory.getLog(ConfigService.class);

	private DiskService diskService;

	private PersistService persistService;

	private NotifyService notifyService;

	/**
	 * content的MD5的缓存,key为group/dataId，value为md5值
	 */
	private final ConcurrentHashMap<String, String> contentMD5Cache = new ConcurrentHashMap<String, String>();

	public void updateMD5Cache(ConfigInfo configInfo) {
		this.contentMD5Cache.put(
				generateMD5CacheKey(configInfo.getDataId(),
						configInfo.getGroup()),
				MD5.getInstance().getMD5String(configInfo.getContent()));
	}

	public String getContentMD5(String dataId, String group) {
		String key = generateMD5CacheKey(dataId, group);
		String md5 = this.contentMD5Cache.get(key);
		if (md5 == null) {
			synchronized (this) {
				// 二重检查
				md5 = this.contentMD5Cache.get(key);
				if (md5 == null) {
					return null;
				} else
					return md5;
			}
		} else
			return md5;
	}

	public String getConfigInfoPath(String dataId, String address, String group) {
		return generatePath(dataId, group);
	}

	String generateMD5CacheKey(String dataId, String group) {
		String key = group + "/" + dataId;
		return key;
	}

	String generatePath(String dataId, final String group) {
		if (!StringUtils.hasLength(dataId)
				|| StringUtils.containsWhitespace(dataId))
			throw new IllegalArgumentException("无效的dataId");

		if (!StringUtils.hasLength(group)
				|| StringUtils.containsWhitespace(group))
			throw new IllegalArgumentException("无效的group");
		String fnDataId = SystemConfig.encodeDataIdForFNIfUnderWin(dataId);
		StringBuilder sb = new StringBuilder("/");
		sb.append(Constants.BASE_DIR).append("/");
		sb.append(group).append("/");
		sb.append(fnDataId);
		return sb.toString();
	}

	/**
	 * 根据dataId和group查找配置信息
	 * 
	 * @param dataId
	 * @param group
	 * @return
	 */
	public ConfigInfo findConfigInfo(String dataId, String group) {
		if (!StringUtils.hasLength(dataId)
				|| StringUtils.containsWhitespace(dataId))
			throw new IllegalArgumentException("无效的dataId");

		if (!StringUtils.hasLength(group)
				|| StringUtils.containsWhitespace(group))
			throw new IllegalArgumentException("无效的group");
		return persistService.findConfigInfo(dataId, group);
	}

	/**
	 * 根据ID删除GroupInfo
	 * 
	 * @param id
	 */
	public void removeConfigInfo(long id) {
		checkOperation("removeConfigInfo");
		try {
			ConfigInfo configInfo = this.persistService.findConfigInfoByID(id);
			this.diskService.removeConfigInfo(configInfo);
			this.contentMD5Cache.remove(generateMD5CacheKey(
					configInfo.getDataId(), configInfo.getGroup()));

			this.persistService.removeConfigInfoByID(id);
			// 通知其他节点
			this.notifyService.notifyConfigInfoChange(configInfo.getDataId(),
					configInfo.getGroup());

		} catch (Exception e) {
			log.error("删除配置信息错误", e);
			throw new ConfigServiceException(e);
		}
	}

	private void checkOperation(String operation) {
		if (SystemConfig.isOfflineMode()) {
			String msg = "OFFLINE模式，不支持的操作:" + operation + "";
			throw new UnsupportedOperationException(msg);
		}
	}

	/**
	 * 添加配置信息, 并将时间戳、源IP和源用户添加到数据库表中
	 * 
	 * @param dataId
	 * @param group
	 * @param content
	 * @param srcIp
	 * @param srcUser
	 */
	public void addConfigInfo(String dataId, String group, String content) {
		checkOperation("addConfigInfo");
		checkParameter(dataId, group, content);
		ConfigInfo configInfo = new ConfigInfo(dataId, group, content);

		// 获取当前时间
		Timestamp currentTime = DiamondUtils.getCurrentTime();
		// 写的顺序: 数据库、内存、磁盘
		try {
			persistService.addConfigInfo(currentTime, configInfo);
			// 切记更新缓存
			this.contentMD5Cache.put(generateMD5CacheKey(dataId, group),
					configInfo.getMd5());
			diskService.saveToDisk(configInfo);
			// 通知其他节点
			this.notifyService.notifyConfigInfoChange(configInfo.getDataId(),
					configInfo.getGroup());
		} catch (Exception e) {
			log.error("保存ConfigInfo失败", e);
			throw new ConfigServiceException(e);
		}
	}

	/**
	 * 更新配置信息
	 * 
	 * @param dataId
	 * @param group
	 * @param content
	 */
	public void updateConfigInfo(String dataId, String group, String content) {
		checkOperation("updateConfigInfo");
		checkParameter(dataId, group, content);
		ConfigInfo configInfo = new ConfigInfo(dataId, group, content);

		Timestamp currentTime = DiamondUtils.getCurrentTime();
		// 先更新数据库，再更新磁盘
		try {
			persistService.updateConfigInfo(currentTime, configInfo);
			// 切记更新缓存
			this.contentMD5Cache.put(generateMD5CacheKey(dataId, group),
					configInfo.getMd5());
			diskService.saveToDisk(configInfo);
			// 通知其他节点
			this.notifyService.notifyConfigInfoChange(configInfo.getDataId(),
					configInfo.getGroup());
		} catch (Exception e) {
			log.error("保存ConfigInfo失败", e);
			throw new ConfigServiceException(e);
		}
	}

	/**
	 * 删除ConfigInfo
	 * 
	 * @param dataId
	 * @param group
	 */
	public void removeConfigInfo(String dataId, String group) {
		checkParameter(dataId, group);
		try {
			this.contentMD5Cache.remove(generateMD5CacheKey(dataId, group));
			diskService.removeConfigInfo(dataId, group);
			persistService.removeConfigInfo(dataId, group);
			this.notifyService.notifyConfigInfoChange(dataId, group);
		} catch (Exception e) {
			log.error("保存ConfigInfo失败", e);
			throw new ConfigServiceException(e);
		}
	}

	/**
	 * 将配置信息从数据库加载到磁盘
	 * 
	 * @param id
	 */
	public void loadConfigInfoToDisk(String dataId, String group) {
		try {
			ConfigInfo configInfo = this.persistService.findConfigInfo(dataId,
					group);
			if (configInfo != null) {
				this.contentMD5Cache.put(generateMD5CacheKey(dataId, group),
						configInfo.getMd5());
				this.diskService.saveToDisk(configInfo);
			} else {
				// 删除文件
				this.contentMD5Cache.remove(generateMD5CacheKey(dataId, group));
				this.diskService.removeConfigInfo(dataId, group);
			}
		} catch (Exception e) {
			log.error("保存ConfigInfo到磁盘失败", e);
			throw new ConfigServiceException(e);
		}
	}

	private void checkParameter(String dataId, String group, String content) {
		if (!StringUtils.hasLength(dataId)
				|| StringUtils.containsWhitespace(dataId))
			throw new ConfigServiceException("无效的dataId");

		if (!StringUtils.hasLength(group)
				|| StringUtils.containsWhitespace(group))
			throw new ConfigServiceException("无效的group");

		if (!StringUtils.hasLength(content))
			throw new ConfigServiceException("无效的content");
	}

	/**
	 * 分页查找配置信息
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param group
	 * @param dataId
	 * @return
	 */
	public Page<ConfigInfo> findConfigInfo(final int pageNo,
			final int pageSize, final String group, final String dataId) {
		if (StringUtils.hasLength(dataId) && StringUtils.hasLength(group)) {
			ConfigInfo ConfigInfo = this.persistService.findConfigInfo(dataId,
					group);
			Page<ConfigInfo> page = new Page<ConfigInfo>();
			if (ConfigInfo != null) {
				page.setPageNumber(1);
				page.setTotalCount(1);
				page.setPagesAvailable(1);
				page.getPageItems().add(ConfigInfo);
			}
			return page;
		} else if (StringUtils.hasLength(dataId)) {
			return this.persistService.findConfigInfoByDataId(pageNo, pageSize,
					dataId);
		} else {
			return this.persistService.findAllConfigInfo(pageNo, pageSize);
		}
	}

	/**
	 * 分页模糊查找配置信息
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param group
	 * @param dataId
	 * @return
	 */
	public Page<ConfigInfo> findConfigInfoLike(final int pageNo,
			final int pageSize, final String group, final String dataId) {
		return this.persistService.findConfigInfoLike(pageNo, pageSize, dataId,
				group);
	}

	private void checkParameter(String dataId, String group) {
		if (!StringUtils.hasLength(dataId)
				|| DiamondUtils.hasInvalidChar(dataId.trim()))
			throw new ConfigServiceException("无效的dataId");

		if (!StringUtils.hasLength(group)
				|| DiamondUtils.hasInvalidChar(group.trim()))
			throw new ConfigServiceException("无效的group");
	}

	public DiskService getDiskService() {
		return diskService;
	}

	public void setDiskService(DiskService diskService) {
		this.diskService = diskService;
	}

	public PersistService getPersistService() {
		return persistService;
	}

	public void setPersistService(PersistService persistService) {
		this.persistService = persistService;
	}

	public NotifyService getNotifyService() {
		return notifyService;
	}

	public void setNotifyService(NotifyService notifyService) {
		this.notifyService = notifyService;
	}

}
