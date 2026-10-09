package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.YrMnthDTO;
import com.example.SPSProjectBackend.model.YrMnth;
import com.example.SPSProjectBackend.repository.YrMnthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class YrMnthService {

    @Autowired
    private YrMnthRepository repository;

    public List<YrMnthDTO> getAllYrMnths() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<YrMnthDTO> getYrMnthByCycle(Integer billCycle) {
        return repository.findById(billCycle)
                .map(this::convertToDTO);
    }

    private YrMnthDTO convertToDTO(YrMnth entity) {
        YrMnthDTO dto = new YrMnthDTO();
        dto.setBillCycle(entity.getBillCycle());
        dto.setBillMnth(entity.getBillMnth());
        return dto;
    }
}