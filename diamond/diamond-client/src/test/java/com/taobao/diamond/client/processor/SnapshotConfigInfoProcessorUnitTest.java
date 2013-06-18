package com.taobao.diamond.client.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;


/**
 * 
 * @author boyan
 * 
 */
public class SnapshotConfigInfoProcessorUnitTest {
    private SnapshotConfigInfoProcessor processor;


    @Before
    public void setUp() throws Exception {
        File tmpFile = File.createTempFile("test", "tmp" + System.currentTimeMillis());
        String path = tmpFile.getParent();
        String dirPath = path + "/snapshot";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        deleteDir(dir);
        tmpFile.delete();
        processor = new SnapshotConfigInfoProcessor(dirPath);

    }


    private void deleteDir(File dir) {
        if (dir.listFiles() != null) {
            for (File f : dir.listFiles()) {
                if (f.isFile())
                    f.delete();
                else if (f.isDirectory()) {
                    deleteDir(f);
                    f.delete();
                }
            }
        }
    }


    @Test
    public void testSaveSnapshotGetDelete() throws Exception {
        final String dataId = "test-dataId";
        final String group = "test";
        assertNull(this.processor.getConfigInfomation(dataId, group));
        this.processor.saveSnaptshot(dataId, group, "hello world\r\n");
        assertEquals("hello world\r\n", this.processor.getConfigInfomation(dataId, group));
        this.processor.removeSnapshot(dataId, group);
        assertNull(this.processor.getConfigInfomation(dataId, group));
    }


    @Test
    public void testSaveSnapshotGetDelete_中文_LargeData() throws Exception {
        final String dataId = "test-dataId";
        final String group = "test";
        assertNull(this.processor.getConfigInfomation(dataId, group));
        String bigString = getBigString();
        this.processor.saveSnaptshot(dataId, group, bigString);
        assertEquals(bigString, this.processor.getConfigInfomation(dataId, group));
        this.processor.removeSnapshot(dataId, group);
        assertNull(this.processor.getConfigInfomation(dataId, group));
    }


    public String getBigString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8 * 1024; i++) {
            sb.append("中文");
            if (i % 1000 == 0)
                sb.append("\r\n");
        }
        return sb.toString();
    }


    @Test
    public void testGetBlankDataIdBlankGroup() throws Exception {
        assertNull(this.processor.getConfigInfomation(null, "test"));
        assertNull(this.processor.getConfigInfomation("", "test"));
        assertNull(this.processor.getConfigInfomation("mytest", null));
        assertNull(this.processor.getConfigInfomation("mytest", ""));
    }


    @Test(expected = IllegalArgumentException.class)
    public void saveBlankDataId() throws IOException {
        this.processor.saveSnaptshot(null, "test", "good");
    }


    @Test(expected = IllegalArgumentException.class)
    public void saveBlankGroup() throws IOException {
        this.processor.saveSnaptshot("mytest", "", "good");
    }


    @Test
    public void saveBlankConfig() throws IOException {
        final String dataId = "test-dataId";
        final String group = "test";
        assertNull(this.processor.getConfigInfomation(dataId, group));
        this.processor.saveSnaptshot(dataId, group, "");
        assertEquals("", this.processor.getConfigInfomation(dataId, group));
    }

}
