package com.botox.exception;

public class NotFoundCommentException extends RuntimeException {
    public NotFoundCommentException(String message) {
        super(message);
    }
}