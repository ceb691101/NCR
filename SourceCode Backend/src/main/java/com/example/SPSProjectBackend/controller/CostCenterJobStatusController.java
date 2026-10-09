package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.CostCenterJobStatusDto;
import com.example.SPSProjectBackend.service.CostCenterJobStatusService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pcesthmt")
@CrossOrigin(origins = "http://localhost:3000" , allowCredentials = "true")
public class CostCenterJobStatusController {

    private final CostCenterJobStatusService service;

    public CostCenterJobStatusController(CostCenterJobStatusService service) {
        this.service = service;
    }

    @GetMapping("/jobstatus/{deptId}")
    public CostCenterJobStatusDto getJobStatusSummary(@PathVariable String deptId) {
        return service.getStatusSummaryForCostCenter(deptId);
    }
}
