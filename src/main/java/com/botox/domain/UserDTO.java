package com.botox.domain;

import com.botox.constant.UserStatus;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String userId;
    private String userProfile;
    private String userProfilePic;
    private int userTemperatureLevel;
    private String userNickname;
    private UserStatus status;
}