package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.AmndTypeDTO;
import com.example.SPSProjectBackend.model.AmndType;
import com.example.SPSProjectBackend.repository.AmndTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AmndTypeService {

    @Autowired
    private AmndTypeRepository repository;

    public List<AmndTypeDTO> getAllAmndTypes() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<AmndTypeDTO> getAmndTypeByCode(String amdType) {
        return repository.findById(amdType)
                .map(this::convertToDTO);
    }

    private AmndTypeDTO convertToDTO(AmndType entity) {
        AmndTypeDTO dto = new AmndTypeDTO();
        dto.setAmdType(entity.getAmdType());
        dto.setAmdDesc(entity.getAmdDesc());
        dto.setUptblName(entity.getUptblName());
        dto.setFieldName(entity.getFieldName());
        dto.setNvalTblnm(entity.getNvalTblnm());
        dto.setNvalFldnm(entity.getNvalFldnm());
        dto.setDtType(entity.getDtType());
        dto.setValJrnl(entity.getValJrnl());
        dto.setAmdtStatus(entity.getAmdtStatus());
        dto.setUserId(entity.getUserId());
        dto.setEnteredDtime(entity.getEnteredDtime());
        dto.setEditedUserId(entity.getEditedUserId());
        dto.setEditedDtime(entity.getEditedDtime());
        return dto;
    }
}