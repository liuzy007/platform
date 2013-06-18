/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.common;

public class Constants {

	public static final String CLIENT_VERSION_HEADER = "Client-Version";

	public static final String CLIENT_VERSION = "2.0.5";

	public static int DATA_IN_BODY_VERSION = 204;
	/**
	 * 默认分组名
	 */
	public static final String DEFAULT_GROUP = "DEFAULT_GROUP";
	/**
	 * 基目录名
	 */
	public static final String BASE_DIR = "config-data";

	public static final String APPNAME = "AppName";

	public static final String DEFAULT_DOMAINNAME = "a.b.c";

	public static final String DAILY_DOMAINNAME = "d.e.f";

	public static final int DEFAULT_PORT = 8080;

	public static final String NULL = "";

	public static final String DATAID = "dataId";

	public static final String GROUP = "group";

	public static final String LAST_MODIFIED = "Last-Modified";

	public static final String ACCEPT_ENCODING = "Accept-Encoding";

	public static final String CONTENT_ENCODING = "Content-Encoding";

	public static final String PROBE_MODIFY_REQUEST = "Probe-Modify-Request";

	public static final String PROBE_MODIFY_RESPONSE = "Probe-Modify-Response";

	public static final String PROBE_MODIFY_RESPONSE_NEW = "Probe-Modify-Response-New";

	public static final String USE_ZIP = "true";

	public static final String CONTENT_MD5 = "Content-MD5";

	public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

	public static final String SPACING_INTERVAL = "client-spacing-interval";

	public static final int POLLING_INTERVAL_TIME = 15;// 秒

	public static final int ONCE_TIMEOUT = 2000;// 毫秒

	public static final int CONN_TIMEOUT = 2000;// 毫秒

	public static final int RECV_WAIT_TIMEOUT = ONCE_TIMEOUT * 5;// 毫秒

	public static final String BASE_URI = "/diamond-server";

	public static final String CONFIG_HTTP_URI_FILE = "/diamond-server/config.co";

	public static final String HTTP_URI_LOGIN = "/diamond-server/login.do";

	public static final String HTTP_URI_ACK = "/diamond-server/client.ack";

	public static final String ENCODE = "GBK";

	public static final String MAP_FILE = "map-file.js";

	public static final int FLOW_CONTROL_THRESHOLD = 20;

	public static final int FLOW_CONTROL_SLOT = 10;

	public static final int FLOW_CONTROL_INTERVAL = 1000;

	public static final String LINE_SEPARATOR = Character.toString((char) 1);

	public static final String WORD_SEPARATOR = Character.toString((char) 2);

	public static final String DEFAULT_DIAMOND_CLUSTER = "diamond";

	public static final String DEFAULT_USERNAME = "user";

	public static final String DEFAULT_PASSWORD = "123";

	public static final String STAT_CLIENT_SUCCESS = "success";

	public static final String STAT_CLIENT_FAILURE = "failure";

	/*
	 * 批量操作时, 单条数据的状态码
	 */
	// 发生异常
	public static final int BATCH_OP_ERROR = -1;
	// 查询成功, 数据存在
	public static final int BATCH_QUERY_EXISTS = 1;
	// 查询成功, 数据不存在
	public static final int BATCH_QUERY_NONEXISTS = 2;
	// 新增成功
	public static final int BATCH_ADD_SUCCESS = 3;
	// 更新成功
	public static final int BATCH_UPDATE_SUCCESS = 4;

}
