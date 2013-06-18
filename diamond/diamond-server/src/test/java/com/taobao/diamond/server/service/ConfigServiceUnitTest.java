package com.taobao.diamond.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletContext;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.md5.MD5;
import com.taobao.diamond.server.utils.DiamondUtils;

public class ConfigServiceUnitTest {
	private ConfigService configService;
	private PersistService persistService;
	private DiskService diskService;

	private ServletContext servletContext;

	private IMocksControl mocksControl;

	private File tempFile;

	private String path;
	private NotifyService notifyService;

	@Before
	public void setUp() throws Exception {
		configService = new ConfigService();
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"application.xml");
		persistService = (PersistService) ctx.getBean("persistService");
		this.diskService = new DiskService();
		this.configService.setPersistService(persistService);
		this.configService.setDiskService(diskService);
		notifyService = new NotifyService();
		this.configService.setNotifyService(notifyService);
		mocksControl = EasyMock.createControl();
		servletContext = mocksControl.createMock(ServletContext.class);
		this.diskService.setServletContext(servletContext);
		tempFile = File.createTempFile("ConfigServiceUnitTest", "tmp");
		path = tempFile.getParent();

		truncateTable();
	}

	private void truncateTable() {
		// persistService.getJdbcTemplate().update("delete from config_info");
		// persistService.getJdbcTemplate().update("delete from group_info");
	}

	@Test
	public void testGetConfigInfoPath() {
		String path = this.configService.getConfigInfoPath("dataId",
				"localhost", null);
		assertEquals("/" + Constants.BASE_DIR + "/" + Constants.DEFAULT_GROUP
				+ "/dataId", path);

		path = this.configService.getConfigInfoPath("dataId", "localhost",
				"group");
		assertEquals("/" + Constants.BASE_DIR + "/group/dataId", path);
		path = this.configService.getConfigInfoPath("dataId", "localhost",
				"group1");
		assertEquals("/config-data/group2/dataId", path);
	}

	@Test
	public void testGetContentMD5_UpdateContentMD5() {
		assertNull(this.configService.getContentMD5("dataId", "group"));

		this.persistService.addConfigInfo(DiamondUtils.getCurrentTime(),
				new ConfigInfo("dataId", "group", "test content"));
		String md5 = this.configService.getContentMD5("dataId", "group");
		// assertNotNull(md5);
		// assertEquals(MD5.getInstance().getMD5String("test content"), md5);
		mockServletContext("dataId", "group", "hello world");
		this.configService.updateConfigInfo("dataId", "group", "hello world");
		md5 = this.configService.getContentMD5("dataId", "group");
		assertNotNull(md5);
		assertEquals(MD5.getInstance().getMD5String("hello world"), md5);
		this.mocksControl.verify();

		// 不通过数据库更新
		this.configService.updateMD5Cache(new ConfigInfo("dataId", "group",
				"boyan@taobao.com"));
		md5 = this.configService.getContentMD5("dataId", "group");
		assertEquals(MD5.getInstance().getMD5String("boyan@taobao.com"), md5);
	}

	@Test
	public void testGenerateMD5CacheKey() {
		assertEquals("group/dataid",
				this.configService.generateMD5CacheKey("dataid", "group"));
		assertEquals("test-group/test",
				this.configService.generateMD5CacheKey("test", "test-group"));
	}

	@Test
	public void testAdd_Update_RemoveConfigInfo() throws Exception {
		File file = null;
		try {
			assertNull(this.configService.findConfigInfo("dataId1", "group1"));
			assertNull(this.configService.getContentMD5("dataId1", "group1"));
			file = new File(path + "/" + "config-data/group1/dataId1");
			assertFalse(file.exists());

			// 插入数据，然后更新，查看数据库和文件是否都被更新
			ConfigInfo configInfo = new ConfigInfo("dataId1", "group1",
					"just a test");
			mockServletContext("dataId1", "group1", "just a test");
			this.configService.addConfigInfo(configInfo.getDataId(),
					configInfo.getGroup(), configInfo.getContent());
			ConfigInfo configInfoFromDB = this.configService.findConfigInfo(
					"dataId1", "group1");
			assertNotNull(configInfoFromDB);
			assertEquals(configInfo, configInfoFromDB);
			assertEquals(MD5.getInstance().getMD5String("just a test"),
					configService.getContentMD5("dataId1", "group1"));
			file = new File(path + "/" + "config-data/group1/dataId1");
			assertTrue(file.exists());
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String line = reader.readLine();
			assertEquals("just a test", line);
			reader.close();

			// 更新
			configInfo.setContent("new content");
			configInfo.setMd5(MD5.getInstance().getMD5String("new content"));
			this.configService.updateConfigInfo(configInfo.getDataId(),
					configInfo.getGroup(), configInfo.getContent());
			configInfoFromDB = this.configService.findConfigInfo("dataId1",
					"group1");
			assertNotNull(configInfoFromDB);
			assertEquals(configInfo, configInfoFromDB);
			assertEquals(MD5.getInstance().getMD5String("new content"),
					configService.getContentMD5("dataId1", "group1"));
			file = new File(path + "/" + "config-data/group1/dataId1");
			assertTrue(file.exists());
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			line = reader.readLine();
			assertEquals("new content", line);
			reader.close();

			// 删除
			this.configService.removeConfigInfo("dataId1", "group1");
			assertNull(this.configService.findConfigInfo("dataId1", "group1"));
			assertNull(this.configService.getContentMD5("dataId1", "group1"));
			file = new File(path + "/" + "config-data/group1/dataId1");
			assertFalse(file.exists());

			mocksControl.verify();

		} finally {
			file.delete();
		}

	}

	public void mockServletContext(String dataId, String group, String content) {
		EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR))
				.andReturn(path + "/" + Constants.BASE_DIR).anyTimes();
		EasyMock.expect(
				servletContext.getRealPath("/" + Constants.BASE_DIR + "/"
						+ group))
				.andReturn(path + "/" + Constants.BASE_DIR + "/" + group)
				.anyTimes();
		String dataPath = path + "/" + Constants.BASE_DIR + "/" + group + "/"
				+ dataId;
		EasyMock.expect(
				servletContext.getRealPath("/" + Constants.BASE_DIR + "/"
						+ group + "/" + dataId)).andReturn(dataPath).anyTimes();
		mocksControl.replay();
	}

	@Test
	public void testLoadConfigInfoToDisk() throws Exception {
		File file = new File(path + "/" + "config-data/group1/dataId1");
		assertFalse(file.exists());

		this.persistService.addConfigInfo(DiamondUtils.getCurrentTime(),
				new ConfigInfo("dataId1", "group1", "hello world"));
		file = new File(path + "/" + "config-data/group1/dataId1");
		assertFalse(file.exists());
		mockServletContext("dataId1", "group1", "hello world");
		this.configService.loadConfigInfoToDisk("dataId1", "group1");
		file = new File(path + "/" + "config-data/group1/dataId1");
		assertTrue(file.exists());
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		String line = reader.readLine();
		assertEquals("hello world", line);
		reader.close();

		// 删除数据库再加载，应该删除文件
		this.persistService.removeConfigInfo("dataId1", "group1");
		this.configService.loadConfigInfoToDisk("dataId1", "group1");
		file = new File(path + "/" + "config-data/group1/dataId1");
		assertFalse(file.exists());

	}

	@After
	public void tearDown() {
		tempFile.delete();
	}
}
