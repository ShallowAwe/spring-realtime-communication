package com.rudra.realTimeApi.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private final Map<String, ClientSession> sessions =
            new ConcurrentHashMap<>();

    public void add(WebSocketSession session) {
        sessions.put(session.getId(), new ClientSession(session));
    }

    public void remove(WebSocketSession session) {
        sessions.remove(session.getId());
    }

    public Collection<ClientSession> getAll() {
        return sessions.values();
    }

    public ClientSession get(String sessionId) {
        return sessions.get(sessionId);
    }
}

