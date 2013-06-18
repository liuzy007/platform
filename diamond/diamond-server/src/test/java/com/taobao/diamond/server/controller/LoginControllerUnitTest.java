package com.taobao.diamond.server.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;

import com.taobao.diamond.server.service.AdminService;


public class LoginControllerUnitTest {

    private LoginController loginController;
    private AdminService adminService;


    @Before
    public void setUp() throws Exception {
        this.loginController = new LoginController();
        adminService = new AdminService();
        this.loginController.setAdminService(adminService);
    }


    @Test
    public void testLoginSuccess() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ModelMap modelMap = new ModelMap();
        assertEquals("admin/admin", this.loginController.login(request, "admin", "admin", modelMap));
        assertEquals("admin", request.getSession().getAttribute("user"));

    }


    @Test
    public void testLoginFail() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ModelMap modelMap = new ModelMap();
        assertEquals("login", this.loginController.login(request, "boyan", "boyan", modelMap));
        assertNull(request.getSession().getAttribute("user"));
        assertEquals("µ«¬º ß∞‹£¨”√ªß√˚√‹¬Î≤ª∆•≈‰", modelMap.get("message"));

    }


    @Test
    public void testLoginSuccess_OtherUser() {
        this.adminService.addUser("boyan", "boyan");
        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            ModelMap modelMap = new ModelMap();
            assertEquals("admin/admin", this.loginController.login(request, "boyan", "boyan", modelMap));
            assertEquals("boyan", request.getSession().getAttribute("user"));
        }
        finally {
            this.adminService.removeUser("boyan");
        }

    }


    @Test
    public void testLogout() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("user", "admin");
        assertEquals("login", this.loginController.logout(request));
        assertNull(request.getSession().getAttribute("user"));

    }

}
