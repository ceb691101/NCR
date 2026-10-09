package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.ProgressMoniterDTO;
import com.example.SPSProjectBackend.model.ProgressMoniter;
import com.example.SPSProjectBackend.repository.ProgressMoniterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProgressMoniterService {

    @Autowired
    private ProgressMoniterRepository repository;

    public ProgressMoniter saveProgressMoniter(ProgressMoniterDTO dto) {
        ProgressMoniter progressMoniter = new ProgressMoniter();
        progressMoniter.setId(dto.getId());
        progressMoniter.setDeptId(dto.getDeptId());
        progressMoniter.setName(dto.getName());
        progressMoniter.setPercentage(dto.getPercentage());
        return repository.save(progressMoniter);
    }
}