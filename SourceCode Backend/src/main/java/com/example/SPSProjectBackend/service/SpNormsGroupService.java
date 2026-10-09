// remove
package com.example.SPSProjectBackend.service;


import com.example.SPSProjectBackend.model.SpNormsGroup;
import com.example.SPSProjectBackend.repository.SpNormsGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SpNormsGroupService {

    private final SpNormsGroupRepository repository;

    public SpNormsGroupService(SpNormsGroupRepository repository) {
        this.repository = repository;
    }

    public List<SpNormsGroup> findAll() {
        return repository.findAll();
    }

    public Optional<SpNormsGroup> findById(String id) {
        return repository.findById(id);
    }

    public SpNormsGroup save(SpNormsGroup group) {
        return repository.save(group);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }
}

