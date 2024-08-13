package com.botox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class BotoxApplication {
	public static void main(String[] args) {
		SpringApplication.run(BotoxApplication.class, args);
	}
}