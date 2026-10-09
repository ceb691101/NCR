package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.MeterDetailsDTO;
import com.example.SPSProjectBackend.model.MeterDetails;
import com.example.SPSProjectBackend.repository.MeterDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeterDetailsService {

    @Autowired
    private MeterDetailsRepository meterDetailsRepository;

    private MeterDetailsDTO convertToDTO(MeterDetails meterDetails) {
        if (meterDetails == null) {
            return null;
        }

        MeterDetailsDTO dto = new MeterDetailsDTO();
        // Map all fields from entity to DTO
        dto.setJobNumber(meterDetails.getJobNumber()); // Now getting from regular field
        dto.setAccountNumber(meterDetails.getAccountNumber()); // Now getting from ID field
        dto.setAreaCode(meterDetails.getAreaCode());
        dto.setAddedBillingCycle(meterDetails.getAddedBillingCycle());
        dto.setMeterSequence(meterDetails.getMeterSequence());
        dto.setSetType(meterDetails.getSetType());
        dto.setMeterOrder(meterDetails.getMeterOrder());
        dto.setMeterType(meterDetails.getMeterType());
        dto.setNumberOfPhases(meterDetails.getNumberOfPhases());
        dto.setNumberOfMeterSets(meterDetails.getNumberOfMeterSets());
        dto.setMeter1Set(meterDetails.getMeter1Set());
        dto.setMeter2Set(meterDetails.getMeter2Set());
        dto.setMeter3Set(meterDetails.getMeter3Set());
        dto.setMeterNumber(meterDetails.getMeterNumber());
        dto.setPresentReading(meterDetails.getPresentReading());
        dto.setMultiplicationFactor(meterDetails.getMultiplicationFactor());
        dto.setCtRatio(meterDetails.getCtRatio());
        dto.setMeterRatio(meterDetails.getMeterRatio());
        dto.setEffectiveBillingCycle(meterDetails.getEffectiveBillingCycle());
        dto.setEffectiveDate(meterDetails.getEffectiveDate());
        dto.setTrCb(meterDetails.getTrCb());
        dto.setDpCode(meterDetails.getDpCode());
        dto.setBrCode(meterDetails.getBrCode());
        dto.setConnectionTransformerPanel(meterDetails.getConnectionTransformerPanel());
        dto.setTransformerVoltage(meterDetails.getTransformerVoltage());
        dto.setTransformerAmps(meterDetails.getTransformerAmps());
        dto.setUserId(meterDetails.getUserId());
        dto.setEnteredDateTime(meterDetails.getEnteredDateTime());
        dto.setEditedUserId(meterDetails.getEditedUserId());
        dto.setEditedDateTime(meterDetails.getEditedDateTime());
        return dto;
    }

    public List<MeterDetailsDTO> getAllMeterDetails() {
        return meterDetailsRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Changed to return List<MeterDetailsDTO>
    public List<MeterDetailsDTO> getMeterDetailsByJobNumber(String jobNumber) {
        // KEY CHANGE: Use findByJobNumber which now returns List
        List<MeterDetails> meterDetailsList = meterDetailsRepository.findByJobNumber(jobNumber);
        if (meterDetailsList.isEmpty()) {
            return null;
        }
        return meterDetailsList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MeterDetailsDTO saveMeterDetails(MeterDetailsDTO dto) {
        MeterDetails meterDetails = convertToEntity(dto);
        return convertToDTO(meterDetailsRepository.save(meterDetails));
    }

    public void deleteMeterDetails(String accountNumber) {
        // Changed parameter from jobNumber to accountNumber
        meterDetailsRepository.deleteById(accountNumber);
    }

    private MeterDetails convertToEntity(MeterDetailsDTO dto) {
        MeterDetails meterDetails = new MeterDetails();
        // Map all fields from DTO to entity
        meterDetails.setAccountNumber(dto.getAccountNumber()); // Set as ID
        meterDetails.setJobNumber(dto.getJobNumber()); // Set as regular field
        meterDetails.setAreaCode(dto.getAreaCode());
        meterDetails.setAddedBillingCycle(dto.getAddedBillingCycle());
        meterDetails.setMeterSequence(dto.getMeterSequence());
        meterDetails.setSetType(dto.getSetType());
        meterDetails.setMeterOrder(dto.getMeterOrder());
        meterDetails.setMeterType(dto.getMeterType());
        meterDetails.setNumberOfPhases(dto.getNumberOfPhases());
        meterDetails.setNumberOfMeterSets(dto.getNumberOfMeterSets());
        meterDetails.setMeter1Set(dto.getMeter1Set());
        meterDetails.setMeter2Set(dto.getMeter2Set());
        meterDetails.setMeter3Set(dto.getMeter3Set());
        meterDetails.setMeterNumber(dto.getMeterNumber());
        meterDetails.setPresentReading(dto.getPresentReading());
        meterDetails.setMultiplicationFactor(dto.getMultiplicationFactor());
        meterDetails.setCtRatio(dto.getCtRatio());
        meterDetails.setMeterRatio(dto.getMeterRatio());
        meterDetails.setEffectiveBillingCycle(dto.getEffectiveBillingCycle());
        meterDetails.setEffectiveDate(dto.getEffectiveDate());
        meterDetails.setTrCb(dto.getTrCb());
        meterDetails.setDpCode(dto.getDpCode());
        meterDetails.setBrCode(dto.getBrCode());
        meterDetails.setConnectionTransformerPanel(dto.getConnectionTransformerPanel());
        meterDetails.setTransformerVoltage(dto.getTransformerVoltage());
        meterDetails.setTransformerAmps(dto.getTransformerAmps());
        meterDetails.setUserId(dto.getUserId());
        meterDetails.setEnteredDateTime(dto.getEnteredDateTime());
        meterDetails.setEditedUserId(dto.getEditedUserId());
        meterDetails.setEditedDateTime(dto.getEditedDateTime());
        return meterDetails;
    }
}