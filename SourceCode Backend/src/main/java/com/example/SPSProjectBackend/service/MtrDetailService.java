package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.MtrDetailDTO;
import com.example.SPSProjectBackend.model.MtrDetail;
import com.example.SPSProjectBackend.repository.MtrDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MtrDetailService {
    @Autowired
    private MtrDetailRepository repository;

    public List<MtrDetailDTO> getAllMtrDetails() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<MtrDetailDTO> getByInstId(String instId) {
        List<MtrDetail> details = repository.findByInstId(instId);
        if (details.isEmpty()) {
            return Optional.empty();
        }
        // Assuming one record or taking the first; adjust if multiple are expected
        return Optional.of(convertToDTO(details.get(0)));
    }

    public List<MtrDetailDTO> getAllByInstId(String instId) {
        return repository.findByInstId(instId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MtrDetailDTO updateMtrDetail(String instId, MtrDetailDTO updatedDto) {
        List<MtrDetail> details = repository.findByInstId(instId);
        if (details.isEmpty()) {
            throw new RuntimeException("MtrDetail not found for instId: " + instId);
        }
        MtrDetail entity = details.get(0); // Assuming first one
        updateMtrDetailFields(entity, updatedDto);
        entity.setEditedUserid(updatedDto.getEditedUserid() != null ? updatedDto.getEditedUserid() : "SYSTEM");
        entity.setEditedDtime(new Timestamp(System.currentTimeMillis()));
        MtrDetail saved = repository.save(entity);
        return convertToDTO(saved);
    }

    private void updateMtrDetailFields(MtrDetail entity, MtrDetailDTO dto) {
        if (dto.getAddedBlcy() != null) entity.setAddedBlcy(dto.getAddedBlcy());
        if (dto.getMtr1set() != null) entity.setMtr1set(dto.getMtr1set());
        if (dto.getMtr2set() != null) entity.setMtr2set(dto.getMtr2set());
        if (dto.getMtr3set() != null) entity.setMtr3set(dto.getMtr3set());
        if (dto.getMtrSeq() != null) entity.setMtrSeq(dto.getMtrSeq());
        if (dto.getMtrsetType() != null) entity.setMtrsetType(dto.getMtrsetType());
        if (dto.getMtrOrder() != null) entity.setMtrOrder(dto.getMtrOrder());
        if (dto.getMtrType() != null) entity.setMtrType(dto.getMtrType());
        if (dto.getNoOfPhases() != null) entity.setNoOfPhases(dto.getNoOfPhases());
        if (dto.getMtrNbr() != null) entity.setMtrNbr(dto.getMtrNbr());
        if (dto.getPrsntRdn() != null) entity.setPrsntRdn(dto.getPrsntRdn());
        if (dto.getCtRatio() != null) entity.setCtRatio(dto.getCtRatio());
        if (dto.getMtrRatio() != null) entity.setMtrRatio(dto.getMtrRatio());
        if (dto.getMFactor() != null) entity.setMFactor(dto.getMFactor());
        if (dto.getEffctBlcy() != null) entity.setEffctBlcy(dto.getEffctBlcy());
        if (dto.getEffctDate() != null) entity.setEffctDate(dto.getEffctDate());
        if (dto.getAvgCnsp3() != null) entity.setAvgCnsp3(dto.getAvgCnsp3());
        if (dto.getAvgCnsp6() != null) entity.setAvgCnsp6(dto.getAvgCnsp6());
        if (dto.getAvgCnsp12() != null) entity.setAvgCnsp12(dto.getAvgCnsp12());
        if (dto.getBrCode() != null) entity.setBrCode(dto.getBrCode());
        if (dto.getUserId() != null) entity.setUserId(dto.getUserId());
        if (dto.getEnteredDtime() != null) entity.setEnteredDtime(dto.getEnteredDtime());
        if (dto.getEditedUserid() != null) entity.setEditedUserid(dto.getEditedUserid());
        if (dto.getEditedDtime() != null) entity.setEditedDtime(dto.getEditedDtime());
    }

    private MtrDetailDTO convertToDTO(MtrDetail entity) {
        MtrDetailDTO dto = new MtrDetailDTO();
        dto.setInstId(entity.getInstId());
        dto.setAddedBlcy(entity.getAddedBlcy());
        dto.setMtr1set(entity.getMtr1set());
        dto.setMtr2set(entity.getMtr2set());
        dto.setMtr3set(entity.getMtr3set());
        dto.setMtrSeq(entity.getMtrSeq());
        dto.setMtrsetType(entity.getMtrsetType());
        dto.setMtrOrder(entity.getMtrOrder());
        dto.setMtrType(entity.getMtrType());
        dto.setNoOfPhases(entity.getNoOfPhases());
        dto.setMtrNbr(entity.getMtrNbr());
        dto.setPrsntRdn(entity.getPrsntRdn());
        dto.setCtRatio(entity.getCtRatio());
        dto.setMtrRatio(entity.getMtrRatio());
        dto.setMFactor(entity.getMFactor());
        dto.setEffctBlcy(entity.getEffctBlcy());
        dto.setEffctDate(entity.getEffctDate());  // Direct set, no new java.sql.Date
        dto.setAvgCnsp3(entity.getAvgCnsp3());
        dto.setAvgCnsp6(entity.getAvgCnsp6());
        dto.setAvgCnsp12(entity.getAvgCnsp12());
        dto.setBrCode(entity.getBrCode());
        dto.setUserId(entity.getUserId());
        dto.setEnteredDtime(entity.getEnteredDtime() == null ? null : new java.sql.Timestamp(entity.getEnteredDtime().getTime()));
        dto.setEditedUserid(entity.getEditedUserid());
        dto.setEditedDtime(entity.getEditedDtime() == null ? null : new java.sql.Timestamp(entity.getEditedDtime().getTime()));
        return dto;
    }
}