package com.alibaba.napoli.common;

/**
 * User: heyman
 * Date: 11/24/11
 * Time: 3:35 下午
 */
public class NapoliException extends Exception {
    private static final long serialVersionUID = 2908618315971075004L;

    /**
     * Creates a new exception.
     */
    public NapoliException() {
        super();
    }

    /**
     * Creates a new exception.
     */
    public NapoliException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception.
     */
    public NapoliException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     */
    public NapoliException(Throwable cause) {
        super(cause);
    }
}
