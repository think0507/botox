package com.botox.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000") // 허용할 도메인
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .exposedHeaders("location")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}