package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.TariffDTO;
import com.example.SPSProjectBackend.model.TmpTariff;
import com.example.SPSProjectBackend.repository.TmpTariffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class TmpTariffService {

    @Autowired
    private TmpTariffRepository tmpTariffRepository;

    // Standard tariff IDs to create when ending all tariffs
    private static final Short[] STANDARD_TARIFF_IDS = {
        11, 13, 14, 15,     // Domestic
        21, 22,             // Religious
        31, 32, 33, 34,     // Industrial
        41, 42,             // Hotel
        51,                 // General Purpose
        61,                 // Government
        71, 72, 73,         // Agriculture
        93                  // EV Charging
    };

    // ================================================================
    // CONVERT ENTITY TO DTO
    // ================================================================
    private TariffDTO convertToDTO(TmpTariff tmpTariff) {
        TariffDTO dto = new TariffDTO();
        dto.setTariff(tmpTariff.getTariff());
        dto.setRateStatus(tmpTariff.getRateStatus());
        dto.setRecordStatus(tmpTariff.getRecordStatus());
        dto.setFromDate(tmpTariff.getFromDate());
        dto.setToDate(tmpTariff.getToDate());
        dto.setMinCharge(tmpTariff.getMinCharge());
        dto.setNumberOfSlabs(tmpTariff.getNumberOfSlabs());

        // Slab 1
        dto.setLimit1(tmpTariff.getLimit1());
        dto.setRate1(tmpTariff.getRate1());
        dto.setFixedCharge1(tmpTariff.getFixedCharge1());
        dto.setFuelCharge1(tmpTariff.getFuelCharge1());
        dto.setServiceCharge1(tmpTariff.getServiceCharge1());
        dto.setFuelStatus1(tmpTariff.getFuelStatus1());
        dto.setDiscount1(tmpTariff.getDiscount1());

        // Slab 2
        dto.setLimit2(tmpTariff.getLimit2());
        dto.setRate2(tmpTariff.getRate2());
        dto.setFixedCharge2(tmpTariff.getFixedCharge2());
        dto.setFuelCharge2(tmpTariff.getFuelCharge2());
        dto.setServiceCharge2(tmpTariff.getServiceCharge2());
        dto.setFuelStatus2(tmpTariff.getFuelStatus2());
        dto.setDiscount2(tmpTariff.getDiscount2());

        // Slab 3
        dto.setLimit3(tmpTariff.getLimit3());
        dto.setRate3(tmpTariff.getRate3());
        dto.setFixedCharge3(tmpTariff.getFixedCharge3());
        dto.setFuelCharge3(tmpTariff.getFuelCharge3());
        dto.setServiceCharge3(tmpTariff.getServiceCharge3());
        dto.setFuelStatus3(tmpTariff.getFuelStatus3());
        dto.setDiscount3(tmpTariff.getDiscount3());

        // Slab 4
        dto.setLimit4(tmpTariff.getLimit4());
        dto.setRate4(tmpTariff.getRate4());
        dto.setFixedCharge4(tmpTariff.getFixedCharge4());
        dto.setFuelCharge4(tmpTariff.getFuelCharge4());
        dto.setServiceCharge4(tmpTariff.getServiceCharge4());
        dto.setFuelStatus4(tmpTariff.getFuelStatus4());
        dto.setDiscount4(tmpTariff.getDiscount4());

        // Slab 5
        dto.setLimit5(tmpTariff.getLimit5());
        dto.setRate5(tmpTariff.getRate5());
        dto.setFixedCharge5(tmpTariff.getFixedCharge5());
        dto.setFuelCharge5(tmpTariff.getFuelCharge5());
        dto.setServiceCharge5(tmpTariff.getServiceCharge5());
        dto.setFuelStatus5(tmpTariff.getFuelStatus5());
        dto.setDiscount5(tmpTariff.getDiscount5());

        // Slab 6
        dto.setLimit6(tmpTariff.getLimit6());
        dto.setRate6(tmpTariff.getRate6());
        dto.setFixedCharge6(tmpTariff.getFixedCharge6());
        dto.setFuelCharge6(tmpTariff.getFuelCharge6());
        dto.setServiceCharge6(tmpTariff.getServiceCharge6());
        dto.setFuelStatus6(tmpTariff.getFuelStatus6());
        dto.setDiscount6(tmpTariff.getDiscount6());

        // Slab 7
        dto.setLimit7(tmpTariff.getLimit7());
        dto.setRate7(tmpTariff.getRate7());
        dto.setFixedCharge7(tmpTariff.getFixedCharge7());
        dto.setFuelCharge7(tmpTariff.getFuelCharge7());
        dto.setServiceCharge7(tmpTariff.getServiceCharge7());
        dto.setFuelStatus7(tmpTariff.getFuelStatus7());
        dto.setDiscount7(tmpTariff.getDiscount7());

        // Slab 8
        dto.setLimit8(tmpTariff.getLimit8());
        dto.setRate8(tmpTariff.getRate8());
        dto.setFixedCharge8(tmpTariff.getFixedCharge8());
        dto.setFuelCharge8(tmpTariff.getFuelCharge8());
        dto.setServiceCharge8(tmpTariff.getServiceCharge8());
        dto.setFuelStatus8(tmpTariff.getFuelStatus8());
        dto.setDiscount8(tmpTariff.getDiscount8());

        // Slab 9
        dto.setLimit9(tmpTariff.getLimit9());
        dto.setRate9(tmpTariff.getRate9());
        dto.setFixedCharge9(tmpTariff.getFixedCharge9());
        dto.setFuelCharge9(tmpTariff.getFuelCharge9());
        dto.setServiceCharge9(tmpTariff.getServiceCharge9());
        dto.setFuelStatus9(tmpTariff.getFuelStatus9());
        dto.setDiscount9(tmpTariff.getDiscount9());

        return dto;
    }

    // ================================================================
    // CONVERT DTO TO ENTITY
    // ================================================================
    private TmpTariff convertToEntity(TariffDTO dto) {
        TmpTariff tmpTariff = new TmpTariff();
        tmpTariff.setTariff(dto.getTariff());
        tmpTariff.setRateStatus(dto.getRateStatus());
        tmpTariff.setRecordStatus(dto.getRecordStatus());
        tmpTariff.setFromDate(dto.getFromDate());
        tmpTariff.setToDate(dto.getToDate());
        tmpTariff.setMinCharge(dto.getMinCharge());
        tmpTariff.setNumberOfSlabs(dto.getNumberOfSlabs());

        // Slab 1
        tmpTariff.setLimit1(dto.getLimit1());
        tmpTariff.setRate1(dto.getRate1());
        tmpTariff.setFixedCharge1(dto.getFixedCharge1());
        tmpTariff.setFuelCharge1(dto.getFuelCharge1());
        tmpTariff.setServiceCharge1(dto.getServiceCharge1());
        tmpTariff.setFuelStatus1(dto.getFuelStatus1());
        tmpTariff.setDiscount1(dto.getDiscount1());

        // Slab 2
        tmpTariff.setLimit2(dto.getLimit2());
        tmpTariff.setRate2(dto.getRate2());
        tmpTariff.setFixedCharge2(dto.getFixedCharge2());
        tmpTariff.setFuelCharge2(dto.getFuelCharge2());
        tmpTariff.setServiceCharge2(dto.getServiceCharge2());
        tmpTariff.setFuelStatus2(dto.getFuelStatus2());
        tmpTariff.setDiscount2(dto.getDiscount2());

        // Slab 3
        tmpTariff.setLimit3(dto.getLimit3());
        tmpTariff.setRate3(dto.getRate3());
        tmpTariff.setFixedCharge3(dto.getFixedCharge3());
        tmpTariff.setFuelCharge3(dto.getFuelCharge3());
        tmpTariff.setServiceCharge3(dto.getServiceCharge3());
        tmpTariff.setFuelStatus3(dto.getFuelStatus3());
        tmpTariff.setDiscount3(dto.getDiscount3());

        // Slab 4
        tmpTariff.setLimit4(dto.getLimit4());
        tmpTariff.setRate4(dto.getRate4());
        tmpTariff.setFixedCharge4(dto.getFixedCharge4());
        tmpTariff.setFuelCharge4(dto.getFuelCharge4());
        tmpTariff.setServiceCharge4(dto.getServiceCharge4());
        tmpTariff.setFuelStatus4(dto.getFuelStatus4());
        tmpTariff.setDiscount4(dto.getDiscount4());

        // Slab 5
        tmpTariff.setLimit5(dto.getLimit5());
        tmpTariff.setRate5(dto.getRate5());
        tmpTariff.setFixedCharge5(dto.getFixedCharge5());
        tmpTariff.setFuelCharge5(dto.getFuelCharge5());
        tmpTariff.setServiceCharge5(dto.getServiceCharge5());
        tmpTariff.setFuelStatus5(dto.getFuelStatus5());
        tmpTariff.setDiscount5(dto.getDiscount5());

        // Slab 6
        tmpTariff.setLimit6(dto.getLimit6());
        tmpTariff.setRate6(dto.getRate6());
        tmpTariff.setFixedCharge6(dto.getFixedCharge6());
        tmpTariff.setFuelCharge6(dto.getFuelCharge6());
        tmpTariff.setServiceCharge6(dto.getServiceCharge6());
        tmpTariff.setFuelStatus6(dto.getFuelStatus6());
        tmpTariff.setDiscount6(dto.getDiscount6());

        // Slab 7
        tmpTariff.setLimit7(dto.getLimit7());
        tmpTariff.setRate7(dto.getRate7());
        tmpTariff.setFixedCharge7(dto.getFixedCharge7());
        tmpTariff.setFuelCharge7(dto.getFuelCharge7());
        tmpTariff.setServiceCharge7(dto.getServiceCharge7());
        tmpTariff.setFuelStatus7(dto.getFuelStatus7());
        tmpTariff.setDiscount7(dto.getDiscount7());

        // Slab 8
        tmpTariff.setLimit8(dto.getLimit8());
        tmpTariff.setRate8(dto.getRate8());
        tmpTariff.setFixedCharge8(dto.getFixedCharge8());
        tmpTariff.setFuelCharge8(dto.getFuelCharge8());
        tmpTariff.setServiceCharge8(dto.getServiceCharge8());
        tmpTariff.setFuelStatus8(dto.getFuelStatus8());
        tmpTariff.setDiscount8(dto.getDiscount8());

        // Slab 9
        tmpTariff.setLimit9(dto.getLimit9());
        tmpTariff.setRate9(dto.getRate9());
        tmpTariff.setFixedCharge9(dto.getFixedCharge9());
        tmpTariff.setFuelCharge9(dto.getFuelCharge9());
        tmpTariff.setServiceCharge9(dto.getServiceCharge9());
        tmpTariff.setFuelStatus9(dto.getFuelStatus9());
        tmpTariff.setDiscount9(dto.getDiscount9());

        return tmpTariff;
    }

    // ================================================================
    // BASIC CRUD OPERATIONS
    // ================================================================
    
    // Get all tmp tariffs
    public List<TariffDTO> getAllTmpTariffs() {
        List<TmpTariff> tariffs = tmpTariffRepository.findAll();
        // Sort by tariff ID in ascending order
        tariffs.sort(Comparator.comparing(TmpTariff::getTariff));
        return tariffs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get active tmp tariffs only (to_date = NULL)
    public List<TariffDTO> getActiveTmpTariffs() {
        List<TmpTariff> tariffs = tmpTariffRepository.findByToDateIsNull();
        return tariffs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get ended tmp tariffs only (to_date != NULL) for archive
    public List<TariffDTO> getEndedTmpTariffs() {
        List<TmpTariff> tariffs = tmpTariffRepository.findByToDateIsNotNull();
        return tariffs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get tmp tariff by ID
    public Optional<TariffDTO> getTmpTariffById(Short tariffId) {
        Optional<TmpTariff> tariff = tmpTariffRepository.findById(tariffId);
        return tariff.map(this::convertToDTO);
    }

    // Get tmp tariff by tariff number
    public Optional<TariffDTO> getTmpTariffByTariff(Short tariff) {
        Optional<TmpTariff> tariffEntity = tmpTariffRepository.findByTariff(tariff);
        return tariffEntity.map(this::convertToDTO);
    }

    // Save tmp tariff
    public TariffDTO saveTmpTariff(TariffDTO tariffDTO) {
        TmpTariff tmpTariff = convertToEntity(tariffDTO);
        return convertToDTO(tmpTariffRepository.save(tmpTariff));
    }

    // Delete tmp tariff
    public void deleteTmpTariff(Short tariffId) {
        tmpTariffRepository.deleteById(tariffId);
    }

    // Update tmp tariff
    @Transactional
    public TariffDTO updateTmpTariff(Short tariffId, TariffDTO tariffDTO) {
        Optional<TmpTariff> existingTariffOptional = tmpTariffRepository.findById(tariffId);

        if (existingTariffOptional.isPresent()) {
            TmpTariff existingTariff = existingTariffOptional.get();

            // Update only non-null fields
            if (tariffDTO.getRateStatus() != null) {
                existingTariff.setRateStatus(tariffDTO.getRateStatus());
            }
            if (tariffDTO.getRecordStatus() != null) {
                existingTariff.setRecordStatus(tariffDTO.getRecordStatus());
            }
            if (tariffDTO.getFromDate() != null) {
                existingTariff.setFromDate(tariffDTO.getFromDate());
            }
            // Always update toDate (for ending tariffs)
            existingTariff.setToDate(tariffDTO.getToDate());
            
            if (tariffDTO.getMinCharge() != null) {
                existingTariff.setMinCharge(tariffDTO.getMinCharge());
            }
            if (tariffDTO.getNumberOfSlabs() != null) {
                existingTariff.setNumberOfSlabs(tariffDTO.getNumberOfSlabs());
            }

            // Update all slab fields (only if not null)
            // Slab 1
            if (tariffDTO.getLimit1() != null) existingTariff.setLimit1(tariffDTO.getLimit1());
            if (tariffDTO.getRate1() != null) existingTariff.setRate1(tariffDTO.getRate1());
            if (tariffDTO.getFixedCharge1() != null) existingTariff.setFixedCharge1(tariffDTO.getFixedCharge1());
            if (tariffDTO.getFuelCharge1() != null) existingTariff.setFuelCharge1(tariffDTO.getFuelCharge1());
            if (tariffDTO.getServiceCharge1() != null) existingTariff.setServiceCharge1(tariffDTO.getServiceCharge1());
            if (tariffDTO.getFuelStatus1() != null) existingTariff.setFuelStatus1(tariffDTO.getFuelStatus1());
            if (tariffDTO.getDiscount1() != null) existingTariff.setDiscount1(tariffDTO.getDiscount1());

            // Slab 2
            if (tariffDTO.getLimit2() != null) existingTariff.setLimit2(tariffDTO.getLimit2());
            if (tariffDTO.getRate2() != null) existingTariff.setRate2(tariffDTO.getRate2());
            if (tariffDTO.getFixedCharge2() != null) existingTariff.setFixedCharge2(tariffDTO.getFixedCharge2());
            if (tariffDTO.getFuelCharge2() != null) existingTariff.setFuelCharge2(tariffDTO.getFuelCharge2());
            if (tariffDTO.getServiceCharge2() != null) existingTariff.setServiceCharge2(tariffDTO.getServiceCharge2());
            if (tariffDTO.getFuelStatus2() != null) existingTariff.setFuelStatus2(tariffDTO.getFuelStatus2());
            if (tariffDTO.getDiscount2() != null) existingTariff.setDiscount2(tariffDTO.getDiscount2());

            // Slab 3
            if (tariffDTO.getLimit3() != null) existingTariff.setLimit3(tariffDTO.getLimit3());
            if (tariffDTO.getRate3() != null) existingTariff.setRate3(tariffDTO.getRate3());
            if (tariffDTO.getFixedCharge3() != null) existingTariff.setFixedCharge3(tariffDTO.getFixedCharge3());
            if (tariffDTO.getFuelCharge3() != null) existingTariff.setFuelCharge3(tariffDTO.getFuelCharge3());
            if (tariffDTO.getServiceCharge3() != null) existingTariff.setServiceCharge3(tariffDTO.getServiceCharge3());
            if (tariffDTO.getFuelStatus3() != null) existingTariff.setFuelStatus3(tariffDTO.getFuelStatus3());
            if (tariffDTO.getDiscount3() != null) existingTariff.setDiscount3(tariffDTO.getDiscount3());

            // Slab 4
            if (tariffDTO.getLimit4() != null) existingTariff.setLimit4(tariffDTO.getLimit4());
            if (tariffDTO.getRate4() != null) existingTariff.setRate4(tariffDTO.getRate4());
            if (tariffDTO.getFixedCharge4() != null) existingTariff.setFixedCharge4(tariffDTO.getFixedCharge4());
            if (tariffDTO.getFuelCharge4() != null) existingTariff.setFuelCharge4(tariffDTO.getFuelCharge4());
            if (tariffDTO.getServiceCharge4() != null) existingTariff.setServiceCharge4(tariffDTO.getServiceCharge4());
            if (tariffDTO.getFuelStatus4() != null) existingTariff.setFuelStatus4(tariffDTO.getFuelStatus4());
            if (tariffDTO.getDiscount4() != null) existingTariff.setDiscount4(tariffDTO.getDiscount4());

            // Slab 5
            if (tariffDTO.getLimit5() != null) existingTariff.setLimit5(tariffDTO.getLimit5());
            if (tariffDTO.getRate5() != null) existingTariff.setRate5(tariffDTO.getRate5());
            if (tariffDTO.getFixedCharge5() != null) existingTariff.setFixedCharge5(tariffDTO.getFixedCharge5());
            if (tariffDTO.getFuelCharge5() != null) existingTariff.setFuelCharge5(tariffDTO.getFuelCharge5());
            if (tariffDTO.getServiceCharge5() != null) existingTariff.setServiceCharge5(tariffDTO.getServiceCharge5());
            if (tariffDTO.getFuelStatus5() != null) existingTariff.setFuelStatus5(tariffDTO.getFuelStatus5());
            if (tariffDTO.getDiscount5() != null) existingTariff.setDiscount5(tariffDTO.getDiscount5());

            // Slab 6
            if (tariffDTO.getLimit6() != null) existingTariff.setLimit6(tariffDTO.getLimit6());
            if (tariffDTO.getRate6() != null) existingTariff.setRate6(tariffDTO.getRate6());
            if (tariffDTO.getFixedCharge6() != null) existingTariff.setFixedCharge6(tariffDTO.getFixedCharge6());
            if (tariffDTO.getFuelCharge6() != null) existingTariff.setFuelCharge6(tariffDTO.getFuelCharge6());
            if (tariffDTO.getServiceCharge6() != null) existingTariff.setServiceCharge6(tariffDTO.getServiceCharge6());
            if (tariffDTO.getFuelStatus6() != null) existingTariff.setFuelStatus6(tariffDTO.getFuelStatus6());
            if (tariffDTO.getDiscount6() != null) existingTariff.setDiscount6(tariffDTO.getDiscount6());

            // Slab 7
            if (tariffDTO.getLimit7() != null) existingTariff.setLimit7(tariffDTO.getLimit7());
            if (tariffDTO.getRate7() != null) existingTariff.setRate7(tariffDTO.getRate7());
            if (tariffDTO.getFixedCharge7() != null) existingTariff.setFixedCharge7(tariffDTO.getFixedCharge7());
            if (tariffDTO.getFuelCharge7() != null) existingTariff.setFuelCharge7(tariffDTO.getFuelCharge7());
            if (tariffDTO.getServiceCharge7() != null) existingTariff.setServiceCharge7(tariffDTO.getServiceCharge7());
            if (tariffDTO.getFuelStatus7() != null) existingTariff.setFuelStatus7(tariffDTO.getFuelStatus7());
            if (tariffDTO.getDiscount7() != null) existingTariff.setDiscount7(tariffDTO.getDiscount7());

            // Slab 8
            if (tariffDTO.getLimit8() != null) existingTariff.setLimit8(tariffDTO.getLimit8());
            if (tariffDTO.getRate8() != null) existingTariff.setRate8(tariffDTO.getRate8());
            if (tariffDTO.getFixedCharge8() != null) existingTariff.setFixedCharge8(tariffDTO.getFixedCharge8());
            if (tariffDTO.getFuelCharge8() != null) existingTariff.setFuelCharge8(tariffDTO.getFuelCharge8());
            if (tariffDTO.getServiceCharge8() != null) existingTariff.setServiceCharge8(tariffDTO.getServiceCharge8());
            if (tariffDTO.getFuelStatus8() != null) existingTariff.setFuelStatus8(tariffDTO.getFuelStatus8());
            if (tariffDTO.getDiscount8() != null) existingTariff.setDiscount8(tariffDTO.getDiscount8());

            // Slab 9
            if (tariffDTO.getLimit9() != null) existingTariff.setLimit9(tariffDTO.getLimit9());
            if (tariffDTO.getRate9() != null) existingTariff.setRate9(tariffDTO.getRate9());
            if (tariffDTO.getFixedCharge9() != null) existingTariff.setFixedCharge9(tariffDTO.getFixedCharge9());
            if (tariffDTO.getFuelCharge9() != null) existingTariff.setFuelCharge9(tariffDTO.getFuelCharge9());
            if (tariffDTO.getServiceCharge9() != null) existingTariff.setServiceCharge9(tariffDTO.getServiceCharge9());
            if (tariffDTO.getFuelStatus9() != null) existingTariff.setFuelStatus9(tariffDTO.getFuelStatus9());
            if (tariffDTO.getDiscount9() != null) existingTariff.setDiscount9(tariffDTO.getDiscount9());

            return convertToDTO(tmpTariffRepository.save(existingTariff));
        } else {
            throw new RuntimeException("Tmp Tariff not found with ID: " + tariffId);
        }
    }

    // ================================================================
    // UPDATE RECORD STATUS - FOR ACTIVE/INACTIVE TOGGLE
    // ================================================================
    @Transactional
    public void updateRecordStatus(Short tariffId, Character recordStatus) {
        Optional<TmpTariff> existingTariffOptional = tmpTariffRepository.findById(tariffId);

        if (existingTariffOptional.isPresent()) {
            TmpTariff existingTariff = existingTariffOptional.get();
            existingTariff.setRecordStatus(recordStatus);
            tmpTariffRepository.save(existingTariff);
            System.out.println("✅ Updated tariff " + tariffId + " record_status to: " + recordStatus);
        } else {
            throw new RuntimeException("Tmp Tariff not found with ID: " + tariffId);
        }
    }

    // ================================================================
    // END ALL TARIFFS - MAIN FUNCTION
    // ================================================================
    // Result class for endAllTariffsAndCreateNew
    // ================================================================
    public static class EndAllTariffsResult {
        private int endedCount;
        private int createdCount;

        public EndAllTariffsResult(int endedCount, int createdCount) {
            this.endedCount = endedCount;
            this.createdCount = createdCount;
        }

        public int getEndedCount() {
            return endedCount;
        }

        public int getCreatedCount() {
            return createdCount;
        }
    }

    // ================================================================
    @Transactional
    public EndAllTariffsResult endAllTariffsAndCreateNew() {
        Date today = new Date();

        // Step 1: Get all active tariffs (to_date = NULL)
        List<TmpTariff> activeTariffs = tmpTariffRepository.findByToDateIsNull();
        int endedCount = activeTariffs.size();

        // Step 2: End all active tariffs by setting to_date = today
        for (TmpTariff tariff : activeTariffs) {
            tariff.setToDate(today);
            tmpTariffRepository.save(tariff);
        }

        // Step 3: Create new empty tariff records with same IDs
        int createdCount = 0;
        for (Short tariffId : STANDARD_TARIFF_IDS) {
            TmpTariff newTariff = createEmptyTariff(tariffId, today);
            tmpTariffRepository.save(newTariff);
            createdCount++;
        }

        return new EndAllTariffsResult(endedCount, createdCount);
    }

    // ================================================================
    // End only specific tariffs and create new ones
    @Transactional
    public EndAllTariffsResult endSpecificTariffsAndCreateNew(List<Short> tariffIdsToEnd) {
        // Validation: ensure list is not null or empty
        if (tariffIdsToEnd == null || tariffIdsToEnd.isEmpty()) {
            throw new IllegalArgumentException("Tariff IDs list cannot be null or empty");
        }

        Date today = new Date();

        // Step 1: Get all active tariffs (to_date = NULL)
        List<TmpTariff> activeTariffs = tmpTariffRepository.findByToDateIsNull();

        // Step 2: Filter to only include tariffs whose ID is in the provided list
        List<TmpTariff> tariffsToEnd = activeTariffs.stream()
            .filter(t -> tariffIdsToEnd.contains(t.getTariff()))
            .collect(Collectors.toList());

        int endedCount = tariffsToEnd.size();

        // Step 3: End filtered tariffs by setting to_date = today
        for (TmpTariff tariff : tariffsToEnd) {
            tariff.setToDate(today);
            tmpTariffRepository.save(tariff);
        }

        // Step 4: Create new empty tariff records for each tariff ID in the list
        int createdCount = 0;
        for (Short tariffId : tariffIdsToEnd) {
            TmpTariff newTariff = createEmptyTariff(tariffId, today);
            tmpTariffRepository.save(newTariff);
            createdCount++;
        }

        return new EndAllTariffsResult(endedCount, createdCount);
    }

    // Helper method to create empty tariff record
    private TmpTariff createEmptyTariff(Short tariffId, Date fromDate) {
        TmpTariff tariff = new TmpTariff();
        
        // Basic fields
        tariff.setTariff(tariffId);
        tariff.setFromDate(fromDate);
        tariff.setToDate(null);  // NULL means active
        tariff.setRateStatus('A');
        tariff.setRecordStatus('A');
        tariff.setMinCharge(BigDecimal.ZERO);
        tariff.setNumberOfSlabs((short) 1);

        // Initialize all slab fields to NULL or 0
        tariff.setLimit1(null);
        tariff.setRate1(BigDecimal.ZERO);
        tariff.setFixedCharge1(BigDecimal.ZERO);
        tariff.setFuelCharge1(BigDecimal.ZERO);
        tariff.setServiceCharge1(BigDecimal.ZERO);
        tariff.setFuelStatus1('N');
        tariff.setDiscount1(BigDecimal.ZERO);

        // Repeat for slabs 2-9 (all NULL/0)
        tariff.setLimit2(null);
        tariff.setRate2(null);
        tariff.setFixedCharge2(null);
        // ... (similar for remaining slabs)

        return tariff;
    }
}