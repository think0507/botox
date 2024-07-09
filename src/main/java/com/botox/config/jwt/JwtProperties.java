package com.botox.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@Getter @Setter
@ConfigurationProperties("jwt")
public class JwtProperties {

    private String issuer;
    private String secretKey;
}

