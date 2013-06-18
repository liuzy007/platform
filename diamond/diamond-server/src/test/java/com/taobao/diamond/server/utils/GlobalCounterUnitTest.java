package com.taobao.diamond.server.utils;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class GlobalCounterUnitTest {

    @Before
    public void setUp() {
        GlobalCounter.getCounter().set(0);
    }


    @After
    public void tearDown() {
        GlobalCounter.getCounter().set(0);
    }


    @Test
    public void testGetSet() {
        assertEquals(0, GlobalCounter.getCounter().get());
        GlobalCounter.getCounter().set(100);
        assertEquals(100, GlobalCounter.getCounter().get());

    }


    @Test
    public void testDecrementAndGet() {
        assertEquals(-1, GlobalCounter.getCounter().decrementAndGet());
        GlobalCounter.getCounter().set(99);
        for (int i = 0; i < 100; i++) {
            assertEquals(99 - i - 1, GlobalCounter.getCounter().decrementAndGet());
        }
    }

}
