package com.taobao.diamond.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletContext;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.ConfigInfo;



public class DiskServiceUnitTest {

    private DiskService diskService;

    private ServletContext servletContext;

    private IMocksControl mocksControl;

    private File tempFile;

    private String path;


    @Before
    public void setUp() throws IOException {
        mocksControl = EasyMock.createControl();
        servletContext = mocksControl.createMock(ServletContext.class);
        tempFile = File.createTempFile("diskServiceTest", "tmp");
        path = tempFile.getParent();
        this.diskService = new DiskService();
        this.diskService.setServletContext(servletContext);
    }


    @Test
    public void testSaveToDisk() throws IOException {
        String dataId = "diskServiceTest-dataId";
        String group = "iskServiceTest-group";
        String content = "hello world\n";
        ConfigInfo info = new ConfigInfo(dataId, group, content);
        EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR))
            .andReturn(path + "/" + Constants.BASE_DIR).anyTimes();
        EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR + "/" + group)).andReturn(
            path + "/" + Constants.BASE_DIR + "/" + group).anyTimes();
        String dataPath = path + "/" + Constants.BASE_DIR + "/" + group + "/" + dataId;
        EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR + "/" + group + "/" + dataId)).andReturn(
            dataPath).anyTimes();
        mocksControl.replay();

        this.diskService.saveToDisk(info);
        // 检验文件内容是否正确
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataPath)));
        String line = reader.readLine();
        assertNotNull(line);
        assertEquals("hello world", line);
        reader.close();
        mocksControl.verify();

        // 最后删除
        this.diskService.removeConfigInfo(dataId, group);
    }


    @Test
    public void testIsModified() throws Exception {
        String dataId = "diskServiceTest-dataId";
        String group = "iskServiceTest-group";
        String content = "hello world\n";
        ConfigInfo info = new ConfigInfo(dataId, group, content);
        EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR))
            .andReturn(path + "/" + Constants.BASE_DIR).anyTimes();
        EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR + "/" + group)).andReturn(
            path + "/" + Constants.BASE_DIR + "/" + group).anyTimes();
        String dataPath = path + "/" + Constants.BASE_DIR + "/" + group + "/" + dataId;
        EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR + "/" + group + "/" + dataId)).andReturn(
            dataPath).anyTimes();
        mocksControl.replay();

        assertFalse(this.diskService.isModified(dataId, group));
        this.diskService.saveToDisk(info);
        assertFalse(this.diskService.isModified(dataId, group));
        this.diskService.removeConfigInfo(info);

    }


    @Test
    public void testRemoveConfigInfo1() throws IOException {
        String dataId = "diskServiceTest-dataId";
        String group = "iskServiceTest-group";
        String content = "hello world\n";
        ConfigInfo info = new ConfigInfo(dataId, group, content);
        EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR))
            .andReturn(path + "/" + Constants.BASE_DIR).anyTimes();
        EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR + "/" + group)).andReturn(
            path + "/" + Constants.BASE_DIR + "/" + group).anyTimes();
        String dataPath = path + "/" + Constants.BASE_DIR + "/" + group + "/" + dataId;
        EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR + "/" + group + "/" + dataId)).andReturn(
            dataPath).anyTimes();
        mocksControl.replay();

        this.diskService.saveToDisk(info);
        File file = new File(dataPath);
        assertTrue(file.exists());
        // 删除
        this.diskService.removeConfigInfo(dataId, group);
        // 确认文件已经被删除
        file = new File(dataPath);
        assertFalse(file.exists());

    }


    @Test
    public void testRemoveConfigInfo2() throws IOException {
        String dataId = "diskServiceTest-dataId";
        String group = "iskServiceTest-group";
        String content = "hello world\n";
        ConfigInfo info = new ConfigInfo(dataId, group, content);
        EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR))
            .andReturn(path + "/" + Constants.BASE_DIR).anyTimes();
        EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR + "/" + group)).andReturn(
            path + "/" + Constants.BASE_DIR + "/" + group).anyTimes();
        String dataPath = path + "/" + Constants.BASE_DIR + "/" + group + "/" + dataId;
        EasyMock.expect(servletContext.getRealPath("/" + Constants.BASE_DIR + "/" + group + "/" + dataId)).andReturn(
            dataPath).anyTimes();
        mocksControl.replay();

        this.diskService.saveToDisk(info);
        File file = new File(dataPath);
        assertTrue(file.exists());
        // 删除
        this.diskService.removeConfigInfo(info);
        // 确认文件已经被删除
        file = new File(dataPath);
        assertFalse(file.exists());

    }


    @After
    public void tearDown() throws IOException {
        tempFile.delete();
    }
}
