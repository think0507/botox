package com.botox.domain;

import lombok.Data;

@Data
public class FriendshipRequestCreateRequest {
    private String senderNickname;
    private String receiverNickname;
}