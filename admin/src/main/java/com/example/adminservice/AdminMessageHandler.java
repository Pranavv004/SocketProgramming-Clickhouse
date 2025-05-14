package com.example.adminservice;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.net.URI;
import java.util.Map;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
@Component
public class AdminMessageHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebSocketSession userServiceSession;
    public AdminMessageHandler() {
        connectToUserService();
    }
    private void connectToUserService() {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            client.doHandshake(this, null, new URI("wss://country-live-service.onrender.com/ws/country-updates"))
                  .addCallback(session -> {
                      this.userServiceSession = session;
                      session.getAttributes().put("isAdmin", true);
                      System.out.println("Connected to country-live-service");
                  }, ex -> {
                      System.err.println("Failed to connect to country-live-service: " + ex.getMessage());
                  });
        } catch (Exception e) {
            System.err.println("Error connecting to country-live-service: " + e.getMessage());
        }
    }
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = payload.get("type");
        if ("adminMessage".equals(type)) {
            String adminMessage = payload.get("message");
            if (adminMessage != null && !adminMessage.trim().isEmpty()) {
                if (userServiceSession != null && userServiceSession.isOpen()) {
                    String broadcastMessage = String.format(
                        "{\"type\":\"adminMessage\",\"message\":\"%s\"}",
                        adminMessage
                    );
                    userServiceSession.sendMessage(new TextMessage(broadcastMessage));
                    session.sendMessage(new TextMessage("{\"type\":\"success\",\"message\":\"Message sent to all users\"}"));
                } else {
                    session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Not connected to user service\"}"));
                }
            } else {
                session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Empty admin message\"}"));
            }
        } else {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Invalid message type\"}"));
        }
    }
}