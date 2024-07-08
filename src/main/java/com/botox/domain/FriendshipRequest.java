package com.botox.domain;

import com.botox.constant.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "friendshiprequest")
public class FriendshipRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    private LocalDateTime requestTime;

    @Enumerated(EnumType.STRING)
    private RequestStatus status; // enum: PENDING, ACCEPTED, DECLINED

    // Getters, setters, constructors
}
