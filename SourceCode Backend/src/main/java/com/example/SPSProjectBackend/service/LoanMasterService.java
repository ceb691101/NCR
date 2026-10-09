package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.LoanMasterDTO;
import com.example.SPSProjectBackend.model.LoanMaster;
import com.example.SPSProjectBackend.repository.LoanMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoanMasterService {

    @Autowired
    private LoanMasterRepository repository;

    public List<LoanMasterDTO> getAllLoans() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<LoanMasterDTO> getLoanByAccNbr(String accNbr) {
        return repository.findById(accNbr).map(this::convertToDTO);
    }

    // LoanMasterService.java - Add this method to the class

    public LoanMasterDTO updateLoanMaster(String accNbr, LoanMasterDTO updatedDto) {
        Optional<LoanMaster> optional = repository.findById(accNbr);
        if (!optional.isPresent()) {
            throw new RuntimeException("LoanMaster not found for accNbr: " + accNbr);
        }
        LoanMaster entity = optional.get();
        updateLoanMasterFields(entity, updatedDto);
        entity.setEditedUser(updatedDto.getEditedUser() != null ? updatedDto.getEditedUser() : "SYSTEM");
        LoanMaster saved = repository.save(entity);
        return convertToDTO(saved);
    }

    private void updateLoanMasterFields(LoanMaster entity, LoanMasterDTO dto) {
        if (dto.getLoanType() != null) entity.setLoanType(dto.getLoanType());
        if (dto.getLoanAmt() != null) entity.setLoanAmt(dto.getLoanAmt());
        if (dto.getNoMonths() != null) entity.setNoMonths(dto.getNoMonths());
        if (dto.getStBillCycle() != null) entity.setStBillCycle(dto.getStBillCycle());
        if (dto.getEndBillCycle() != null) entity.setEndBillCycle(dto.getEndBillCycle());
        if (dto.getIntRate() != null) entity.setIntRate(dto.getIntRate());
        if (dto.getActiveSt() != null) entity.setActiveSt(dto.getActiveSt());
        if (dto.getMonPmnt() != null) entity.setMonPmnt(dto.getMonPmnt());
        if (dto.getEnteredUser() != null) entity.setEnteredUser(dto.getEnteredUser());
        if (dto.getEditedUser() != null) entity.setEditedUser(dto.getEditedUser());
    }

    private LoanMasterDTO convertToDTO(LoanMaster entity) {
        LoanMasterDTO dto = new LoanMasterDTO();
        dto.setAccNbr(entity.getAccNbr());
        dto.setLoanType(entity.getLoanType());
        dto.setLoanAmt(entity.getLoanAmt());
        dto.setNoMonths(entity.getNoMonths());
        dto.setStBillCycle(entity.getStBillCycle());
        dto.setEndBillCycle(entity.getEndBillCycle());
        dto.setIntRate(entity.getIntRate());
        dto.setActiveSt(entity.getActiveSt());
        dto.setMonPmnt(entity.getMonPmnt());
        dto.setEnteredUser(entity.getEnteredUser());
        dto.setEditedUser(entity.getEditedUser());
        return dto;
    }
}
