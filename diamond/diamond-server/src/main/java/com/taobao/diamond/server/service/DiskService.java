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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.server.exception.ConfigServiceException;
import com.taobao.diamond.server.utils.SystemConfig;

/**
 * 磁盘操作服务
 * 
 * @author boyan
 * @date 2010-5-4
 */
public class DiskService {

	private static final Log log = LogFactory.getLog(DiskService.class);

	private ServletContext servletContext;

	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * 修改标记缓存
	 */
	private final ConcurrentHashMap<String, Boolean> modifyMarkCache = new ConcurrentHashMap<String, Boolean>();

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public void saveToDisk(ConfigInfo configInfo) throws IOException {
		if (configInfo == null)
			throw new IllegalArgumentException("configInfo不能为空");
		if (!StringUtils.hasLength(configInfo.getDataId())
				|| StringUtils.containsWhitespace(configInfo.getDataId()))
			throw new IllegalArgumentException("无效的dataId");

		if (!StringUtils.hasLength(configInfo.getGroup())
				|| StringUtils.containsWhitespace(configInfo.getGroup()))
			throw new IllegalArgumentException("无效的group");

		if (!StringUtils.hasLength(configInfo.getContent()))
			throw new IllegalArgumentException("无效的content");

		final String basePath = WebUtils.getRealPath(servletContext,
				Constants.BASE_DIR);
		createDirIfNessary(basePath);
		final String groupPath = WebUtils.getRealPath(servletContext,
				Constants.BASE_DIR + "/" + configInfo.getGroup());
		createDirIfNessary(groupPath);

		String group = configInfo.getGroup();

		String dataId = configInfo.getDataId();

		dataId = SystemConfig.encodeDataIdForFNIfUnderWin(dataId);

		final String dataPath = WebUtils.getRealPath(servletContext,
				Constants.BASE_DIR + "/" + group + "/" + dataId);
		File targetFile = createFileIfNessary(dataPath);

		File tempFile = File.createTempFile(group + "-" + dataId, ".tmp");
		FileOutputStream out = null;
		PrintWriter writer = null;
		try {
			out = new FileOutputStream(tempFile);
			BufferedOutputStream stream = new BufferedOutputStream(out);
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					stream, Constants.ENCODE)));
			configInfo.dump(writer);
			writer.flush();
		} catch (Exception e) {
			log.error("保存磁盘文件失败, tempFile:" + tempFile + ",targetFile:"
					+ targetFile, e);
		} finally {
			if (writer != null)
				writer.close();
		}

		String cacheKey = generateCacheKey(configInfo.getGroup(),
				configInfo.getDataId());
		// 标记
		if (this.modifyMarkCache.putIfAbsent(cacheKey, true) == null) {
			try {
				// 文件内容不一样的情况下才进行拷贝
				if (!FileUtils.contentEquals(tempFile, targetFile)) {
					try {
						// TODO 考虑使用版本号, 保证文件一定写成功 , 或者知道文件没有写成功。
						FileUtils.copyFile(tempFile, targetFile);
					} catch (Throwable t) {
						log.error("保存磁盘文件失败, tempFile:" + tempFile
								+ ", targetFile:" + targetFile, t);
						SystemConfig.system_pause();
						throw new RuntimeException();

					}
				}
				tempFile.delete();
			} finally {
				// 清除标记
				this.modifyMarkCache.remove(cacheKey);
			}
		} else
			throw new ConfigServiceException("配置信息正在被修改");
	}

	public boolean isModified(String dataId, String group) {
		return this.modifyMarkCache.get(generateCacheKey(group, dataId)) != null;
	}
	
	public ConcurrentHashMap<String, Boolean> getModifyMarkCache() {
        return this.modifyMarkCache;
    }

	/**
	 * 生成缓存key，用于标记文件是否正在被修改
	 * 
	 * @param group
	 * @param dataId
	 * 
	 * @return
	 */
	public final String generateCacheKey(String group, String dataId) {
		return group + "/" + dataId;
	}

	public void removeConfigInfo(String dataId, String group)
			throws IOException {
		if (!StringUtils.hasLength(dataId)
				|| StringUtils.containsWhitespace(dataId))
			throw new IllegalArgumentException("无效的dataId");

		if (!StringUtils.hasLength(group)
				|| StringUtils.containsWhitespace(group))
			throw new IllegalArgumentException("无效的group");

		final String basePath = WebUtils.getRealPath(servletContext,
				Constants.BASE_DIR);
		createDirIfNessary(basePath);
		final String groupPath = WebUtils.getRealPath(servletContext,
				Constants.BASE_DIR + "/" + group);
		final File groupDir = new File(groupPath);
		if (!groupDir.exists()) {
			return;
		}
		// 这里并没有去判断group目录是否为空并删除，也就是说哪怕group目录内没有任何dataId文件了也将仍然保留在磁盘上
		String fnDataId = SystemConfig.encodeDataIdForFNIfUnderWin(dataId);
		final String dataPath = WebUtils.getRealPath(servletContext,
				Constants.BASE_DIR + "/" + group + "/" + fnDataId);
		File dataFile = new File(dataPath);
		if (!dataFile.exists()) {
			return;
		}
		String cacheKey = generateCacheKey(group, dataId);
		// 标记
		if (this.modifyMarkCache.putIfAbsent(cacheKey, true) == null) {
			try {
				if (!dataFile.delete())
					throw new ConfigServiceException("删除配置文件失败");
			} finally {
				this.modifyMarkCache.remove(cacheKey);
			}
		} else
			throw new ConfigServiceException("配置文件正在被修改");
	}

	public void removeConfigInfo(ConfigInfo configInfo) throws IOException {
		removeConfigInfo(configInfo.getDataId(), configInfo.getGroup());
	}

	private void createDirIfNessary(String path) {
		final File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}

	private File createFileIfNessary(String path) throws IOException {
		final File file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				log.error("创建文件失败, path=" + path, e);
			}
		}
		return file;
	}

}
