package com.botox.domain;

import com.botox.constant.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true)
    private String userId;

    @Column(name = "user_profile")
    private String userProfile;

    @Column(name = "user_profile_pic")
    private String userProfilePic;

    @Column(name = "user_temperature_level")
    private Integer userTemperatureLevel;

    @Column(name = "user_nickname")
    private String userNickname;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;
}