package com.botox.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDTO {
    private String username;
    private String userNickname;
    private String userProfile;
    private String userProfilePic;
}

