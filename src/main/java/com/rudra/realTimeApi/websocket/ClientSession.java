package com.rudra.realTimeApi.websocket;

import org.springframework.web.socket.WebSocketSession;

public class ClientSession {

    private final WebSocketSession session;
    private final long connectedAt = System.currentTimeMillis();
    private volatile long lastPongAt = connectedAt;

    public ClientSession(WebSocketSession session) {
        this.session = session;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public void pongReceived() {
        lastPongAt = System.currentTimeMillis();
    }

    public long getLastPongAt() {
        return lastPongAt;
    }
}
