package com.botox.config;

import com.botox.config.jwt.TokenProvider;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOServer;
import io.netty.handler.codec.http.cors.CorsHandler;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class SocketIOConfig {

    final TokenProvider tokenProvider;
    private SocketIOServer server; // 서버 인스턴스를 클래스 레벨에서 관리

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(8090); // Spring Boot와 다른 포트를 사용하도록 설정

// JWT 토큰을 이용한 인증 설정
//        config.setAuthorizationListener(handshakeData -> {
//            // 클라이언트에서 전달된 토큰을 가져옴
//            String token = handshakeData.getSingleUrlParam("token");
//
//            // 토큰이 존재하고 유효한지 검증
//            if (token != null && tokenProvider.validateToken(token)) {
//                return true;  // 토큰이 유효한 경우 연결 허용
//            }
//            return false;  // 토큰이 유효하지 않은 경우 연결 거부
//        });
//
        server = new SocketIOServer(config);
        server.start();
        System.out.println("Socket.IO server started on port 8090"); // 서버 시작 로그 출력
        return server;
    }

//    @PreDestroy
//    public void stopSocketIOServer() {
//        if (server != null) {
//            server.stop(); // 애플리케이션 종료 시 서버를 명시적으로 종료
//        }
//    }
}
