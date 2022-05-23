package com.simple.pagination.dialect;

import com.simple.pagination.util.PaginationParam;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 16:20
 */
public interface Dialect {
    String getPagingSql(String var1);

    String getPagingSql(String var1, PaginationParam var2);

    String getCountSql(String var1);

    String getCountSql(String var1, String var2);

    Object assembleParameter(MappedStatement var1, Object var2, BoundSql var3, CacheKey var4, PaginationParam var5) throws Exception;
}
