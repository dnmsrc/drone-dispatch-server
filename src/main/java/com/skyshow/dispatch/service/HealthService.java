package com.skyshow.dispatch.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private final JdbcTemplate jdbcTemplate;

    public HealthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean databaseIsAlive() {
        Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return one != null && one == 1;
    }
}