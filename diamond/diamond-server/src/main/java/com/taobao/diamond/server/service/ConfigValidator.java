/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.service;

/**
 * 配置的语义校验器
 * 
 * @author boyan
 * @date 2010-5-11
 */
public interface ConfigValidator {
    /**
     * 校验配置是否正确
     * 
     * @param dataId
     * @param group
     * @param content
     * @return
     */
    public boolean validate(String dataId, String group, String content);
}
