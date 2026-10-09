package com.example.SPSProjectBackend.controller;


import com.example.SPSProjectBackend.model.SpNormsGroup;
import com.example.SPSProjectBackend.service.SpNormsGroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spnormsgroup")
public class SpNormsGroupController {

    private final SpNormsGroupService service;

    public SpNormsGroupController(SpNormsGroupService service) {
        this.service = service;
    }

    @GetMapping
    public List<SpNormsGroup> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SpNormsGroup getById(@PathVariable String id) {
        return service.findById(id).orElse(null);
    }

    @PostMapping
    public SpNormsGroup create(@RequestBody SpNormsGroup group) {
        return service.save(group);
    }

    @PutMapping("/{id}")
    public SpNormsGroup update(@PathVariable String id, @RequestBody SpNormsGroup group) {
        group.setSectionTypeId(id);
        return service.save(group);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.deleteById(id);
    }
}

