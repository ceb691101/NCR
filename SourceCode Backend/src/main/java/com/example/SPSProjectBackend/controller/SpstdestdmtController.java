package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.SpstdestdmtDto;
import com.example.SPSProjectBackend.service.SpstdestdmtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class SpstdestdmtController {

    @Autowired
    private SpstdestdmtService spstdestdmtService;

    @GetMapping("/spsstdestdmt")
    public ResponseEntity<List<SpstdestdmtDto>> getAllByStdNo(@RequestParam String stdNo) {
        List<SpstdestdmtDto> result = spstdestdmtService.findAllByStdNo(stdNo);
        return ResponseEntity.ok(result);
    }
}