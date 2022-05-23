package com.simple.pagination.exception;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 16:12
 */
public class SqlDialectException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SqlDialectException() {
    }

    public SqlDialectException(String message) {
        super(message);
    }

    public SqlDialectException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlDialectException(Throwable cause) {
        super(cause);
    }
}
