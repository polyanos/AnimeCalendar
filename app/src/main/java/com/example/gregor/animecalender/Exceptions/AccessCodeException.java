package com.example.gregor.animecalender.Exceptions;

/**
 * Created by Gregor on 27-10-2015.
 */
public class AccessCodeException extends Exception {
    public AccessCodeException() {
        super();
    }

    public AccessCodeException(String message) {
        super(message);
    }

    public AccessCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessCodeException(Throwable cause) {
        super(cause);
    }
}
