package com.taobao.diamond.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.sql.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;

import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.domain.Page;
import com.taobao.diamond.server.utils.DiamondUtils;

public class PersistServiceUnitTest {
	private PersistService persistService;

	@Before
	public void setUp() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"application.xml");
		persistService = (PersistService) ctx.getBean("persistService");
		truncateTable();
	}

	@After
	public void tearDown() {
		// truncateTable();
	}

	private void truncateTable() {
		((DBPersistService) persistService).getJdbcTemplate().update(
				"delete from config_info");
		((DBPersistService) persistService).getJdbcTemplate().update(
				"delete from group_info");
	}

	private Timestamp getCurrentTime() {
		return DiamondUtils.getCurrentTime();
	}

	@Test
	public void testAddConfigInfo_GetConfigInfo() {
		ConfigInfo configInfo = new ConfigInfo("test-dataId1", "test-group",
				"test content");
		this.persistService.addConfigInfo(this.getCurrentTime(), configInfo);

		ConfigInfo configInfoFromDB = this.persistService.findConfigInfo(
				configInfo.getDataId(), configInfo.getGroup());
		assertNotSame(configInfo, configInfoFromDB);
		assertEquals(configInfo, configInfoFromDB);
	}

	@Test(expected = DataAccessException.class)
	public void testAddConfigInfo_NullDataId() {
		ConfigInfo configInfo = new ConfigInfo(null, "test-group",
				"test content");
		this.persistService.addConfigInfo(this.getCurrentTime(), configInfo);

	}

	@Test(expected = DataAccessException.class)
	public void testAddConfigInfo_NullContent() {
		ConfigInfo configInfo = new ConfigInfo("test-dataId1", "test-group",
				null);
		this.persistService.addConfigInfo(this.getCurrentTime(), configInfo);

	}

	@Test(expected = DataAccessException.class)
	public void testAddConfigInfo_NullGroup() {
		ConfigInfo configInfo = new ConfigInfo("test-dataId1", null,
				"test-content");
		this.persistService.addConfigInfo(this.getCurrentTime(), configInfo);

	}

	@Test
	public void testGetConfigInfo() {
		ConfigInfo configInfoFromDB = this.persistService.findConfigInfo(
				"test-dataId2", "test-group2");
		assertNull(configInfoFromDB);
		ConfigInfo configInfo = new ConfigInfo("test-dataId2", "test-group2",
				"test content");
		this.persistService.addConfigInfo(this.getCurrentTime(), configInfo);
		configInfoFromDB = this.persistService.findConfigInfo("test-dataId2",
				"test-group2");
		assertNotNull(configInfoFromDB);
		assertNotSame(configInfo, configInfoFromDB);
		assertEquals(configInfo, configInfoFromDB);
	}

	@Test
	public void testRemoveConfigInfo() {
		ConfigInfo configInfo = new ConfigInfo("test-dataId3", "test-group3",
				"test content");
		this.persistService.addConfigInfo(this.getCurrentTime(), configInfo);
		ConfigInfo configInfoFromDB = this.persistService.findConfigInfo(
				"test-dataId3", "test-group3");
		assertNotNull(configInfoFromDB);
		assertNotSame(configInfo, configInfoFromDB);

		this.persistService.removeConfigInfo("test-dataId3", "test-group3");
		configInfoFromDB = this.persistService.findConfigInfo("test-dataId3",
				"test-group3");
		assertNull(configInfoFromDB);// 已经被删除

		// 测试根据id删除
		configInfo = new ConfigInfo("test-dataId3", "test-group3",
				"test content");
		this.persistService.addConfigInfo(this.getCurrentTime(), configInfo);
		configInfoFromDB = this.persistService.findConfigInfo("test-dataId3",
				"test-group3");
		assertNotNull(configInfoFromDB);
		this.persistService.removeConfigInfoByID(configInfoFromDB.getId());
		configInfoFromDB = this.persistService.findConfigInfo("test-dataId3",
				"test-group3");
		assertNull(configInfoFromDB);// 已经被删除
	}

	@Test
	public void testRemoveConfigInfo_不存在的记录() {
		truncateTable();
		this.persistService.removeConfigInfo("test-dataId4", "test-group4");
	}

	@Test
	public void testPagination() {
		truncateTable();
		int count = 300;
		final String dataId = "test-dataId";
		for (int i = 0; i < count; i++) {
			ConfigInfo configInfo = new ConfigInfo(dataId, "test-group" + i,
					"test content" + i);
			this.persistService
					.addConfigInfo(this.getCurrentTime(), configInfo);
		}
		Page<ConfigInfo> page1 = this.persistService.findConfigInfoByDataId(1,
				100, dataId);
		Page<ConfigInfo> page2 = this.persistService.findConfigInfoByDataId(2,
				100, dataId);
		Page<ConfigInfo> page3 = this.persistService.findConfigInfoByDataId(3,
				100, dataId);

		assertNotNull(page1);
		assertNotNull(page2);
		assertNotNull(page3);
		assertEquals(3, page1.getPagesAvailable());
		assertEquals(3, page2.getPagesAvailable());
		assertEquals(3, page3.getPagesAvailable());

		assertEquals(1, page1.getPageNumber());
		assertEquals(2, page2.getPageNumber());
		assertEquals(3, page3.getPageNumber());

		assertEquals(100, page1.getPageItems().size());
		assertEquals(100, page2.getPageItems().size());
		assertEquals(100, page3.getPageItems().size());

		assertFalse(page1.getPageItems().equals(page2.getPageItems()));
		assertFalse(page2.getPageItems().equals(page3.getPageItems()));
		assertFalse(page1.getPageItems().equals(page3.getPageItems()));
	}

	@Test
	public void testFindConfigInfoByID() {
		ConfigInfo configInfo = new ConfigInfo("dataId", "notify",
				"hello world");
		this.persistService.addConfigInfo(this.getCurrentTime(), configInfo);
		ConfigInfo configInfoFromDB = this.persistService.findConfigInfo(
				"dataId", "notify");
		ConfigInfo configInfoFromDB2 = this.persistService
				.findConfigInfoByID(configInfoFromDB.getId());
		assertNotSame(configInfo, configInfoFromDB);
		assertNotSame(configInfo, configInfoFromDB2);
		assertNotSame(configInfoFromDB2, configInfoFromDB);
		assertEquals(configInfo, configInfoFromDB);
		assertEquals(configInfoFromDB, configInfoFromDB2);
		this.persistService.removeConfigInfo("dataId", "notify");

	}

	@Test
	public void testUpdateConfigInfo() {
		ConfigInfo configInfo = new ConfigInfo("dataId", "notify",
				"hello world");
		this.persistService.addConfigInfo(this.getCurrentTime(), configInfo);
		ConfigInfo configInfoFromDB = this.persistService.findConfigInfo(
				"dataId", "notify");

		configInfo.setContent("diamond");
		this.persistService.updateConfigInfo(this.getCurrentTime(), configInfo);
		configInfoFromDB = this.persistService
				.findConfigInfoByID(configInfoFromDB.getId());
		assertEquals("diamond", configInfoFromDB.getContent());
	}

	@Test
	public void testFindAllConfigInfo() {
		truncateTable();
		int count = 100;
		final String dataId = "test-dataId";
		for (int i = 0; i < count; i++) {
			ConfigInfo configInfo = new ConfigInfo(dataId, "test-group" + i,
					"test content" + i);
			this.persistService
					.addConfigInfo(this.getCurrentTime(), configInfo);
		}

		Page<ConfigInfo> page = this.persistService.findAllConfigInfo(1, 100);
		assertNotNull(page);
		assertEquals(count, page.getPageItems().size());
		assertEquals(1, page.getPagesAvailable());
		assertNull(this.persistService.findAllConfigInfo(2, 100));

	}

	@Test
	public void testFindConfigInfoLike_DataId() {
		truncateTable();
		int count = 100;
		final String dataId = "test-dataId";
		final String group = "test-group";
		for (int i = 0; i < count; i++) {
			ConfigInfo configInfo = new ConfigInfo(dataId, group + i,
					"test content" + i);
			this.persistService
					.addConfigInfo(this.getCurrentTime(), configInfo);
		}
		Page<ConfigInfo> page = this.persistService.findConfigInfoLike(1, 100,
				dataId, null);
		assertNotNull(page);
		assertEquals(count, page.getPageItems().size());
		assertEquals(1, page.getPagesAvailable());
		assertNull(persistService.findConfigInfoLike(2, 100, dataId, null));
	}

	@Test
	public void testFindConfigInfoLike_Group() {
		truncateTable();
		int count = 100;
		final String dataId = "test-dataId";
		final String group = "test-group";
		for (int i = 0; i < count; i++) {
			ConfigInfo configInfo = new ConfigInfo(dataId, group + i,
					"test content" + i);
			this.persistService
					.addConfigInfo(this.getCurrentTime(), configInfo);
		}
		Page<ConfigInfo> page = this.persistService.findConfigInfoLike(1, 100,
				null, group);
		assertNotNull(page);
		assertEquals(count, page.getPageItems().size());
		assertEquals(1, page.getPagesAvailable());
		assertNull(persistService.findConfigInfoLike(2, 100, null, group));
	}

	@Test
	public void testFindConfigInfoLike_Group_dataId() {
		truncateTable();
		int count = 100;
		final String dataId = "test-dataId";
		final String group = "test-group";
		for (int i = 0; i < count; i++) {
			ConfigInfo configInfo = new ConfigInfo(dataId, group + i,
					"test content" + i);
			this.persistService
					.addConfigInfo(this.getCurrentTime(), configInfo);
		}
		Page<ConfigInfo> page = this.persistService.findConfigInfoLike(1, 100,
				dataId, group);
		assertNotNull(page);
		assertEquals(count, page.getPageItems().size());
		assertEquals(1, page.getPagesAvailable());
		assertNull(persistService.findConfigInfoLike(2, 100, dataId, group));
	}

}
