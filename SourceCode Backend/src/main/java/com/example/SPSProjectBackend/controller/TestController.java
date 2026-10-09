package com.example.SPSProjectBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/database")
    public Map<String, Object> testDatabase() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", new java.util.Date());
        
        try {
            // Test if we can query the database
            Integer result = jdbcTemplate.queryForObject("SELECT 1 FROM systables WHERE tabid = 1", Integer.class);
            response.put("status", "SUCCESS");
            response.put("message", "Database connection is working");
            response.put("testResult", result);
            
            // Try to query journal table
            try {
                Long journalCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM dbadmin.tmp_journals", Long.class);
                response.put("journalCount", journalCount);
            } catch (Exception e) {
                response.put("journalTableError", e.getMessage());
            }
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Database connection failed: " + e.getMessage());
            response.put("error", e.toString());
        }
        
        return response;
    }
}