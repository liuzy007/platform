package com.alifi.mizar.common.paging;

/**
 * 分页查询常量。
 * 
 * @author lipinliang
 */
public class Constants {

    private Constants() {
        super();
    }

    /**
     * 排序查询，数据模型对象的属性名。
     */
    public static final String ORDER                    = "order";

    /**
     * 按升、降序查询（仅当设置查询排序列时有效）。
     */
    public static final String DIR                      = "dir";

    /**
     * 升序（值）。
     */
    public static final String DIR_ASC                  = "asc";

    /**
     * 降序（值）。
     */
    public static final String DIR_DESC                 = "desc";

    /**
     * 分页查询，分页起始行
     */
    public static final String PAGE_FIRST_ELEMENT_INDEX = "pageFirstElementIndex";

    /**
     * 分页查询，分页结束行
     */
    public static final String PAGE_LAST_ELEMENT_INDEX  = "pageLastElementIndex";

}
