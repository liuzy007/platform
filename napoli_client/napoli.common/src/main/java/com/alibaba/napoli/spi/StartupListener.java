package com.alibaba.napoli.spi;

import com.alibaba.napoli.client.NapoliClient;

/**
 * User: heyman
 * Date: 5/22/12
 * Time: 4:24 下午
 */
public interface StartupListener {
public void startup(NapoliClient napoliClient);
}
