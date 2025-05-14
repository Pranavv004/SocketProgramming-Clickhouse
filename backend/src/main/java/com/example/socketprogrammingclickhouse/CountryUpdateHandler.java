package com.example.socketprogrammingclickhouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CountryUpdateHandler extends TextWebSocketHandler {
    private final DataSource dataSource;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public CountryUpdateHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        broadcastUserCount();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        broadcastUserCount();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        CountryRequest request = objectMapper.readValue(message.getPayload(), CountryRequest.class);
        if (request.getName() == null || request.getName().trim().isEmpty() || 
            request.getCode() == null || !request.getCode().matches("^[A-Z]{2}$")) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Invalid country name or code\"}"));
            return;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO testdb.countries (name, code, inserted_at) VALUES (?, ?, now())")) {
            stmt.setString(1, request.getName());
            stmt.setString(2, request.getCode());
            stmt.executeUpdate();

            String updateMessage = String.format("{\"type\":\"country\",\"message\":\"New country added: %s (%s)\"}", 
                                                request.getName(), request.getCode());
            broadcastMessage(updateMessage);
        } catch (Exception e) {
            e.printStackTrace();
            String sanitizedError = e.getMessage().replaceAll("[\\p{Cntrl}]", " ");
            String url = System.getenv("SPRING_DATASOURCE_URL");
            String username = System.getenv("SPRING_DATASOURCE_USERNAME");
            String password = System.getenv("SPRING_DATASOURCE_PASSWORD");
            String logMessage = String.format(
                "Database error: %s | URL: %s | Username: %s | Password: %s",
                sanitizedError, url, username, password != null ? password : "null"
            );
            System.out.println(logMessage);
            String errorMsg = String.format("{\"type\":\"error\",\"message\":\"Database error: %s\"}", sanitizedError);
            session.sendMessage(new TextMessage(errorMsg));
        }
    }

    private void broadcastMessage(String message) throws Exception {
        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                session.sendMessage(textMessage);
            }
        }
    }

    private void broadcastUserCount() throws Exception {
        String userCountMessage = String.format("{\"type\":\"userCount\",\"count\":%d}", sessions.size());
        broadcastMessage(userCountMessage);
    }

    static class CountryRequest {
        private String name;
        private String code;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}