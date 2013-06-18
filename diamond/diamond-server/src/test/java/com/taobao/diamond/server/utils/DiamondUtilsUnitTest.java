package com.taobao.diamond.server.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class DiamondUtilsUnitTest {
    @Test
    public void testHasInvalidChar() {
        assertFalse(DiamondUtils.hasInvalidChar("notify!."));
        assertFalse(DiamondUtils.hasInvalidChar("notify"));
        assertTrue(DiamondUtils.hasInvalidChar("hello world"));
        assertTrue(DiamondUtils.hasInvalidChar("notify*"));
        assertTrue(DiamondUtils.hasInvalidChar(null));
        assertTrue(DiamondUtils.hasInvalidChar(""));
        //assertTrue(DiamondUtils.hasInvalidChar(":notify"));
        for (char ch : DiamondUtils.INVALID_CHAR) {
            assertTrue(DiamondUtils.hasInvalidChar("notify" + ch));
        }
    }

}
