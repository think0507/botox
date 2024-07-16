package com.botox.config;

import com.botox.controller.TextChatController;
import com.botox.repository.ChatRepository;
import com.botox.repository.RoomRepository;
import com.botox.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatRepository chatRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public WebSocketConfig(ChatRepository chatRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Bean
    public TextChatController webSocketHandler() {
        return new TextChatController(chatRepository, roomRepository, userRepository);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(), "/ws").setAllowedOrigins("https://suportscore.site");
    }
}
