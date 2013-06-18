package com.alibaba.napoli;

import javax.jms.JMSException;

/**
 * User: heyman
 * Date: 1/30/12
 * Time: 11:08 上午
 */
public interface PerfCase {
    void setup() throws Exception;
    void excute() throws Exception;
    void close();
}
