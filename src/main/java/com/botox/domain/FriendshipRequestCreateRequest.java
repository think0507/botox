package com.botox.domain;

import lombok.Data;

@Data
public class FriendshipRequestCreateRequest {
    private Long senderId;
    private Long receiverId;
}
