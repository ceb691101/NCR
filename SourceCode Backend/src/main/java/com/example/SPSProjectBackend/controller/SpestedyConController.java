package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.model.SpestedyCon;
import com.example.SPSProjectBackend.service.SpestedyConService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spestedycon")
public class SpestedyConController {

    private final SpestedyConService service;

    @Autowired
    public SpestedyConController(SpestedyConService service) {
        this.service = service;
    }

    // GET: all data
    @GetMapping
    public List<SpestedyCon> getAll() {
        return service.getAll();
    }

    // GET: by deptId
    @GetMapping("/dept/{deptId}")
    public List<SpestedyCon> getByDeptId(@PathVariable String deptId) {
        return service.getByDeptId(deptId);
    }
}
