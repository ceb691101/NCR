package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.MonTotDTO;
import com.example.SPSProjectBackend.model.MonTot;
import com.example.SPSProjectBackend.model.MonTotId;
import com.example.SPSProjectBackend.repository.MonTotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MonTotService {

    @Autowired
    private MonTotRepository repository;

    public List<MonTotDTO> getAllMonTots() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<MonTotDTO> getByAccNbrAndBillCycle(String accNbr, String billCycle) {
        MonTotId id = new MonTotId();
        id.setAccNbr(accNbr);
        id.setBillCycle(billCycle);
        return repository.findById(id).map(this::convertToDTO);
    }

    public List<MonTotDTO> getByAccNbr(String accNbr) {
        return repository.findByAccNbr(accNbr).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MonTotDTO> getByAreaCodeAndBillCycle(String areaCode, String billCycle) {
        return repository.findByAreaCodeAndBillCycle(areaCode, billCycle).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // MonTotService.java - Add this method to the class

    public MonTotDTO updateMonTot(String accNbr, String billCycle, MonTotDTO updatedDto) {
        MonTotId id = new MonTotId();
        id.setAccNbr(accNbr);
        id.setBillCycle(billCycle);
        Optional<MonTot> optional = repository.findById(id);
        if (!optional.isPresent()) {
            throw new RuntimeException("MonTot not found for accNbr: " + accNbr + ", billCycle: " + billCycle);
        }
        MonTot entity = optional.get();
        updateMonTotFields(entity, updatedDto);
        entity.setEditedUserId(updatedDto.getEditedUserId() != null ? updatedDto.getEditedUserId() : "SYSTEM");
        entity.setEditedDtime(new Timestamp(System.currentTimeMillis()));
        MonTot saved = repository.save(entity);
        return convertToDTO(saved);
    }

    private void updateMonTotFields(MonTot entity, MonTotDTO dto) {
        if (dto.getErrStat() != null) entity.setErrStat(dto.getErrStat());
        if (dto.getBfBal() != null) entity.setBfBal(dto.getBfBal());
        if (dto.getTotUntsKwo() != null) entity.setTotUntsKwo(dto.getTotUntsKwo());
        if (dto.getTotUntsKwd() != null) entity.setTotUntsKwd(dto.getTotUntsKwd());
        if (dto.getTotUntsKwp() != null) entity.setTotUntsKwp(dto.getTotUntsKwp());
        if (dto.getTotKva() != null) entity.setTotKva(dto.getTotKva());
        if (dto.getTotKwoChg() != null) entity.setTotKwoChg(dto.getTotKwoChg());
        if (dto.getTotKwdChg() != null) entity.setTotKwdChg(dto.getTotKwdChg());
        if (dto.getTotKwpChg() != null) entity.setTotKwpChg(dto.getTotKwpChg());
        if (dto.getTotKvaChg() != null) entity.setTotKvaChg(dto.getTotKvaChg());
        if (dto.getTotCharge() != null) entity.setTotCharge(dto.getTotCharge());
        if (dto.getFixedChg() != null) entity.setFixedChg(dto.getFixedChg());
        if (dto.getTotGst() != null) entity.setTotGst(dto.getTotGst());
        if (dto.getTotAmt() != null) entity.setTotAmt(dto.getTotAmt());
        if (dto.getDebtTot() != null) entity.setDebtTot(dto.getDebtTot());
        if (dto.getCrdtTot() != null) entity.setCrdtTot(dto.getCrdtTot());
        if (dto.getPayTot() != null) entity.setPayTot(dto.getPayTot());
        if (dto.getCrntBal() != null) entity.setCrntBal(dto.getCrntBal());
        if (dto.getKwoAvg3() != null) entity.setKwoAvg3(dto.getKwoAvg3());
        if (dto.getKwoAvg6() != null) entity.setKwoAvg6(dto.getKwoAvg6());
        if (dto.getKwoAvg12() != null) entity.setKwoAvg12(dto.getKwoAvg12());
        if (dto.getKwdAvg3() != null) entity.setKwdAvg3(dto.getKwdAvg3());
        if (dto.getKwdAvg6() != null) entity.setKwdAvg6(dto.getKwdAvg6());
        if (dto.getKwdAvg12() != null) entity.setKwdAvg12(dto.getKwdAvg12());
        if (dto.getKwpAvg3() != null) entity.setKwpAvg3(dto.getKwpAvg3());
        if (dto.getKwpAvg6() != null) entity.setKwpAvg6(dto.getKwpAvg6());
        if (dto.getKwpAvg12() != null) entity.setKwpAvg12(dto.getKwpAvg12());
        if (dto.getKvaAvg3() != null) entity.setKvaAvg3(dto.getKvaAvg3());
        if (dto.getKvaAvg6() != null) entity.setKvaAvg6(dto.getKvaAvg6());
        if (dto.getKvaAvg12() != null) entity.setKvaAvg12(dto.getKvaAvg12());
        if (dto.getTotBillStat() != null) entity.setTotBillStat(dto.getTotBillStat());
        if (dto.getUserId() != null) entity.setUserId(dto.getUserId());
        if (dto.getEnteredDtime() != null) entity.setEnteredDtime(dto.getEnteredDtime());
        if (dto.getEditedUserId() != null) entity.setEditedUserId(dto.getEditedUserId());
        if (dto.getEditedDtime() != null) entity.setEditedDtime(dto.getEditedDtime());
    }

    private MonTotDTO convertToDTO(MonTot entity) {
        MonTotDTO dto = new MonTotDTO();
        dto.setAccNbr(entity.getAccNbr());
        dto.setBillCycle(entity.getBillCycle());
        dto.setErrStat(entity.getErrStat());
        dto.setBfBal(entity.getBfBal());
        dto.setTotUntsKwo(entity.getTotUntsKwo());
        dto.setTotUntsKwd(entity.getTotUntsKwd());
        dto.setTotUntsKwp(entity.getTotUntsKwp());
        dto.setTotKva(entity.getTotKva());
        dto.setTotKwoChg(entity.getTotKwoChg());
        dto.setTotKwdChg(entity.getTotKwdChg());
        dto.setTotKwpChg(entity.getTotKwpChg());
        dto.setTotKvaChg(entity.getTotKvaChg());
        dto.setTotCharge(entity.getTotCharge());
        dto.setFixedChg(entity.getFixedChg());
        dto.setTotGst(entity.getTotGst());
        dto.setTotAmt(entity.getTotAmt());
        dto.setDebtTot(entity.getDebtTot());
        dto.setCrdtTot(entity.getCrdtTot());
        dto.setPayTot(entity.getPayTot());
        dto.setCrntBal(entity.getCrntBal());
        dto.setKwoAvg3(entity.getKwoAvg3());
        dto.setKwoAvg6(entity.getKwoAvg6());
        dto.setKwoAvg12(entity.getKwoAvg12());
        dto.setKwdAvg3(entity.getKwdAvg3());
        dto.setKwdAvg6(entity.getKwdAvg6());
        dto.setKwdAvg12(entity.getKwdAvg12());
        dto.setKwpAvg3(entity.getKwpAvg3());
        dto.setKwpAvg6(entity.getKwpAvg6());
        dto.setKwpAvg12(entity.getKwpAvg12());
        dto.setKvaAvg3(entity.getKvaAvg3());
        dto.setKvaAvg6(entity.getKvaAvg6());
        dto.setKvaAvg12(entity.getKvaAvg12());
        dto.setTotBillStat(entity.getTotBillStat());
        dto.setUserId(entity.getUserId());
        dto.setEnteredDtime(entity.getEnteredDtime());
        dto.setEditedUserId(entity.getEditedUserId());
        dto.setEditedDtime(entity.getEditedDtime());
        return dto;
    }
}