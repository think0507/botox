package com.botox.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_user")
public class RoomUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_num")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime joinTime;
    private LocalDateTime leaveTime;

    // Getters, setters, constructors
}
