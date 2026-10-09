package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.service.EstimateResponse;
import com.example.SPSProjectBackend.service.PcesthmtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pcesthmt")
public class PcesthmtController {

    @Autowired
    private PcesthmtService pcesthmtService;

    @GetMapping("/estimateNumbers")
    public ResponseEntity<List<String>> getAllEstimateNumbers() {
        List<String> estimateNumbers = pcesthmtService.getAllEstimateNumbers();
        return ResponseEntity.ok(estimateNumbers);
    }

    @GetMapping("/{estimateNo}")
    public ResponseEntity<EstimateResponse> checkEstimate(@PathVariable String estimateNo) {
        EstimateResponse response = pcesthmtService.checkEstimateAndGetDate(estimateNo);
        return ResponseEntity.ok(response);
    }
}
