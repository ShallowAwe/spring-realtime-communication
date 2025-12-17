package com.rudra.realTimeApi.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebSocketViewController {

    @Value("${server.port:8080}")
    private String serverPort;

    @GetMapping("/monitor")
    public String monitorPage(Model model) {
        // Pass all endpoint URLs to the template
        model.addAttribute("wsUrl", "ws://localhost:" + serverPort + "/ws/metrics");
        model.addAttribute("sseUrl", "/sse/stream");
        model.addAttribute("shortPollUrl", "/polling/short");
        model.addAttribute("longPollUrl", "/polling/long");

        return "monitor";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/monitor";
    }
}
