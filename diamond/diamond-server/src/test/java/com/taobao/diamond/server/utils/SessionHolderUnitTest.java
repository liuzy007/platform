package com.taobao.diamond.server.utils;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;


public class SessionHolderUnitTest {
    @Test
    public void testSetSessionGetSession() {
        Assert.assertNull(SessionHolder.getSession());
        MockHttpSession session = new MockHttpSession();
        SessionHolder.setSession(session);
        Assert.assertSame(session, SessionHolder.getSession());
        SessionHolder.setSession(null);
        Assert.assertNull(SessionHolder.getSession());
    }
}
