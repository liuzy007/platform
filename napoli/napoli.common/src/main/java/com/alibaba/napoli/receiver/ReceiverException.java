package com.alibaba.napoli.receiver;

/**
 * User: heyman
 * Date: 12/9/11
 * Time: 5:21 下午
 */
public class ReceiverException extends RuntimeException {
    private static final long serialVersionUID = -4596059237992273913L;
    private short type;

    /**
     * Creates a new instance.
     */
    public ReceiverException() {
        super();
    }

    /**
     * Creates a new instance.
     */
    public ReceiverException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReceiverException(short type, String message, Throwable cause) {
        this(message, cause);
        this.type = type;
    }

    /**
     * Creates a new instance.
     */
    public ReceiverException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public ReceiverException(Throwable cause) {
        super(cause);
    }

    public short getType() {
        return type;
    }
}
