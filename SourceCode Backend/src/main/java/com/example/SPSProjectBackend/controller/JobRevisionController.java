package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.JobReviseDropDownDTO;
import com.example.SPSProjectBackend.dto.JobRevisionDTO;
import com.example.SPSProjectBackend.dto.PcestdmtDto;
import com.example.SPSProjectBackend.model.Pcesthmt;
import com.example.SPSProjectBackend.service.JobRevisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estimates")
public class JobRevisionController {
        @Autowired
        private JobRevisionService jobRevisionService;

        @PostMapping("/save")
        public ResponseEntity<String> saveEstimate(@RequestBody JobRevisionDTO jobRevisionDTO) {
            jobRevisionService.saveEstimateWithDetails(jobRevisionDTO);
            return ResponseEntity.ok("Saved successfully");
        }

        @GetMapping("/dropdowndata")
        public ResponseEntity<JobReviseDropDownDTO> getDropdownData() {
            return ResponseEntity.ok(jobRevisionService.getDropdownData());
        }

        @Autowired
        private JobRevisionService pcestdmtService;

        @GetMapping("/byproject")
        public ResponseEntity<List<PcestdmtDto>> getDmtByProjectNo(@RequestParam String projectNo) {
            List<PcestdmtDto> result = pcestdmtService.getDmtRecordsByProjectNo(projectNo);
            return ResponseEntity.ok(result);
        }

}

