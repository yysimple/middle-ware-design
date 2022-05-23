package com.simple.pagination.dialect;

import com.simple.pagination.util.PaginationParam;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 18:03
 */
public class InformixDialect extends AbstractDialect {
    @Override
    public String getPagingSql(String sql) {
        return "SELECT SKIP ? FIRST ? * FROM (" + sql + ")";
    }

    @Override
    public Object[] getPagingParams(PaginationParam param) {
        return new Object[]{
                param.getOffset(),
                param.getRows()};
    }
}
