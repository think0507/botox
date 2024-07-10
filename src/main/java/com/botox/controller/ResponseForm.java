package com.botox.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ResponseForm<T> {
    private HttpStatus code;
    private T data;
    private String message;
}
