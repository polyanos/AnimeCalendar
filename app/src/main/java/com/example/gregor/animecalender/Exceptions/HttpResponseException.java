package com.example.gregor.animecalender.Exceptions;

/**
 * Created by Gregor on 27-10-2015.
 */
public class HttpResponseException extends Exception {
    public HttpResponseException() {
        super();
    }

    public HttpResponseException(String message) {
        super(message);
    }

    public HttpResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpResponseException(Throwable cause) {
        super(cause);
    }
}
