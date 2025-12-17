package com.rudra.realTimeApi.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class Count {

    private final AtomicInteger counter = new AtomicInteger(0);
    private volatile long lastUpdated = System.currentTimeMillis();

    @Scheduled(fixedRate = 2000)
    public void update() {
        counter.incrementAndGet();
        lastUpdated = System.currentTimeMillis();
    }

    public Map<String, Object> getMetric() {
        return Map.of(
                "counter", counter.get(),
                "lastUpdated", lastUpdated
        );
    }

    public int getCounter() {
        return counter.get();
    }
}
