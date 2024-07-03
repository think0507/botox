package com.botox.domain;

import com.botox.constant.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
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

    // Getters, setters, constructors

}

