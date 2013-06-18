/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.controller;

import static com.taobao.diamond.common.Constants.LINE_SEPARATOR;
import static com.taobao.diamond.common.Constants.WORD_SEPARATOR;

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.server.service.ConfigService;
import com.taobao.diamond.server.service.DiskService;
import com.taobao.diamond.server.utils.GlobalCounter;
import com.taobao.diamond.utils.Protocol;

/**
 * 处理配置信息获取和提交的controller
 * 
 * @author boyan
 * @date 2010-5-4
 */
@Controller
@RequestMapping("/config.do")
public class ConfigController {

	@Autowired
	private ConfigService configService;

	@Autowired
	private DiskService diskService;

	/**
	 * 处理配置信息请求
	 * 
	 * @param request
	 * @param dataId
	 * @param clientGroup
	 *            客户端设置的分组信息
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String getConfig(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(Constants.DATAID) String dataId,
			@RequestParam(value = Constants.GROUP, required = false) String group) {
		response.setHeader("Content-Type", "text/html;charset=GBK");
		final String address = getRemortIP(request);
		if (address == null) {
			// 未找到远端地址，返回400错误
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "400";

		}

		if (GlobalCounter.getCounter().decrementAndGet() >= 0) {
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return "503";
		}

		if (group == null) {
			group = Constants.DEFAULT_GROUP;
		}

		String md5 = this.configService.getContentMD5(dataId, group);
		if (md5 != null) {
			response.setHeader(Constants.CONTENT_MD5, md5);

			// 如果Md5值一致，返回304
			String requestMd5 = request.getHeader(Constants.CONTENT_MD5);
			if (md5.equals(requestMd5)) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return "304";
			}
		} else {
			// 没有404.jsp, 返回给客户端是404状态码, 这个状态码实际上是找不到404.jsp所致
			return "404";
		}
		// 正在被修改，返回304，这里的检查并没有办法保证一致性，因此做double-check尽力保证
		if (diskService.isModified(dataId, group)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return "304";
		}
		String path = configService.getConfigInfoPath(dataId, address, group);
		// 再次检查
		if (diskService.isModified(dataId, group)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return "304";
		}
		// 禁用缓存
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setHeader("Cache-Control", "no-cache,no-store");
		return "forward:" + path;
	}

	/**
	 * 处理配置信息请求
	 * 
	 * @param request
	 * @param dataId
	 * @param clientGroup
	 *            客户端设置的分组信息
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String getProbeModifyResult(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(Constants.PROBE_MODIFY_REQUEST) String probeModify) {
		String version = request.getHeader(Constants.CLIENT_VERSION_HEADER);
		if (version == null) {
			version = "2.0.0";
		}
		response.setHeader("Content-Type", "text/html;charset=GBK");
		final String address = getRemortIP(request);
		if (address == null) {
			// 未找到远端地址，返回400错误
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "400";

		}

		if (GlobalCounter.getCounter().decrementAndGet() >= 0) {
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return "503";
		}

		final List<ConfigKey> configKeyList = getConfigKeyList(probeModify);

		StringBuilder resultBuilder = new StringBuilder();
		StringBuilder newResultBuilder = new StringBuilder();
		int index = 0;
		for (ConfigKey key : configKeyList) {
			String md5 = this.configService.getContentMD5(key.getDataId(),
					key.getGroup());
			if (md5 != null) {
				if (!md5.equals(key.getMd5())) {
					resultBuilder.append(key.getDataId()).append(":")
							.append(key.getGroup()).append(";");
					newResultBuilder.append(key.getDataId())
							.append(WORD_SEPARATOR).append(key.getGroup())
							.append(LINE_SEPARATOR);
					index++;
				}
			} else {
				if (key.getMd5() != null) {
					resultBuilder.append(key.getDataId()).append(":")
							.append(key.getGroup()).append(";");
					newResultBuilder.append(key.getDataId())
							.append(WORD_SEPARATOR).append(key.getGroup())
							.append(LINE_SEPARATOR);
					index++;
				}
			}

		}

		// 禁用缓存
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setHeader("Cache-Control", "no-cache,no-store");

		int versionNum = 0;
		if (index > 0) {
			versionNum = Protocol.getVersionNumber(version);
		} else {
			versionNum = -1;
		}
		String returnHeader = newResultBuilder.toString();
		try {
			returnHeader = URLEncoder.encode(newResultBuilder.toString(),
					"UTF-8");
		} catch (Exception e) {
		}
		if (versionNum >= 204) {
			request.setAttribute("content", returnHeader);
		} else if (versionNum >= 0) {
			response.addHeader(Constants.PROBE_MODIFY_RESPONSE_NEW,
					returnHeader);
			response.addHeader(Constants.PROBE_MODIFY_RESPONSE,
					resultBuilder.toString());// 老版的1.0客户端在用
		} else {
			request.setAttribute("content", "");
			response.addHeader(Constants.PROBE_MODIFY_RESPONSE_NEW, "");
			response.addHeader(Constants.PROBE_MODIFY_RESPONSE, "");
		}
		return "200";
	}

	public ConfigService getConfigService() {
		return configService;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public DiskService getDiskService() {
		return diskService;
	}

	public void setDiskService(DiskService diskService) {
		this.diskService = diskService;
	}

	public String getRemortIP(HttpServletRequest request) {
		if (request.getHeader("x-forwarded-for") == null) {
			return request.getRemoteAddr();
		}
		return request.getHeader("x-forwarded-for");
	}

	public List<ConfigKey> getConfigKeyList(String configKeysString) {
		List<ConfigKey> configKeyList = new LinkedList<ConfigKey>();
		if (null == configKeysString || "".equals(configKeysString)) {
			return configKeyList;
		}
		String[] configKeyStrings = configKeysString.split(LINE_SEPARATOR);
		for (String configKeyString : configKeyStrings) {
			String[] configKey = configKeyString.split(WORD_SEPARATOR);
			if (configKey.length > 3) {
				continue;
			}
			ConfigKey key = new ConfigKey();
			if ("".equals(configKey[0])) {
				continue;
			}
			key.setDataId(configKey[0]);
			if (configKey.length >= 2 && !"".equals(configKey[1])) {
				key.setGroup(configKey[1]);
			}
			if (configKey.length == 3 && !"".equals(configKey[2])) {
				key.setMd5(configKey[2]);
			}
			configKeyList.add(key);
		}

		return configKeyList;
	}

	public static class ConfigKey {
		private String dataId;
		private String group;
		private String md5;

		public String getDataId() {
			return dataId;
		}

		public void setDataId(String dataId) {
			this.dataId = dataId;
		}

		public String getGroup() {
			return group;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public String getMd5() {
			return md5;
		}

		public void setMd5(String md5) {
			this.md5 = md5;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("DataID: ").append(dataId).append("\r\n");
			sb.append("Group: ").append((null == group ? "" : group))
					.append("\r\n");
			sb.append("MD5: ").append((null == md5 ? "" : md5)).append("\r\n");
			return sb.toString();
		}
	}
}
