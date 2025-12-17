package com.rudra.realTimeApi.websocket;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class WebSocketBroadcastService {

    private final WebSocketSessionRegistry registry;

    public WebSocketBroadcastService(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    public void broadcast(String payload) {
        registry.getAll().forEach(client -> {
            WebSocketSession session = client.getSession();
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(payload));
                } catch (Exception e) {
                    registry.remove(session);
                }
            } else {
                registry.remove(session);
            }
        });
    }
}
