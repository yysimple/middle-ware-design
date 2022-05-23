package com.simple.pagination.exception;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 16:09
 */
public class IllegalSqlException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IllegalSqlException() {
    }

    public IllegalSqlException(String message) {
        super(message);
    }

    public IllegalSqlException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalSqlException(Throwable cause) {
        super(cause);
    }

}
