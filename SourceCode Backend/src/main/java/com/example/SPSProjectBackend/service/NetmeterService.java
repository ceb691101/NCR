package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.NetmeterDTO;
import com.example.SPSProjectBackend.model.Netmeter;
import com.example.SPSProjectBackend.repository.NetmeterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NetmeterService {
    @Autowired
    private NetmeterRepository repository;

    public List<NetmeterDTO> getAllNetmeters() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<NetmeterDTO> getNetmeterByAccNbr(String accNbr) {
        return repository.findById(accNbr)
                .map(this::convertToDTO);
    }

    // NetmeterService.java - Add this method to the class
    public NetmeterDTO updateNetmeter(String accNbr, NetmeterDTO updatedDto) {
        Optional<Netmeter> optional = repository.findById(accNbr);
        if (!optional.isPresent()) {
            throw new RuntimeException("Netmeter not found for accNbr: " + accNbr);
        }
        Netmeter entity = optional.get();
        updateNetmeterFields(entity, updatedDto);
        Netmeter saved = repository.save(entity);
        return convertToDTO(saved);
    }

    private void updateNetmeterFields(Netmeter entity, NetmeterDTO dto) {
        if (dto.getBfUnits() != null) entity.setBfUnits(dto.getBfUnits());
        if (dto.getAvgImp() != null) entity.setAvgImp(dto.getAvgImp());
        if (dto.getAvgExp() != null) entity.setAvgExp(dto.getAvgExp());
        if (dto.getNetType() != null) entity.setNetType(dto.getNetType());
        if (dto.getAgrmntDate() != null) entity.setAgrmntDate(new java.sql.Date(dto.getAgrmntDate().getTime()));  // Convert util.Date to sql.Date
        if (dto.getRate1() != null) entity.setRate1(dto.getRate1());
        if (dto.getRate2() != null) entity.setRate2(dto.getRate2());
        if (dto.getPeriod1() != null) entity.setPeriod1(dto.getPeriod1());
        if (dto.getPeriod2() != null) entity.setPeriod2(dto.getPeriod2());
        if (dto.getBankCode() != null) entity.setBankCode(dto.getBankCode());
        if (dto.getBranCode() != null) entity.setBranCode(dto.getBranCode());
        if (dto.getBkAcNo() != null) entity.setBkAcNo(dto.getBkAcNo());
        if (dto.getGenCap() != null) entity.setGenCap(dto.getGenCap());
        if (dto.getEnteredDtime() != null) entity.setEnteredDtime(dto.getEnteredDtime());
        if (dto.getAddedBlcy() != null) entity.setAddedBlcy(dto.getAddedBlcy());
        if (dto.getSetoff() != null) entity.setSetoff(dto.getSetoff());
        if (dto.getSchm() != null) entity.setSchm(dto.getSchm());
        if (dto.getTotReten() != null) entity.setTotReten(dto.getTotReten());
        if (dto.getRate3() != null) entity.setRate3(dto.getRate3());
    }

    private NetmeterDTO convertToDTO(Netmeter entity) {
        NetmeterDTO dto = new NetmeterDTO();
        dto.setAccNbr(entity.getAccNbr());
        dto.setBfUnits(entity.getBfUnits());
        dto.setAvgImp(entity.getAvgImp());
        dto.setAvgExp(entity.getAvgExp());
        dto.setNetType(entity.getNetType());
        dto.setAgrmntDate(entity.getAgrmntDate()); // Direct set
        dto.setRate1(entity.getRate1());
        dto.setRate2(entity.getRate2());
        dto.setPeriod1(entity.getPeriod1());
        dto.setPeriod2(entity.getPeriod2());
        dto.setBankCode(entity.getBankCode());
        dto.setBranCode(entity.getBranCode());
        dto.setBkAcNo(entity.getBkAcNo());
        dto.setGenCap(entity.getGenCap());
        dto.setEnteredDtime(entity.getEnteredDtime());
        dto.setAddedBlcy(entity.getAddedBlcy());
        dto.setSetoff(entity.getSetoff());
        dto.setSchm(entity.getSchm());
        dto.setTotReten(entity.getTotReten());
        dto.setRate3(entity.getRate3());
        return dto;
    }
}