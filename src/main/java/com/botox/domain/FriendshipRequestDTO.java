package com.botox.domain;

import com.botox.constant.RequestStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data

public class FriendshipRequestDTO {
    private Long requestId;
    private Long senderId;
    private Long receiverId;
//    private UserIdOnlyDTO sender;
//    private UserIdOnlyDTO receiver;
    private LocalDateTime requestTime;
    private RequestStatus status;

}
