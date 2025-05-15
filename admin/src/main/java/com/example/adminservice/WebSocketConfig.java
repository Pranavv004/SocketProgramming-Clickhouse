package com.example.adminservice;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AdminMessageHandler adminMessageHandler;

    public WebSocketConfig(AdminMessageHandler adminMessageHandler) {
        this.adminMessageHandler = adminMessageHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(adminMessageHandler, "/ws/admin")
                .setAllowedOrigins("*"); // Adjust for production
    }
}