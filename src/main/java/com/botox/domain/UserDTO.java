package com.botox.domain;

import com.botox.constant.UserRole;
import com.botox.constant.UserStatus;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String userProfile;
    private String userProfilePic;
    private Integer userTemperatureLevel=0;
    private String userNickname;
    private UserStatus status;
    private UserRole userRole;


}