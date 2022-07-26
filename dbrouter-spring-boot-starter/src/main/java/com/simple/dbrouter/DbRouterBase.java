package com.simple.dbrouter;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-30 10:17
 **/
public class DbRouterBase {

    private String tbIdx;

    public String getTbIdx() {
        return DbContextHolder.getTBKey();
    }

}
