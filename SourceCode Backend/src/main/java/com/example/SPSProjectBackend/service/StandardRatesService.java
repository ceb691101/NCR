package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.StandardRates;
import com.example.SPSProjectBackend.repository.StandardRatesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StandardRatesService {

    @Autowired
    private StandardRatesRepository repository;

    public List<StandardRates> getAll() {
        return repository.findAll();
    }

    public Optional<StandardRates> getById(String id) {
        return repository.findById(id);
    }

    public StandardRates save(StandardRates standardRates) {
        return repository.save(standardRates);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }
}

