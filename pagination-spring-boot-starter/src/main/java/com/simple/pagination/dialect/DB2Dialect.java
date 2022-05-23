package com.simple.pagination.dialect;

import com.simple.pagination.util.PaginationParam;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 17:46
 */
public class DB2Dialect extends AbstractDialect {

    @Override
    public String getPagingSql(String sql) {
        return "SELECT *" +
                " FROM (SELECT " + TEMP_TABLE_ALIAS + ".*, ROW_NUMBER() OVER() AS " + ROW_NUMBER_ALIAS + " FROM (" + sql + ") " + TEMP_TABLE_ALIAS + ") " + PAGING_TABLE_ALIAS +
                " WHERE " + ROW_NUMBER_ALIAS + " BETWEEN ? AND ?";
    }

    @Override
    public Object[] getPagingParams(PaginationParam param) {
        return new Object[]{
                param.getOffset() + 1,
                param.getOffset() + param.getRows()};
    }
}
