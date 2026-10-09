package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.GenerationHistoryDTO;
import com.example.SPSProjectBackend.service.GenerationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api/v1/generation-history")
public class GenerationHistoryController {

    @Autowired
    private GenerationHistoryService generationHistoryService;

    @GetMapping("/{accNbr}/{areaCd}")
    public ResponseEntity<List<GenerationHistoryDTO>> getGenerationHistory(
            @PathVariable String accNbr,
            @PathVariable String areaCd,
            @RequestParam(defaultValue = "3") int cycleCount) {
        
        List<GenerationHistoryDTO> history = generationHistoryService.getLatestGenerationHistory(accNbr, areaCd, cycleCount);
        return ResponseEntity.ok(history);
    }
}
