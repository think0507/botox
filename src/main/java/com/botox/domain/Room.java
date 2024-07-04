package com.botox.domain;

import com.botox.constant.RoomStatus;
import com.botox.constant.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_num")
    private Long roomNum;

    private String roomTitle;

    private String roomContent;

    private Integer roomUserCount;

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    private String gameName;

    @ManyToOne
    @JoinColumn(name = "room_master")
    private User roomMaster;

    @Enumerated(EnumType.STRING)
    private RoomStatus roomStatus;

    private String roomPassword;
    private int roomCapacityLimit;
    private LocalDateTime roomUpdateTime;
    private LocalDateTime roomCreateAt;

    @OneToMany(mappedBy = "room")
    private List<RoomUser> participants;




    // Custom method to get room master's ID
    public Long getRoomMasterId() {
        return this.roomMaster != null ? this.roomMaster.getUserId() : null;
    }
}