package com.rudra.realTimeApi.sse;

import com.rudra.realTimeApi.services.Count;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/sse")
public class SseController {

    private final Count metricService;
    private final List<SseEmitter> clients = new CopyOnWriteArrayList<>();

    public SseController(Count metricService) {
        this.metricService = metricService;
    }


    @GetMapping("/stream")
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        clients.add(emitter);

        emitter.onCompletion(() -> clients.remove(emitter));
        emitter.onTimeout(() -> clients.remove(emitter));
        emitter.onError(e -> clients.remove(emitter));

        return emitter;
    }

    @Scheduled(fixedRate = 2000)
    public void pushUpdates() {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(metricService.getMetric());
            } catch (Exception e) {
                clients.remove(emitter);
            }
        }
    }



}
