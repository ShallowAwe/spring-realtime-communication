package com.rudra.realTimeApi.websocket;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
@EnableScheduling
public class WebSocketHeartbeatService {

    private static final Logger log =
            LoggerFactory.getLogger(WebSocketHeartbeatService.class);

    private final WebSocketSessionRegistry registry;

    public WebSocketHeartbeatService(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @Scheduled(fixedRate = 15000)
    public void sendPing() {
        registry.getAll().forEach(client -> {
            WebSocketSession session = client.getSession();
            if (session.isOpen()) {
                try {
                    session.sendMessage(new PingMessage());
                } catch (Exception e) {
                    try {
                        session.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }



    @Scheduled(fixedRate = 30000)
    public void cleanupDeadSessions() {
        long now = System.currentTimeMillis();
        registry.getAll().removeIf(
                client -> now - client.getLastPongAt() > 30000
        );
    }
}
