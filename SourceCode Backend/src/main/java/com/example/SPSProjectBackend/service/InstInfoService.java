package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.InstInfoDTO;
import com.example.SPSProjectBackend.model.InstInfo;
import com.example.SPSProjectBackend.repository.InstInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InstInfoService {
    @Autowired
    private InstInfoRepository repository;

    public List<InstInfoDTO> getAllInstInfo() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<InstInfoDTO> getByInstId(String instId) {
        // Use native query and trim for safety against whitespace/dialect issues
        return repository.findByInstIdNative(instId.trim())
                .map(this::convertToDTO);
    }

    // InstInfoService.java - Add this method to the class
    public InstInfoDTO updateInstInfo(String instId, InstInfoDTO updatedDto) {
        Optional<InstInfo> optional = repository.findById(instId.trim());
        if (!optional.isPresent()) {
            throw new RuntimeException("InstInfo not found for instId: " + instId);
        }
        InstInfo entity = optional.get();
        updateInstInfoFields(entity, updatedDto);
        entity.setEditedUserId(updatedDto.getEditedUserId() != null ? updatedDto.getEditedUserId() : "SYSTEM");
        entity.setEditedDtime(new Timestamp(System.currentTimeMillis()));
        InstInfo saved = repository.save(entity);
        return convertToDTO(saved);
    }

    private void updateInstInfoFields(InstInfo entity, InstInfoDTO dto) {
        if (dto.getDateCnnct() != null) entity.setDateCnnct(new java.sql.Date(dto.getDateCnnct().getTime()));
        if (dto.getMtrSet() != null) entity.setMtrSet(dto.getMtrSet());
        if (dto.getNbrMet() != null) entity.setNbrMet(dto.getNbrMet());
        if (dto.getCustNum() != null) entity.setCustNum(dto.getCustNum());
        if (dto.getTypeMet() != null) entity.setTypeMet(dto.getTypeMet());
        if (dto.getDpCode() != null) entity.setDpCode(dto.getDpCode());
        if (dto.getTrCb() != null) entity.setTrCb(dto.getTrCb());
        if (dto.getCnnctTrpnl() != null) entity.setCnnctTrpnl(dto.getCnnctTrpnl());
        if (dto.getTrpnlVolt() != null) entity.setTrpnlVolt(dto.getTrpnlVolt());
        if (dto.getTrpnlAmps() != null) entity.setTrpnlAmps(dto.getTrpnlAmps());
        if (dto.getUserId() != null) entity.setUserId(dto.getUserId());
        if (dto.getEnteredDtime() != null) entity.setEnteredDtime(dto.getEnteredDtime());
        if (dto.getEditedUserId() != null) entity.setEditedUserId(dto.getEditedUserId());
        if (dto.getEditedDtime() != null) entity.setEditedDtime(dto.getEditedDtime());
    }

    private InstInfoDTO convertToDTO(InstInfo entity) {
        InstInfoDTO dto = new InstInfoDTO();
        dto.setInstId(entity.getInstId());
        dto.setDateCnnct(entity.getDateCnnct()); // Direct set
        dto.setMtrSet(entity.getMtrSet());
        dto.setNbrMet(entity.getNbrMet());
        dto.setCustNum(entity.getCustNum());
        dto.setTypeMet(entity.getTypeMet());
        dto.setDpCode(entity.getDpCode());
        dto.setTrCb(entity.getTrCb());
        dto.setCnnctTrpnl(entity.getCnnctTrpnl());
        dto.setTrpnlVolt(entity.getTrpnlVolt());
        dto.setTrpnlAmps(entity.getTrpnlAmps());
        dto.setUserId(entity.getUserId());
        dto.setEnteredDtime(entity.getEnteredDtime());
        dto.setEditedUserId(entity.getEditedUserId());
        dto.setEditedDtime(entity.getEditedDtime());
        return dto;
    }
}