package com.alifi.mizar.common.paging;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果数据模型。
 * 
 * @author lipinliang
 */
public class Paging<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 默认每页显示数量
     */
    public static final long PAGE_SIZE_DEFAULT = 10L;

    /**
     * 是否为第一页
     */
    private boolean isFirstPage;

    /**
     * 是否为最后一页
     */
    private boolean isLastPage;

    /**
     * 有没有前一页
     */
    private boolean hasPrevPage;

    /**
     * 有没有下一页
     */
    private boolean hasNextPage;

    /**
     * 第一页的页号
     */
    private long    firstPageNumber;

    /**
     * 最后一页的页号
     */
    private long    lastPageNumber;

    /**
     * 前一页的页号
     */
    private long    prevPageNubmer;

    /**
     * 下一页的页号
     */
    private long    nextPageNumber;

    /**
     * 此页第一个元素在整个元素中的编号
     */
    private long    pageFirstElementIndex;

    /**
     * 此页最后一个元素在整个元素中的编号
     */
    private long    pageLastElementIndex;

    /**
     * 每页显示数量
     */
    private long    pageSize;

    /**
     * 查询第几页
     */
    private long    pageNumber;

    /**
     * 此页中包含的元素
     */
    private List<T> pageElements;

    /**
     * 所有元素个数
     */
    private long    elementCount;

    /**
     * 分页查询记录数据模型构造器。
     * 
     * @param pageSize
     *            每页显示数量，默认为10条记录
     * @param pageNumber
     *            查询第几页
     * @param elementCount
     *            所有元素个数
     */
    public Paging(long pageSize, long pageNumber, long elementCount) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.elementCount = elementCount;

        if (this.pageNumber < 1) {
            this.pageNumber = 1;
        }

        if (this.pageSize < 1) {
            this.pageSize = PAGE_SIZE_DEFAULT;
        }

        if (this.elementCount < 1) {
            this.elementCount = 0;
            this.firstPageNumber = 1;
            this.lastPageNumber = 1;
            this.pageNumber = lastPageNumber;
            this.isFirstPage = true;
            this.isLastPage = true;
            this.hasNextPage = false;
            this.hasPrevPage = false;
            this.nextPageNumber = 1;
            this.prevPageNubmer = 1;
            this.pageFirstElementIndex = 0;
            this.pageLastElementIndex = 0;
        } else {
            // 第一页的页号
            this.firstPageNumber = 1;

            // 最后一页的页号
            if (this.elementCount % this.pageSize == 0) {
                this.lastPageNumber = this.elementCount / this.pageSize;
            } else {
                this.lastPageNumber = this.elementCount / this.pageSize + 1;
            }

            if (this.pageNumber > this.lastPageNumber) {
                this.pageNumber = this.lastPageNumber;
            }

            // 是否为第一页
            this.isFirstPage = this.pageNumber == this.firstPageNumber;

            // 是否为最后一页
            this.isLastPage = this.pageNumber == this.lastPageNumber;

            // 有没有下一页
            this.hasNextPage = this.pageNumber < this.lastPageNumber;

            // 有没有前一页
            this.hasPrevPage = this.pageNumber > this.firstPageNumber;

            // 下一页的页号
            this.nextPageNumber = this.isLastPage ? this.lastPageNumber : this.pageNumber + 1;

            // 前一页的页号
            this.prevPageNubmer = this.isFirstPage ? this.firstPageNumber : this.pageNumber - 1;

            // 此页第一个元素在整个元素中的编号
            this.pageFirstElementIndex = (this.pageNumber - 1) * this.pageSize + 1;

            // 最后一页是否为满页
            long fullPage = this.pageFirstElementIndex + this.pageSize - 1;
            // 此页最后一个元素在整个元素中的编号
            this.pageLastElementIndex = this.elementCount < fullPage ? this.elementCount : fullPage;
        }
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public boolean isHasPrevPage() {
        return hasPrevPage;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public long getFirstPageNumber() {
        return firstPageNumber;
    }

    public long getLastPageNumber() {
        return lastPageNumber;
    }

    public long getPrevPageNubmer() {
        return prevPageNubmer;
    }

    public long getNextPageNumber() {
        return nextPageNumber;
    }

    public long getPageFirstElementIndex() {
        return pageFirstElementIndex;
    }

    public long getPageLastElementIndex() {
        return pageLastElementIndex;
    }

    public long getPageSize() {
        return pageSize;
    }

    public long getPageNumber() {
        return pageNumber;
    }

    public long getElementCount() {
        return elementCount;
    }

    public List<T> getPageElements() {
        return pageElements;
    }

    public Paging<T> setPageElements(List<T> pageElements) {
        this.pageElements = pageElements;

        return this;
    }

}
