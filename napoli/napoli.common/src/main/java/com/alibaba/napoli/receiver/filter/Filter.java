package com.alibaba.napoli.receiver.filter;

/**
 * 使用这个接口在消息进入和离开mq的时候对消息进行预处理，提供给应用一种无缝的方法对消息做过滤，转换等高级操作。
 * 
 * @author guolin.zhuanggl 2009-7-16 下午07:35:09
 */
public interface Filter {

    /**
     * 初始化Filter，在sender或者receiver初始化时调用。
     * 
     */
    void init();

    /**
     * 对消息进行预处理，处理后的对象设置到context中。
     * 
     * @param context
     * @param next
     */
    void filter(Context context, FilterFinder next);

    /**
     * 销毁Filter，在Sender或者Receiver关闭时调用。
     * 
     */
    void destroy();
}
