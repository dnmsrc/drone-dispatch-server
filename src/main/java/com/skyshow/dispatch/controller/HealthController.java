package com.skyshow.dispatch.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        body.put("db", (one != null && one == 1) ? "connected" : "unknown");
        body.put("time", LocalDateTime.now().toString());
        return body;
    }
}