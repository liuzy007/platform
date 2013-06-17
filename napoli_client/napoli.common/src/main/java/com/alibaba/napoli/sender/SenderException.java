package com.alibaba.napoli.sender;

/**
 * User: heyman
 * Date: 11/24/11
 * Time: 3:58 下午
 */
public class SenderException extends RuntimeException{
    private static final long serialVersionUID = -4596059237992273913L;
    private short type;

    /**
     * Creates a new instance.
     */
    public SenderException() {
        super();
    }

    /**
     * Creates a new instance.
     */
    public SenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public SenderException(short type,String message, Throwable cause) {
        this(message, cause);
        this.type = type;
    }

    /**
     * Creates a new instance.
     */
    public SenderException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public SenderException(Throwable cause) {
        super(cause);
    }

    public short getType() {
        return type;
    }
}
