// remove
package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.SpestedyCon;
import com.example.SPSProjectBackend.repository.SpestedyConRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpestedyConService {

    private final SpestedyConRepository repository;

    @Autowired
    public SpestedyConService(SpestedyConRepository repository) {
        this.repository = repository;
    }

    public List<SpestedyCon> getAll() {
        return repository.findAll();
    }

    public List<SpestedyCon> getByDeptId(String deptId) {
        return repository.findByIdDeptId(deptId);
    }
}
