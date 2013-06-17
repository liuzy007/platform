package com.alibaba.napoli;

import com.alibaba.napoli.common.util.ExtensionLoader;
import com.alibaba.napoli.sender.NapoliSender;
import com.alibaba.napoli.sender.impl.SenderMq;
import com.alibaba.napoli.spi.TransportSender;

import java.util.Map;

import org.junit.Test;
import static  org.junit.Assert.*;

/**
 * User: heyman Date: 2/17/12 Time: 2:40 下午
 */
public class ExtensionTest {
    @Test
    public void testExtension() {
        Map<String, TransportSender> map = ExtensionLoader.getExtensionLoader(TransportSender.class);
        System.out.println(map);
        assertEquals(2,map.size());
        NapoliSender napoliSender = ExtensionLoader.buildSenderFilterChain(new String[]{"monitor", "localStore"},new SenderMq());
        //System.out.println(napoliSender);
    }
}
