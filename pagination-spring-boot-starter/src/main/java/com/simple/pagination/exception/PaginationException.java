package com.simple.pagination.exception;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 16:11
 */
public class PaginationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PaginationException() {
    }

    public PaginationException(String message) {
        super(message);
    }

    public PaginationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaginationException(Throwable cause) {
        super(cause);
    }
}
