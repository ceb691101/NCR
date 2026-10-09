package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.ProgressMoniterDTO;
import com.example.SPSProjectBackend.model.ProgressMoniter;
import com.example.SPSProjectBackend.service.ProgressMoniterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/progressMoniter")
public class ProgressMoniterController {

    @Autowired
    private ProgressMoniterService service;

    @PostMapping("/save")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ProgressMoniter> saveProgressMoniter(@RequestBody ProgressMoniterDTO dto) {

        System.out.println(dto.getDeptId());
        System.out.println(dto.getName());
        System.out.println(dto.getPercentage());
        System.out.println(dto.getId());
        ProgressMoniter savedProgressMoniter = service.saveProgressMoniter(dto);
        return ResponseEntity.ok(savedProgressMoniter);
    }
}