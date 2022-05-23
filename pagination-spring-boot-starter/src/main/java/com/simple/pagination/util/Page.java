package com.simple.pagination.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 10:35
 */
public class Page<T> implements Serializable {
    private static final long serialVersionUID = -1L;

    protected List<T> list;
    protected Integer page;
    protected Integer size;
    protected Long total;
    protected Long totalPages;
    protected Boolean next;
    protected Boolean previous;

    public Page() {
    }

    public Page(List<T> list, Integer page, Integer size, Long total, Long totalPages, Boolean next, Boolean previous) {
        this.list = list;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
        this.next = next;
        this.previous = previous;
    }

    public Page(List<T> list, Integer page, Integer size) {
        this.list = list;
        this.page = page;
        this.size = size;
    }

    public Page(List<T> list, Long total) {
        this.list = list;
        this.total = total;
    }

    public Page(List<T> list) {
        this.list = list;
    }

    public static <T> Page<T> of(List<T> list, Integer page, Integer size, Long total, Long totalPages, Boolean next, Boolean previous) {
        return new Page<T>(list, page, size, total, totalPages, next, previous);
    }

    public static <T> Page<T> of(List<T> list, Integer page, Integer size, Long total) {
        if (page != null && size != null) {
            if (total == null) {
                return new Page<T>(list, page, size);
            } else {
                boolean next;
                boolean previous;
                long totalPages;
                if (page > 0 && size > 0) {
                    // 是否存在下一页
                    next = page.longValue() * (long) size < total;
                    previous = page != 1;
                    // 总页数
                    totalPages = total % (long) size > 0L ? total / (long) size + 1L : total / (long) size;
                } else {
                    next = false;
                    previous = false;
                    totalPages = 0L;
                }
                return new Page<T>(list, page, size, total, totalPages, next, previous);
            }
        } else {
            return new Page<T>(list, total);
        }
    }

    public static <T> Page<T> of(List<T> list, Integer page, Integer size) {
        return new Page<T>(list, page, size);
    }

    public static <T> Page<T> of(List<T> list, Long total) {
        return new Page<T>(list, total);
    }

    public static <T> Page<T> wrap(Collection<T> collection) {
        return new Page<T>(toList(collection));
    }

    public static <T> Page<T> wrap(List<T> list) {
        return new Page<T>(list);
    }

    public static <T> Page<T> empty() {
        return new Page<T>(new ArrayList<T>());
    }

    public static <T> Page<T> of(PaginationCollection<T> source) {
        return source == null ? (Page<T>) empty() : of(toList(source), source.getPage(), source.getSize(), source.getTotal());
    }

    public static <T> Page<T> of(Collection<T> source) {
        if (source == null) {
            return empty();
        } else {
            return source instanceof PaginationCollection ? (Page<T>) of((PaginationCollection) source) : wrap(toList(source));
        }
    }

    public static <T> Page<T> of(PaginationList<T> source) {
        return source == null ? (Page<T>) empty() : of(source, source.getPage(), source.getSize(), source.getTotal());
    }

    public static <T> Page<T> of(List<T> source) {
        if (source == null) {
            return empty();
        } else {
            return source instanceof PaginationList ? (Page<T>) of((PaginationList) source) : wrap(source);
        }
    }

    public static <T, S> Page<T> of(PaginationCollection<S> source, Function<? super S, ? extends T> mapper) {
        return source == null ? empty() : of(mapping(source, mapper), source.getPage(), source.getSize(), source.getTotal());
    }

    public static <T, S> Page<T> of(Collection<S> source, Function<? super S, ? extends T> mapper) {
        if (source == null) {
            return empty();
        } else {
            return source instanceof PaginationCollection ? of((PaginationCollection) source, mapper) : wrap(mapping(source, mapper));
        }
    }

    public static <T, S> Page<T> transfer(PaginationCollection<S> source, Collection<T> target) {
        return source != null && target != null ? of(toList(target), source.getPage(), source.getSize(), source.getTotal()) : empty();
    }

    public static <T, S> Page<T> transfer(Collection<S> source, Collection<T> target) {
        if (source != null && target != null) {
            return source instanceof PaginationCollection ? transfer((PaginationCollection) source, target) : wrap(toList(target));
        } else {
            return empty();
        }
    }

    private static <T, S> List<T> mapping(Collection<S> source, Function<? super S, ? extends T> mapper) {
        Objects.requireNonNull(mapper);
        return source != null && !source.isEmpty() ? (List) source.stream().map(mapper).collect(Collectors.toList()) : new ArrayList();
    }


    private static <T> List<T> toList(Collection<T> collection) {
        if (collection == null) {
            return new ArrayList<T>();
        } else {
            return (collection instanceof List ? (List) collection : new ArrayList(collection));
        }
    }

    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        return new Page<U>(this.list == null ? null : (List) this.list.stream().map(converter).collect(Collectors.toList()), this.page, this.size, this.total, this.totalPages, this.next, this.previous);
    }

    public boolean isEmpty() {
        return this.list == null || this.list.isEmpty();
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Long totalPages) {
        this.totalPages = totalPages;
    }

    public Boolean getNext() {
        return next;
    }

    public void setNext(Boolean next) {
        this.next = next;
    }

    public Boolean getPrevious() {
        return previous;
    }

    public void setPrevious(Boolean previous) {
        this.previous = previous;
    }
}
