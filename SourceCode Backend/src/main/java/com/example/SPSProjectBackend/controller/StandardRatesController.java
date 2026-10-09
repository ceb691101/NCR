package com.example.SPSProjectBackend.controller;



import com.example.SPSProjectBackend.model.StandardRates;
import com.example.SPSProjectBackend.service.StandardRatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/standard-rates")
public class StandardRatesController {

    @Autowired
    private StandardRatesService service;

    @GetMapping
    public List<StandardRates> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Optional<StandardRates> getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PostMapping
    public StandardRates create(@RequestBody StandardRates standardRates) {
        return service.save(standardRates);
    }

    @PutMapping("/{id}")
    public StandardRates update(@PathVariable String id, @RequestBody StandardRates updated) {
        updated.setLinesectiontypeid(id);
        return service.save(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}

