package com.taobao.diamond.mockserver;

import org.junit.Assert;
import org.junit.Test;


public class MockServerUnitTest {

    @Test
    public void testTestFlag() {
        Assert.assertFalse(MockServer.isTestMode());
        
        MockServer.setUpMockServer();
        
        Assert.assertTrue(MockServer.isTestMode());
        
        MockServer.tearDownMockServer();
        
        Assert.assertFalse(MockServer.isTestMode());
    }
}
