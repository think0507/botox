package com.botox.domain;

import com.botox.constant.RoomType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomNum;

    private String roomTitle;
    @Column(columnDefinition = "TEXT")
    private String roomContent;

    @Enumerated(EnumType.STRING)
    private RoomType roomType; // enum: VOICE, TEXT

    private String gameName;

    @ManyToOne
    @JoinColumn(name = "room_master")
    private User roomMaster;

    private int roomStatus;
    private String roomPassword;
    private int roomCapacityLimit;
    private LocalDateTime roomUpdateTime;
    private LocalDateTime roomCreateAt;

    // Getters, setters, constructors
}
