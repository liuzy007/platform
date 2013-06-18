package com.taobao.diamond.server.listener;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;


public class AuthorizationFilterUnitTest {
    @Test
    public void testDoFilter_认证通过() throws Exception {
        AuthorizationFilter filter = new AuthorizationFilter();
        IMocksControl mocksControl = EasyMock.createControl();

        HttpServletRequest request = mocksControl.createMock(HttpServletRequest.class);
        HttpServletResponse response = mocksControl.createMock(HttpServletResponse.class);
        HttpSession session = mocksControl.createMock(HttpSession.class);
        FilterChain chain = mocksControl.createMock(FilterChain.class);

        EasyMock.expect(request.getSession()).andReturn(session).once();
        EasyMock.expect(session.getAttribute("user")).andReturn("admin").once();
        chain.doFilter(request, response);
        EasyMock.expectLastCall();
        mocksControl.replay();

        filter.doFilter(request, response, chain);

        mocksControl.verify();
    }


    @Test
    public void testDoFilter_认证失败() throws Exception {
        AuthorizationFilter filter = new AuthorizationFilter();
        IMocksControl mocksControl = EasyMock.createControl();

        HttpServletRequest request = mocksControl.createMock(HttpServletRequest.class);
        HttpServletResponse response = mocksControl.createMock(HttpServletResponse.class);
        HttpSession session = mocksControl.createMock(HttpSession.class);
        FilterChain chain = mocksControl.createMock(FilterChain.class);

        EasyMock.expect(request.getSession()).andReturn(session).once();
        String path = "/hello";
        EasyMock.expect(request.getContextPath()).andReturn(path).once();
        EasyMock.expect(session.getAttribute("user")).andReturn(null).once();
        response.sendRedirect("/hello/jsp/login.jsp");
        EasyMock.expectLastCall();
        mocksControl.replay();

        filter.doFilter(request, response, chain);

        mocksControl.verify();
    }
}
