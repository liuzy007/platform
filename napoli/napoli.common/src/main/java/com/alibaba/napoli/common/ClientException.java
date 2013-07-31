package com.alibaba.napoli.common;

/**
 * User: heyman
 * Date: 12/9/11
 * Time: 4:11 下午
 */
public class ClientException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String errorCode;

    private Exception linkedException;

    public ClientException(String reason, String errorCode) {
        super(reason);
        this.errorCode = errorCode;
        linkedException = null;
    }

    public ClientException(String reason, Throwable t) {
        super(reason, t);
    }

    public ClientException(String reason) {
        super(reason, null);
        linkedException = null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Exception getLinkedException() {
        return linkedException;
    }

    /*
     * Although not specified in the JavaDoc, this method need to be
     * declared synchronized to make the serialUID the same as for
     * the RI. However, given that the setter is not synchronized
     * this may be problematic on some platforms.
     */
    public synchronized void setLinkedException(Exception ex) {
        linkedException = ex;
    }
}
