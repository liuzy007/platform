package com.alibaba.napoli.mqImpl;

import com.alibaba.napoli.common.NapoliException;

/**
 * User: heyman
 * Date: 12/1/11
 * Time: 11:46 上午
 */
public class TransportException extends NapoliException {


    private static final long serialVersionUID = 4538787249715613739L;
    public static final short HALF_STAGE = 0;
    public static final short COMMIT_STAGE = 1;
    public static final short ROLLBACK_STAGE = 2;
    public static final short BIZ_STAGE = 3;
    private short type = -1;

    /**
     * Creates a new instance.
     */
    public TransportException() {
        super();
    }

    /**
     * Creates a new instance.
     */
    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransportException(short stage, String message, Throwable cause) {
        this(message,cause);
        this.type = stage;
    }

    /**
     * Creates a new instance.
     */
    public TransportException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public TransportException(Throwable cause) {
        super(cause);
    }

    public short getType() {
        return type;
    }
}