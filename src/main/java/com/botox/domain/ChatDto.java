package com.botox.domain;

import lombok.Data;

@Data
public class ChatDto {
    private Integer roomId;
    private Integer userId;
    private String chat;
}