package com.botox.domain;

import com.botox.constant.RequestStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendshipRequestDTO {
    private Long requestId;
    private String senderNickname;
    private String receiverNickname;
    private LocalDateTime requestTime;
    private RequestStatus status;
}