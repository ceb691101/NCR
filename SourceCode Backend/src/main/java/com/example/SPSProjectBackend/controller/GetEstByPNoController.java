package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.PcesthmtDto;
import com.example.SPSProjectBackend.model.Pcesthmt;
import com.example.SPSProjectBackend.service.GetEstByProNo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estimate")
public class GetEstByPNoController {
    private final GetEstByProNo estimateService;

    @Autowired
    public GetEstByPNoController(GetEstByProNo estimateService) {
        this.estimateService = estimateService;
    }

    @GetMapping("/byproject")
    public List<PcesthmtDto> getEstimatesByProjectNo(@RequestParam String projectNo) {
        return estimateService.getEstimatesByProjectNo(projectNo);
    }
}
