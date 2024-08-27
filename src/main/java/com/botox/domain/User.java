package com.botox.domain;

import com.botox.constant.UserRole;
import com.botox.constant.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_user_nickname", columnList = "user_nickname"),
        @Index(name = "idx_temperature_status", columnList = "user_temperature_level, status")
})
@Getter @Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "user_profile")
    private String userProfile;

    @Column(name = "user_profile_pic")
    private String userProfilePic;

    @Column(name = "user_temperature_level")
    private Integer userTemperatureLevel;

    @Column(name = "user_nickname", nullable = false, unique = true)
    private String userNickname;

    @Column(name = "user_password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @OneToMany(mappedBy = "sender")
    private List<Chat> sentChats;

    @OneToMany(mappedBy = "reportingUser")
    private List<Report> reportsFiled;

    @OneToMany(mappedBy = "reportedUser")
    private List<Report> reportsReceived;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role = UserRole.USER;
}