package com.example.gregor.animecalender.Exceptions;

/**
 * Created by Gregor on 9-11-2015.
 */
public class XmlParseException extends Exception {
    /**
     * Constructs a new {@code Exception} that includes the current stack trace.
     */
    public XmlParseException() {
        super();
    }

    /**
     * Constructs a new {@code Exception} with the current stack trace and the
     * specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public XmlParseException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code Exception} with the current stack trace, the
     * specified detail message and the specified cause.
     *
     * @param detailMessage the detail message for this exception.
     * @param throwable
     */
    public XmlParseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@code Exception} with the current stack trace and the
     * specified cause.
     *
     * @param throwable the cause of this exception.
     */
    public XmlParseException(Throwable throwable) {
        super(throwable);
    }
}
