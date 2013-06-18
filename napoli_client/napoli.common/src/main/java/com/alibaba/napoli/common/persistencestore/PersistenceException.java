package com.alibaba.napoli.common.persistencestore;

/**
 * User: heyman
 * Date: 12/21/11
 * Time: 1:49 下午
 */
public class PersistenceException extends RuntimeException{
    private static final long serialVersionUID = -4596059237992273913L;
    private short type;

    /**
     * Creates a new instance.
     */
    public PersistenceException() {
        super();
    }

    /**
     * Creates a new instance.
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(short type,String message, Throwable cause) {
        this(message, cause);
        this.type = type;
    }

    /**
     * Creates a new instance.
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public PersistenceException(Throwable cause) {
        super(cause);
    }

    public short getType() {
        return type;
    }
}
