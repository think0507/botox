package com.botox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    private String roomType; // enum: VOICE, TEXT

    private String gameName;

    @ManyToOne
    @JoinColumn(name = "room_master")
    private User roomMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int roomStatus;
    private String roomPassword;
    private int roomCapacityLimit;
    private LocalDateTime roomUpdateTime;
    private LocalDateTime roomCreateAt;

    // Getters for fields
    public Long getRoomNum() {
        return roomNum;
    }

    public String getRoomTitle() {
        return roomTitle;
    }

    public String getRoomContent() {
        return roomContent;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getGameName() {
        return gameName;
    }

    public User getRoomMaster() {
        return roomMaster;
    }

    public User getUser() {
        return user;
    }

    public int getRoomStatus() {
        return roomStatus;
    }

    public String getRoomPassword() {
        return roomPassword;
    }

    public int getRoomCapacityLimit() {
        return roomCapacityLimit;
    }

    public LocalDateTime getRoomUpdateTime() {
        return roomUpdateTime;
    }

    public LocalDateTime getRoomCreateAt() {
        return roomCreateAt;
    }

    // Setters for fields
    public void setRoomNum(Long roomNum) {
        this.roomNum = roomNum;
    }

    public void setRoomTitle(String roomTitle) {
        this.roomTitle = roomTitle;
    }

    public void setRoomContent(String roomContent) {
        this.roomContent = roomContent;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public void setRoomMaster(User roomMaster) {
        this.roomMaster = roomMaster;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRoomStatus(int roomStatus) {
        this.roomStatus = roomStatus;
    }

    public void setRoomPassword(String roomPassword) {
        this.roomPassword = roomPassword;
    }

    public void setRoomCapacityLimit(int roomCapacityLimit) {
        this.roomCapacityLimit = roomCapacityLimit;
    }

    public void setRoomUpdateTime(LocalDateTime roomUpdateTime) {
        this.roomUpdateTime = roomUpdateTime;
    }

    public void setRoomCreateAt(LocalDateTime roomCreateAt) {
        this.roomCreateAt = roomCreateAt;
    }

    // getRoomMasterId 메서드
    public Long getRoomMasterId() {
        return this.roomMaster != null ? this.roomMaster.getUserId() : null;
    }
}