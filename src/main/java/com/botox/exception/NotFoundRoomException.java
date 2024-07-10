package com.botox.exception;

public class NotFoundRoomException extends RuntimeException{
    public NotFoundRoomException(String message) {
        super(message);
    }
}
