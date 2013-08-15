package com.alifi.mizar.service;

import java.util.Map;


public interface B2BService {

    /**
     * 接收B2B投诉信息
     * @param queryString
     * @return
     */
    public String receiveComplaint(Map<String, String[]> queryBody);
}