    package com.rudra.realTimeApi.websocket;

    import com.rudra.realTimeApi.services.Count;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Component;
    import org.springframework.web.socket.WebSocketHandler;
    import tools.jackson.databind.ObjectMapper;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;


    @Component
    public class WebSocketPublisher {
        private static final Logger log =
                LoggerFactory.getLogger(WebSocketHandler.class);

        private final Count metricService;
        private final WebSocketBroadcastService broadcastService;
        private final ObjectMapper mapper = new ObjectMapper();

        public WebSocketPublisher(
                Count metricService,
                WebSocketBroadcastService broadcastService) {
            this.metricService = metricService;
            this.broadcastService = broadcastService;
        }

        @Scheduled(fixedRate = 2000)
        public void pushUpdates() throws Exception {
            broadcastService.broadcast(
                    mapper.writeValueAsString(metricService.getMetric())
            );
        }
    }
