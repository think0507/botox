package com.botox.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserCreateForm {
    private String userId;
    private String password1;
    private String password2;
    private String userNickName;
}