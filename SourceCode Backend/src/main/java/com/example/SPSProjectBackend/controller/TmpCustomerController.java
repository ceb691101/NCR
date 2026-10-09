package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.TmpCustomerDTO;
import com.example.SPSProjectBackend.service.TmpCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/tmpcustomers")
public class TmpCustomerController {

    @Autowired
    private TmpCustomerService tmpCustomerService;

    @GetMapping
    public List<TmpCustomerDTO> getAllTmpCustomers() {
        return tmpCustomerService.getAllTmpCustomers();
    }

    // Changed to RequestParam to handle jobNbr with slashes
    @GetMapping("/by-job")
    public ResponseEntity<TmpCustomerDTO> getTmpCustomerByJobNbr(@RequestParam String jobNbr) {
        try {
            TmpCustomerDTO dto = tmpCustomerService.getTmpCustomerByJobNbr(jobNbr);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/generate-acc")
    public ResponseEntity<Map<String, Object>> generateAcc(@RequestBody Map<String, Object> body) {
        String jobNbr = (String) body.get("JobNbr");
        if (jobNbr == null) {
            return ResponseEntity.badRequest().build();
        }
        tmpCustomerService.generateAcc(jobNbr);
        Map<String, Object> response = new HashMap<>();
        Map<String, String> gen = new HashMap<>();
        gen.put("result", "1");
        response.put("generatedAccNo", gen);
        return ResponseEntity.ok(response);
    }
}
