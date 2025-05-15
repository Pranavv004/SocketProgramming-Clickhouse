package com.example.socketprogrammingclickhouse;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CountryUpdateHandler countryUpdateHandler;

    public WebSocketConfig(CountryUpdateHandler countryUpdateHandler) {
        this.countryUpdateHandler = countryUpdateHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(countryUpdateHandler, "/ws/country-updates")
        .setAllowedOrigins(
                "https://country-live-webpage.onrender.com",
                "https://admin-webpage.onrender.com",
                "http://localhost:3000"
            );
    }
}