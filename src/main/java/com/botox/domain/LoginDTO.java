package com.botox.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class LoginDTO {

    private String username;
    private String password;
}