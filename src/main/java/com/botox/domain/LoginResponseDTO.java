package com.botox.domain;

import com.botox.constant.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter @Setter
@AllArgsConstructor
public class LoginResponseDTO {
    private String username;
    private String password;
    private String accessToken;
    private String refreshToken;
    private UserStatus userStatus; // ONLINE, OFFLINE
}
