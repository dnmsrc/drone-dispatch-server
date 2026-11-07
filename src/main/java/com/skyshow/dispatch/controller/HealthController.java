package com.skyshow.dispatch.controller;

import com.skyshow.dispatch.service.HealthService;
import com.skyshow.dispatch.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/api/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        body.put("db", healthService.databaseIsAlive() ? "connected" : "unknown");
        body.put("time", LocalDateTime.now().toString());
        return Result.success(body);
    }
}