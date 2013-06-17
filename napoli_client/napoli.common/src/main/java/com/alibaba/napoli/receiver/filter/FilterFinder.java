package com.alibaba.napoli.receiver.filter;


public interface FilterFinder {
    /**
     * 取得当前filter的下一个filter，用于Filter链式反应
     * 
     * @param filter 当前使用的filter
     * @return 下一个filter，可能为空
     */
    Filter nextFilter(Filter filter);
}
