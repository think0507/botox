package com.botox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@EnableAspectJAutoProxy
public class BotoxApplication {
	public static void main(String[] args) {
		SpringApplication.run(BotoxApplication.class, args);
	}
}