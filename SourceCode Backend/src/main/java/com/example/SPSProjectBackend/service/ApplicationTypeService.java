// remove
package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.ApplicationTypeDto;
import com.example.SPSProjectBackend.model.ApplicationType;
import com.example.SPSProjectBackend.repository.ApplicationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationTypeService {

    @Autowired
    private ApplicationTypeRepository repository;

    public List<ApplicationTypeDto> getAllApplicationTypes() {
        return repository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ApplicationTypeDto convertToDto(ApplicationType entity) {
        ApplicationTypeDto dto = new ApplicationTypeDto();
        dto.setApptype(entity.getApptype());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}
