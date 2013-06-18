package com.taobao.diamond.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;


public class AdminServiceUnitTest {
    private AdminService adminService;


    @Before
    public void setUp() {
        this.adminService = new AdminService();
    }


    @Test
    public void testLogin() {
        assertFalse(this.adminService.login("test", "test"));
        assertFalse(this.adminService.login("admin ", "admin"));
        assertTrue(this.adminService.login("admin", "admin"));
    }


    @Test
    public void testAddUser() throws Exception {
        assertFalse(this.adminService.addUser("admin", "test"));

        String userName = "hello";
        String password = "world";
        assertTrue(this.adminService.addUser(userName, password));

        URL url = this.adminService.getUrl();
        FileInputStream in = new FileInputStream(url.getPath());
        Properties props = new Properties();
        props.load(in);
        in.close();
        // 确认存入文件
        assertTrue(props.containsKey(userName));
        assertEquals(password, props.get(userName));

        assertTrue(this.adminService.login(userName, password));
        this.adminService.removeUser(userName);

    }


    @Test
    public void testSetPassword() throws Exception {
        assertFalse(this.adminService.updatePassword("hello", "world"));
        this.adminService.addUser("hello", "hello");
        assertTrue(this.adminService.login("hello", "hello"));
        assertFalse(this.adminService.login("hello", "world"));

        assertTrue(this.adminService.updatePassword("hello", "world"));

        URL url = this.adminService.getUrl();
        FileInputStream in = new FileInputStream(url.getPath());
        Properties props = new Properties();
        props.load(in);
        in.close();

        assertEquals("world", props.get("hello"));
        assertTrue(this.adminService.login("hello", "world"));
        this.adminService.removeUser("hello");
    }


    @Test
    public void testRemoveUser() throws Exception {
        assertTrue(this.adminService.addUser("hello", "world"));
        assertTrue(this.adminService.login("hello", "world"));

        this.adminService.removeUser("hello");
        assertFalse(this.adminService.login("hello", "world"));

        URL url = this.adminService.getUrl();
        FileInputStream in = new FileInputStream(url.getPath());
        Properties props = new Properties();
        props.load(in);
        in.close();
        assertNull(props.get("hello"));
        
        //最后一个不能删除
        assertFalse(this.adminService.removeUser("admin"));
    }

}
