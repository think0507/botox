package com.botox.config;

import com.botox.controller.ChatWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.kurento.client.KurentoClient;
import org.kurento.commons.exception.KurentoException;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public KurentoClient kurentoClient() {
        try {
            return KurentoClient.create("ws://localhost:8888/kurento");
        } catch (KurentoException e) {
            System.err.println("KurentoClient 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Bean
    public ChatWebSocketHandler webSocketHandler(KurentoClient kurentoClient) {
        return new ChatWebSocketHandler(kurentoClient);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(kurentoClient()), "/ws").setAllowedOrigins("http://localhost:3000");
    }
}
