package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.PivAmount;
import com.example.SPSProjectBackend.model.PivAmountId;
import com.example.SPSProjectBackend.repository.PivAmountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class PivAmountService {

    @Autowired
    private PivAmountRepository repository;

    public Optional<PivAmount> getPivAmount(PivAmountId id) {
        return repository.findById(id);
    }

    public PivAmount savePivAmount(PivAmount pivAmount) {
        return repository.save(pivAmount);
    }

    public void deletePivAmount(PivAmountId id) {
        repository.deleteById(id);
    }
}
