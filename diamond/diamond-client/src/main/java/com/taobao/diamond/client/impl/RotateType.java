/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.client.impl;


/**
 * 轮询类型, 有轮询本地配置, 轮询diamond server配置, 轮询本地snapshot配置三种类型
 * @author leiwen.zh
 *
 */
enum RotateType {

    LOCAL, SERVER, SNAPSHOT;
}
