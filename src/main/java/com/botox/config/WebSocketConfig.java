//package com.botox.config;
//
//
//import com.botox.controller.ChatWebSocketHandler;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//import org.kurento.client.KurentoClient;
//
//@Configuration
//@EnableWebSocket
//public class WebSocketConfig implements WebSocketConfigurer {
//
//    @Bean
//    public KurentoClient kurentoClient() {
//        return KurentoClient.create("ws://localhost:8888/kurento");
//    }
//
//    @Bean
//    public ChatWebSocketHandler webSocketHandler(KurentoClient kurentoClient) {
//        return new ChatWebSocketHandler(kurentoClient);
//    }
//
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(webSocketHandler(kurentoClient()), "/ws").setAllowedOrigins("http://localhost:3000");
//    }
//}

package com.botox.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // stomp 접속 주소 url = ws://localhost:8080/ws, 프로토콜이 http가 아니다!
        registry.addEndpoint("/ws") // 연결될 엔드포인트
                .setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 구독(수신)하는 요청 엔드포인트
        registry.enableSimpleBroker("/sub");

        // 메시지를 발행(송신)하는 엔드포인트
        registry.setApplicationDestinationPrefixes("/pub");
    }
}