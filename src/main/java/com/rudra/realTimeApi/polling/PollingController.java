package com.rudra.realTimeApi.polling;

import com.rudra.realTimeApi.services.Count;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/polling")
@RequiredArgsConstructor
public class PollingController {

    private final Count count;
    private final AtomicInteger shortPollCount = new AtomicInteger(0);
    private final AtomicInteger longPollCount = new AtomicInteger(0);

    @GetMapping("/short")
    public Map<String, Object> shortPolling() {
        int requestNumber = shortPollCount.incrementAndGet();
        log.info("Short Polling: Request #{} - Current counter value: {}",
                requestNumber, count.getCounter());

        Map<String, Object> metric = count.getMetric();
        log.debug("Short Polling: Returning data: {}", metric);

        return metric;
    }

    @GetMapping("/long")
    public DeferredResult<Map<String, Object>> longPolling(
            @RequestParam int lastValue) {

        int requestNumber = longPollCount.incrementAndGet();
        log.info("Long Polling: Request #{} started - Waiting for value change from: {}",
                requestNumber, lastValue);

        DeferredResult<Map<String, Object>> result = new DeferredResult<>(30000L);

        result.onTimeout(() -> {
            log.warn("Long Polling: Request #{} timed out after 30s", requestNumber);
            result.setErrorResult("Request timed out");
        });

        result.onCompletion(() -> {
            log.info("Long Polling: Request #{} completed", requestNumber);
        });

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                int checkCount = 0;
                while (count.getCounter() == lastValue) {
                    Thread.sleep(200);
                    checkCount++;

                    if (checkCount % 25 == 0) { // Log every 5 seconds (25 * 200ms)
                        log.debug("Long Polling: Request #{} still waiting... ({}s elapsed)",
                                requestNumber, checkCount * 200 / 1000);
                    }
                }

                Map<String, Object> metric = count.getMetric();
                log.info("Long Polling: Request #{} - Value changed to: {}, responding with data",
                        requestNumber, count.getCounter());
                result.setResult(metric);

            } catch (InterruptedException e) {
                log.error("Long Polling: Request #{} interrupted", requestNumber, e);
                Thread.currentThread().interrupt();
                result.setErrorResult("Request interrupted");
            } catch (Exception e) {
                log.error("Long Polling: Request #{} error occurred", requestNumber, e);
                result.setErrorResult(e.getMessage());
            }
        });

        return result;
    }
}
