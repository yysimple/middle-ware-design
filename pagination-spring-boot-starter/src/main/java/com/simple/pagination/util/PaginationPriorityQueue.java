package com.simple.pagination.util;

import java.util.Collection;
import java.util.PriorityQueue;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 15:07
 */
public class PaginationPriorityQueue<E> extends PriorityQueue<E> implements PaginationQueue<E> {

    private static final long serialVersionUID = -1L;
    private Long total;
    private Integer page;
    private Integer size;

    public PaginationPriorityQueue() {
    }

    public PaginationPriorityQueue(Long total, Integer page, Integer size) {
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public PaginationPriorityQueue(Collection<? extends E> c, Long total, Integer page, Integer size) {
        super(c);
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public Long getTotal() {
        return this.total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPage() {
        return this.page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return this.size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
