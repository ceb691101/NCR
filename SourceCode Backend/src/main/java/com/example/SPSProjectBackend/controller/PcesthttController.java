package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.service.PcesthttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PcesthttController {

    @Autowired
    private PcesthttService pcesthttService;

    @GetMapping("/pcesthtt/row-count-by-status")
    public Map<Short, Long> getRowCountByStatus() {
        return pcesthttService.getRowCountByStatus();
    }
}