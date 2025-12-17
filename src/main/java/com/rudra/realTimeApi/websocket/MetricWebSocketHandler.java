package com.rudra.realTimeApi.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class MetricWebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger log =
            LoggerFactory.getLogger(MetricWebSocketHandler.class);

    private final WebSocketSessionRegistry registry;

    public MetricWebSocketHandler(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        registry.add(session);
        log.info("WS CONNECTED | sessionId={} | totalSessions={}",
                session.getId(),
                registry.getAll().size());
    }


    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) {
        ClientSession client = registry.get(session.getId());
        if (client != null) {
            client.pongReceived();
            log.debug("WS PONG RECEIVED | sessionId={}", session.getId());
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        registry.remove(session);
        log.info("WS DISCONNECTED | sessionId={} | status={} | totalSessions={}",
                session.getId(),
                status,
                registry.getAll().size());
    }

}
