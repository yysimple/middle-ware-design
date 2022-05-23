package com.simple.pagination.dialect;

import com.simple.pagination.util.PaginationParam;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 18:04
 */
public class OracleDialect extends AbstractDialect {

    @Override
    public String getPagingSql(String sql) {
        return "SELECT *" +
                " FROM (SELECT " + TEMP_TABLE_ALIAS + ".*, ROWNUM AS " + ROW_NUMBER_ALIAS + " FROM (" + sql + ") " + TEMP_TABLE_ALIAS + " WHERE ROWNUM <= ?) " + PAGING_TABLE_ALIAS +
                " WHERE " + ROW_NUMBER_ALIAS + " > ?";
    }

    @Override
    public Object[] getPagingParams(PaginationParam param) {
        return new Object[]{
                param.getOffset() + param.getRows(),
                param.getOffset()};
    }
}
