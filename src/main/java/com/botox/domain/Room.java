package com.botox.domain;
import com.botox.constant.RoomStatus;
import com.botox.constant.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "room")
@Getter @Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_num")
    private Long roomNum;

    @Column(name = "room_title")
    private String roomTitle;

    @Column(name = "room_content", columnDefinition = "TEXT")
    private String roomContent;

    private Integer roomUserCount=0;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type")
    private RoomType roomType;

    @Column(name = "game_name")
    private String gameName;

    @ManyToOne
    @JoinColumn(name = "room_master", referencedColumnName = "user_id")
    private User roomMaster;

    @Column(name = "room_status")
    @Enumerated(EnumType.STRING)
    private RoomStatus roomStatus;

    @Column(name = "room_password")
    private String roomPassword;

    @Column(name = "room_capacity_limit")
    private Integer roomCapacityLimit;

    @Column(name = "room_update_time")
    private LocalDateTime roomUpdateTime;

    @Column(name = "room_create_at")
    private LocalDateTime roomCreateAt;



    // Custom method to get room master's ID
    public String getRoomMasterId() {
        return this.roomMaster != null ? this.roomMaster.getUserId() : null;
    }
}