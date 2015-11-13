package com.example.gregor.animecalender.Exceptions;

/**
 * Created by Gregor on 27-10-2015.
 */
public class AuthorizeException extends Exception {
    public AuthorizeException() {
        super();
    }

    public AuthorizeException(String message) {
        super(message);
    }

    public AuthorizeException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorizeException(Throwable cause) {
        super(cause);
    }
}
