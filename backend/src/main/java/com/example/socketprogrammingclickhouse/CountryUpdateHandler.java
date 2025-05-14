package com.example.socketprogrammingclickhouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CountryUpdateHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

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
        Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = payload.get("type");

        if ("adminMessage".equals(type)) {
            // Only allow admin-service to send admin messages (basic validation)
            if (session.getAttributes().containsKey("isAdmin")) {
                String adminMessage = payload.get("message");
                if (adminMessage != null && !adminMessage.trim().isEmpty()) {
                    String broadcastMessage = String.format(
                        "{\"type\":\"adminMessage\",\"message\":\"Admin: %s\"}",
                        adminMessage
                    );
                    broadcastMessage(broadcastMessage);
                } else {
                    session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Empty admin message\"}"));
                }
            } else {
                session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Unauthorized admin message\"}"));
            }
            return;
        }

        session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Invalid message type\"}"));
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
}