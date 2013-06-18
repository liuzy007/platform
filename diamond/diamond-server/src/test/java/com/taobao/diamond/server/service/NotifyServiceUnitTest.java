package com.taobao.diamond.server.service;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class NotifyServiceUnitTest {

    private NotifyService notifyService;


    @Before
    public void setUp() {
        this.notifyService = new NotifyService();
    }


    @Test
    public void testGenerateNotifyGroupChangedPath_正常情况() {
        String address = "localhost:8080";
        assertEquals("http://localhost:8080/diamond-server/notify.do?method=notifyGroup", this.notifyService
            .generateNotifyGroupChangedPath(address));

        address = "192.168.207.101";
        assertEquals("http://192.168.207.101/diamond-server/notify.do?method=notifyGroup", this.notifyService
            .generateNotifyGroupChangedPath(address));
    }


    @Test
    public void testGenerateNotifyGroupChangedPath_自定义URL() {
        String address = "192.168.207.101";
        this.notifyService.getNodeProperties().put(address,
            "http://192.168.207.101/diamond/change.do");
        assertEquals("http://192.168.207.101/diamond/change.do?method=notifyGroup", this.notifyService
            .generateNotifyGroupChangedPath(address));
    }


    @Test
    public void testGenerateNotifyConfigInfoPath_正常情况() {
        final String dataId = "test-data";
        final String group = "group";
        String address = "localhost:8080";
        assertEquals(
            "http://localhost:8080/diamond-server/notify.do?method=notifyConfigInfo&dataId=test-data&group=group",
            this.notifyService.generateNotifyConfigInfoPath(dataId, group, address));

        address = "192.168.207.101";
        assertEquals(
            "http://192.168.207.101/diamond-server/notify.do?method=notifyConfigInfo&dataId=test-data&group=group",
            this.notifyService.generateNotifyConfigInfoPath(dataId, group, address));

    }


    @Test
    public void testGenerateNotifyConfigInfoPath_自定义URL() {
        final String address = "localhost:8080";
        this.notifyService.getNodeProperties().put(address,
            "http://192.168.207.101:8080/diamond/change.do");
        final String dataId = "test-data";
        final String group = "group";

        assertEquals(
            "http://192.168.207.101:8080/diamond/change.do?method=notifyConfigInfo&dataId=test-data&group=group",
            this.notifyService.generateNotifyConfigInfoPath(dataId, group, address));
    }

}
