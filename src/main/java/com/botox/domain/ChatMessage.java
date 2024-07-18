package com.botox.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessage {

    private Long id;
    private String name;
    private String message;

    public ChatMessage(Long id, String name, String message) {
        this.id = id;
        this.name = name;
        this.message = message;
    }
}