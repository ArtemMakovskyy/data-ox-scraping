package com.dataox.exception;

public class JobFetchingException extends RuntimeException {
    public JobFetchingException(String message, Throwable cause) {
        super(message, cause);
    }
}
