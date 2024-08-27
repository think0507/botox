package com.botox.domain;

import com.botox.constant.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "friendship_request")
@Getter @Setter
public class FriendshipRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @ManyToOne
    @JoinColumn(name = "sender_nickname", referencedColumnName = "user_nickname")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_nickname", referencedColumnName = "user_nickname")
    private User receiver;

    @Column(name = "request_time")
    private LocalDateTime requestTime;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}