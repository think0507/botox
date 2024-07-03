package com.botox.domain;

import com.botox.constant.UserStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String userProfile;
    private String userProfilePic;
    private int userTemperatureLevel;
    private String userNickname;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status; // enum: ONLINE, OFFLINE

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public String getUserProfilePic() {
        return userProfilePic;
    }

    public void setUserProfilePic(String userProfilePic) {
        this.userProfilePic = userProfilePic;
    }

    public int getUserTemperatureLevel() {
        return userTemperatureLevel;
    }

    public void setUserTemperatureLevel(int userTemperatureLevel) {
        this.userTemperatureLevel = userTemperatureLevel;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}