package com.taobao.diamond.server.controller;

import static com.taobao.diamond.common.Constants.LINE_SEPARATOR;
import static com.taobao.diamond.common.Constants.WORD_SEPARATOR;
import static org.junit.Assert.assertEquals;

import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.md5.MD5;
import com.taobao.diamond.server.controller.ConfigController.ConfigKey;
import com.taobao.diamond.server.utils.GlobalCounter;

public class ConfigControllerUnitTest extends AbstractControllerUnitTest {
	ConfigController configController;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.configController = new ConfigController();
		this.configController.setConfigService(configService);
		this.configController.setDiskService(diskService);
	}

	@Test
	public void testGetConfig_默认分组() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		ConfigInfo configInfo = new ConfigInfo("dataId1",
				Constants.DEFAULT_GROUP, "test");
		this.configService.updateMD5Cache(configInfo);
		assertEquals("forward:/" + Constants.BASE_DIR + "/"
				+ Constants.DEFAULT_GROUP + "/dataId1",
				configController.getConfig(request, response, "dataId1", null));
	}

	@Test
	public void testGetConfig_客户端定义分组() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		ConfigInfo configInfo = new ConfigInfo("dataId1", "test",
				"test content");
		this.configService.updateMD5Cache(configInfo);
		assertEquals("forward:/config-data/test/dataId1",
				configController
						.getConfig(request, response, "dataId1", "test"));
	}

	@Test
	public void testGetConfig_400() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr(null);
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertEquals("400", configController.getConfig(request, response,
				"dataId1", "test"));
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
	}

	@Test
	public void testGetConfig_503() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GlobalCounter.getCounter().set(1);
		assertEquals("503", configController.getConfig(request, response,
				"dataId1", "test"));
		assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
				response.getStatus());
		assertEquals(0, GlobalCounter.getCounter().get());
	}

	@Test
	public void testGetConfig_404() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertEquals("404", configController.getConfig(request, response,
				"dataId1", "test404"));
	}

	@Test
	public void testGetConfig_304() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		ConfigInfo configInfo = new ConfigInfo("dataId1", "test1", "test 304");
		this.configService.updateMD5Cache(configInfo);
		this.diskService.getModifyMarkCache().put(
				this.diskService.generateCacheKey("test1", "dataId1"), true);
		assertEquals("304", configController.getConfig(request, response,
				"dataId1", "test1"));
		assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatus());
	}

	@Test
	public void testGetConfig_304_md5() {
		String content = "test 304 md5";
		String requestMd5 = MD5.getInstance().getMD5String(content);
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addHeader(Constants.CONTENT_MD5, requestMd5);
		ConfigInfo configInfo = new ConfigInfo("dataId1", "test1", content);
		this.configService.updateMD5Cache(configInfo);
		assertEquals("304", configController.getConfig(request, response,
				"dataId1", "test1"));
		assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatus());
	}

	@Test
	public void testGetConfig_md5() {
		mockServletContext("dataId1", "test", "hello world");
		this.configService.addConfigInfo("dataId1", "test", "hello world");
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertEquals("forward:/config-data/test/dataId1",
				configController
						.getConfig(request, response, "dataId1", "test"));
		assertEquals(MD5.getInstance().getMD5String("hello world"),
				response.getHeader(Constants.CONTENT_MD5));
		this.mocksControl.verify();

	}

	@Test
	public void testGetConfigKeyList() {
		List<ConfigKey> configKeyList = null;
		configKeyList = this.configController.getConfigKeyList("dataId1"
				+ WORD_SEPARATOR + "group1" + WORD_SEPARATOR + "md51"
				+ LINE_SEPARATOR + "dataId2" + WORD_SEPARATOR + "group2"
				+ WORD_SEPARATOR + "md52" + LINE_SEPARATOR + "dataId2"
				+ WORD_SEPARATOR + "group2" + WORD_SEPARATOR + "md52"
				+ LINE_SEPARATOR);
		Assert.assertEquals(3, configKeyList.size());

		configKeyList = this.configController.getConfigKeyList("dataId1"
				+ WORD_SEPARATOR + WORD_SEPARATOR + "md51" + LINE_SEPARATOR
				+ "dataId2" + WORD_SEPARATOR + "group2" + WORD_SEPARATOR
				+ "md52" + LINE_SEPARATOR + "dataId2" + WORD_SEPARATOR
				+ "group2" + WORD_SEPARATOR + "md52" + LINE_SEPARATOR);
		Assert.assertEquals(3, configKeyList.size());

		configKeyList = this.configController.getConfigKeyList("dataId1"
				+ WORD_SEPARATOR + "group1" + WORD_SEPARATOR + LINE_SEPARATOR
				+ "dataId2" + WORD_SEPARATOR + "group2" + WORD_SEPARATOR
				+ "md52" + LINE_SEPARATOR + "dataId2" + WORD_SEPARATOR
				+ "group2" + WORD_SEPARATOR + "md52" + LINE_SEPARATOR);
		Assert.assertEquals(3, configKeyList.size());

		configKeyList = this.configController.getConfigKeyList("dataId1:;;;"
				+ WORD_SEPARATOR + WORD_SEPARATOR + LINE_SEPARATOR + "dataId2"
				+ WORD_SEPARATOR + "group2" + WORD_SEPARATOR + "md52"
				+ LINE_SEPARATOR + "dataId2" + WORD_SEPARATOR + "group2"
				+ WORD_SEPARATOR + "md52" + LINE_SEPARATOR);
		Assert.assertEquals(3, configKeyList.size());

		StringBuilder buf = new StringBuilder();
		buf.append("aaa:;:").append(WORD_SEPARATOR).append("groupId1:")
				.append(WORD_SEPARATOR).append("md1").append(LINE_SEPARATOR);
		buf.append("bbb:;:").append(WORD_SEPARATOR).append("groupId2:")
				.append(WORD_SEPARATOR).append("md2").append(LINE_SEPARATOR);
		buf.append("ccc:;:").append(WORD_SEPARATOR).append(WORD_SEPARATOR)
				.append("md3").append(LINE_SEPARATOR);
		configKeyList = this.configController.getConfigKeyList(buf.toString());
		Assert.assertEquals(3, configKeyList.size());
		Assert.assertEquals("aaa:;:", configKeyList.get(0).getDataId());
		Assert.assertEquals("groupId1:", configKeyList.get(0).getGroup());
		Assert.assertEquals("md1", configKeyList.get(0).getMd5());

		Assert.assertEquals("bbb:;:", configKeyList.get(1).getDataId());
		Assert.assertEquals("groupId2:", configKeyList.get(1).getGroup());
		Assert.assertEquals("md2", configKeyList.get(1).getMd5());

		Assert.assertEquals("ccc:;:", configKeyList.get(2).getDataId());
		Assert.assertNull(configKeyList.get(2).getGroup());
		Assert.assertEquals("md3", configKeyList.get(2).getMd5());

	}

	@Test
	public void testGetRemoteIp() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("x-forwarded-for", "192.168.0.1");
		Assert.assertEquals("192.168.0.1",
				this.configController.getRemortIP(request));

		request = new MockHttpServletRequest();
		Assert.assertEquals("127.0.0.1",
				this.configController.getRemortIP(request));
	}

	@Test
	public void testGetProbe_400() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRemoteAddr(null);
		assertEquals("400", configController.getProbeModifyResult(request,
				response, "dataId1" + WORD_SEPARATOR + "test"));
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
	}

	@Test
	public void testGetProbe_503() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		GlobalCounter.getCounter().set(1);
		assertEquals("503", configController.getProbeModifyResult(request,
				response, "dataId1" + WORD_SEPARATOR + "test"));
		assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
				response.getStatus());
		assertEquals(0, GlobalCounter.getCounter().get());
	}

	@Test
	public void testGetProbe_MD5不为空且和缓存中的MD5值不同() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(Constants.CLIENT_VERSION_HEADER, "2.0.5");
		MockHttpServletResponse response = new MockHttpServletResponse();
		String dataId = "probe1";
		String group = "test";
		String content = "test probe md5";
		// 探测的dataId的MD5和缓存中的MD5不同
		String probeMd5 = "probeMd5";
		ConfigInfo configInfo = new ConfigInfo(dataId, group, content);
		this.configService.updateMD5Cache(configInfo);

		String probeString = dataId + WORD_SEPARATOR + group + WORD_SEPARATOR
				+ probeMd5 + LINE_SEPARATOR;
		String expected = dataId + WORD_SEPARATOR + group + LINE_SEPARATOR;
		expected = URLEncoder.encode(expected, "UTF-8");

		Assert.assertEquals("200", this.configController.getProbeModifyResult(
				request, response, probeString));
		Assert.assertEquals(expected, request.getAttribute("content"));
	}

	@Test
	public void testGetProbe_MD5不为空且和缓存中的MD5相同() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		String dataId = "probe2";
		String group = "test";
		String content = "test probe md5";
		// 探测的dataId的MD5和缓存中的MD5相同
		String probeMd5 = MD5.getInstance().getMD5String(content);
		ConfigInfo configInfo = new ConfigInfo(dataId, group, content);
		this.configService.updateMD5Cache(configInfo);

		String probeString = dataId + WORD_SEPARATOR + group + WORD_SEPARATOR
				+ probeMd5 + LINE_SEPARATOR;

		Assert.assertEquals("200", this.configController.getProbeModifyResult(
				request, response, probeString));
		Assert.assertEquals("", request.getAttribute("content"));
		Assert.assertEquals("",
				response.getHeader(Constants.PROBE_MODIFY_RESPONSE_NEW));
	}

	@Test
	public void testGetProbe_MD5不为空缓存中的MD5为空() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(Constants.CLIENT_VERSION_HEADER, "2.0.5");
		MockHttpServletResponse response = new MockHttpServletResponse();
		String dataId = "probe3";
		String group = "test";
		String probeMd5 = "probeMd5";

		String probeString = dataId + WORD_SEPARATOR + group + WORD_SEPARATOR
				+ probeMd5 + LINE_SEPARATOR;
		String expected = dataId + WORD_SEPARATOR + group + LINE_SEPARATOR;
		expected = URLEncoder.encode(expected, "UTF-8");

		Assert.assertEquals("200", this.configController.getProbeModifyResult(
				request, response, probeString));
		Assert.assertEquals(expected, request.getAttribute("content"));
	}

	@Test
	public void testGetProbe_MD5为空且缓存中的MD5也为空() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		String dataId = "probe4";
		String group = "test";

		String probeString = dataId + WORD_SEPARATOR + group + WORD_SEPARATOR
				+ LINE_SEPARATOR;

		Assert.assertEquals("200", this.configController.getProbeModifyResult(
				request, response, probeString));
		Assert.assertEquals("", request.getAttribute("content"));
		Assert.assertEquals("",
				response.getHeader(Constants.PROBE_MODIFY_RESPONSE_NEW));
	}

	@Test
	public void testGetProbe_老版本客户端() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(Constants.CLIENT_VERSION_HEADER, "2.0.3");
		MockHttpServletResponse response = new MockHttpServletResponse();
		String dataId = "probe5";
		String group = "test";
		String probeMd5 = "probeMd5";

		String probeString = dataId + WORD_SEPARATOR + group + WORD_SEPARATOR
				+ probeMd5 + LINE_SEPARATOR;
		String expected = dataId + WORD_SEPARATOR + group + LINE_SEPARATOR;
		expected = URLEncoder.encode(expected, "UTF-8");

		Assert.assertEquals("200", this.configController.getProbeModifyResult(
				request, response, probeString));
		Assert.assertEquals(expected,
				response.getHeader(Constants.PROBE_MODIFY_RESPONSE_NEW));
	}

}
