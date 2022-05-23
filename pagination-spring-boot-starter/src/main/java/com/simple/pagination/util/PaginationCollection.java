package com.simple.pagination.util;

import java.util.Collection;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 14:41
 */
public interface PaginationCollection<E> extends Collection<E> {

    Long getTotal();

    Integer getPage();

    Integer getSize();
}
