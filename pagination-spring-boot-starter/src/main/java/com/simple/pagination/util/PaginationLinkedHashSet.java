package com.simple.pagination.util;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 15:02
 */
public class PaginationLinkedHashSet<E> extends LinkedHashSet<E> implements PaginationSet<E> {

    private static final long serialVersionUID = -1L;
    private Long total;
    private Integer page;
    private Integer size;

    public PaginationLinkedHashSet() {
    }

    public PaginationLinkedHashSet(Long total, Integer page, Integer size) {
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public PaginationLinkedHashSet(Collection<? extends E> c, Long total, Integer page, Integer size) {
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
