package com.botox.config;

//import com.botox.controller.StompHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


@Configuration
@EnableWebSocketMessageBroker
//@EnableWebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(new MyWebSocketHandler(), "/ws").setAllowedOrigins("http://localhost:3000");
//    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // stomp 접속 주소 url = ws://localhost:8080/ws, 프로토콜이 http가 아니다!
        registry.addEndpoint("/ws") // 연결될 엔드포인트
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 구독(수신)하는 요청 엔드포인트
        registry.enableSimpleBroker("/sub");

        // 메시지를 발행(송신)하는 엔드포인트
        registry.setApplicationDestinationPrefixes("/pub");
    }

}



//class MyWebSocketHandler extends TextWebSocketHandler {
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        // WebSocket 연결이 성립된 후 호출됩니다.
//        System.out.println("New WebSocket connection established");
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        // 클라이언트로부터 메시지를 받았을 때 호출됩니다.
//        String payload = message.getPayload();
//        System.out.println("Received message: " + payload);
//
//        // 받은 메시지를 그대로 클라이언트에게 회신하는 예시
//        session.sendMessage(new TextMessage("Echo: " + payload));
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        // WebSocket 연결이 종료된 후 호출됩니다.
//        System.out.println("WebSocket connection closed with status: " + status);
//    }
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//        // WebSocket 연결 중 오류가 발생했을 때 호출됩니다.
//        System.out.println("Transport error: " + exception.getMessage());
//        session.close(CloseStatus.SERVER_ERROR);
//    }
//}
