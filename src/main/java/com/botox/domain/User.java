package com.botox.domain;

import com.botox.constant.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "profile")
    private String userProfile;

    @Column(name = "profile_pic")
    private String userProfilePic;

    @Column(name = "user_temperature_level")
    private int userTemperatureLevel;

    @Column(name = "user_nickname")
    private String userNickname;

    @Column(name = "user_password")
    private String password;


    @Enumerated(EnumType.STRING)
    private UserStatus status; // enum: ONLINE, OFFLINE

    @OneToMany(mappedBy = "author")
    private List<Post> posts;

    @OneToMany(mappedBy = "sender")
    private List<Chat> sentChats;
    // Getters and Setters
//    public Long getUserId() {
//        return userId;
//    }
//
//    public void setUserId(Long userId) {
//        this.userId = userId;
//    }
//
//    public String getUserProfile() {
//        return userProfile;
//    }
//
//    public void setUserProfile(String userProfile) {
//        this.userProfile = userProfile;
//    }
//
//    public String getUserProfilePic() {
//        return userProfilePic;
//    }
//
//    public void setUserProfilePic(String userProfilePic) {
//        this.userProfilePic = userProfilePic;
//    }
//
//    public int getUserTemperatureLevel() {
//        return userTemperatureLevel;
//    }
//
//    public void setUserTemperatureLevel(int userTemperatureLevel) {
//        this.userTemperatureLevel = userTemperatureLevel;
//    }
//
//    public String getUserNickname() {
//        return userNickname;
//    }
//
//    public void setUserNickname(String userNickname) {
//        this.userNickname = userNickname;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public UserStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(UserStatus status) {
//        this.status = status;
//    }
}