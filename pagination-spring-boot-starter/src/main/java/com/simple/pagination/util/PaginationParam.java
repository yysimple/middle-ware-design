package com.simple.pagination.util;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 16:20
 */
public class PaginationParam {
    private static final PaginationParam NON = new PaginationParam(false);
    protected Integer page;
    protected Integer size;
    protected Integer offset;
    protected Integer rows;
    protected String countExpr;
    protected String countMethodName;
    protected final boolean pageable;

    public PaginationParam(boolean pageable) {
        this.pageable = pageable;
    }

    public PaginationParam(Integer page, Integer size, Integer offset, Integer rows, String countExpr, String countMethodName, boolean pageable) {
        this.page = page;
        this.size = size;
        this.offset = offset;
        this.rows = rows;
        this.countExpr = countExpr;
        this.countMethodName = countMethodName;
        this.pageable = pageable;
    }

    public static PaginationParam nonPagination() {
        return NON;
    }

    public boolean isPageable() {
        return this.pageable;
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

    public Integer getOffset() {
        return this.offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getRows() {
        return this.rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public String getCountExpr() {
        return this.countExpr;
    }

    public void setCountExpr(String countExpr) {
        this.countExpr = countExpr;
    }

    public String getCountMethodName() {
        return this.countMethodName;
    }

    public void setCountMethodName(String countMethodName) {
        this.countMethodName = countMethodName;
    }
}
