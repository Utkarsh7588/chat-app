package com.example.chat_app.exceptions;

public class EmptyCredentialsException extends RuntimeException {
    public EmptyCredentialsException(String message) {
        super(message);
    }
}
