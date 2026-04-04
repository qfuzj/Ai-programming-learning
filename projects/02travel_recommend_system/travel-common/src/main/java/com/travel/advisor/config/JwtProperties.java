package com.travel.advisor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    private String issuer;
    private String secret;
    private Long accessTokenExpireSeconds;
    private Long refreshTokenExpireSeconds;
}
