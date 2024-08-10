package com.botox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_participant")
@Getter
@Setter
@NoArgsConstructor
public class RoomParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "entry_time")
    private LocalDateTime entryTime;

    public RoomParticipant(Room room, User user, LocalDateTime entryTime) {
        this.room = room;
        this.user = user;
        this.entryTime = entryTime;
    }
}