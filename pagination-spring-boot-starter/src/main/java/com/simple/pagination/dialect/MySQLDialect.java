package com.simple.pagination.dialect;

import com.simple.pagination.util.PaginationParam;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 17:42
 */
public class MySQLDialect extends AbstractDialect {
    @Override
    public String getPagingSql(String sql) {
        return sql + " LIMIT ?, ?";
    }

    @Override
    public Object[] getPagingParams(PaginationParam param) {
        return new Object[]{
                param.getOffset(),
                param.getRows()};
    }
}
