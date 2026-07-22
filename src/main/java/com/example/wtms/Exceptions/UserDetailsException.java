package com.example.wtms.Exceptions;


public class UserDetailsException extends RuntimeException {

    public UserDetailsException() {
        super();
    }

    public UserDetailsException(String message) {
        super(message);
    }

    public UserDetailsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserDetailsException(Throwable cause) {
        super(cause);
    }
}