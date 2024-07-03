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

    // Getters, setters, constructors

}

