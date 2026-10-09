package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.TariffDTO;
import com.example.SPSProjectBackend.model.Tariff;
import com.example.SPSProjectBackend.model.TmpTariff;
import com.example.SPSProjectBackend.repository.TariffRepository;
import com.example.SPSProjectBackend.repository.TmpTariffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;



@Service
public class TariffService {

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private TmpTariffRepository tmpTariffRepository;

    // ================================================================
    // CONVERT ENTITY TO DTO
    // ================================================================
    private TariffDTO convertToDTO(Tariff tariff) {
        TariffDTO dto = new TariffDTO();
        dto.setTariff(tariff.getTariff());
        dto.setRateStatus(tariff.getRateStatus());
        dto.setRecordStatus(tariff.getRecordStatus());
        dto.setFromDate(tariff.getFrmDate());  // ✅ Maps frmDate to fromDate
        dto.setToDate(tariff.getToDate());
        dto.setMinCharge(tariff.getMinCharge());
        dto.setNumberOfSlabs(tariff.getNumberOfSlabs());

        // Slab 1
        dto.setLimit1(tariff.getLimit1());
        dto.setRate1(tariff.getRate1());
        dto.setFixedCharge1(tariff.getFixedCharge1());
        dto.setFuelCharge1(tariff.getFuelCharge1());
        dto.setServiceCharge1(tariff.getServiceCharge1());
        dto.setFuelStatus1(tariff.getFuelStatus1());
        dto.setDiscount1(tariff.getDiscount1());

        // Slab 2
        dto.setLimit2(tariff.getLimit2());
        dto.setRate2(tariff.getRate2());
        dto.setFixedCharge2(tariff.getFixedCharge2());
        dto.setFuelCharge2(tariff.getFuelCharge2());
        dto.setServiceCharge2(tariff.getServiceCharge2());
        dto.setFuelStatus2(tariff.getFuelStatus2());
        dto.setDiscount2(tariff.getDiscount2());

        // Slab 3
        dto.setLimit3(tariff.getLimit3());
        dto.setRate3(tariff.getRate3());
        dto.setFixedCharge3(tariff.getFixedCharge3());
        dto.setFuelCharge3(tariff.getFuelCharge3());
        dto.setServiceCharge3(tariff.getServiceCharge3());
        dto.setFuelStatus3(tariff.getFuelStatus3());
        dto.setDiscount3(tariff.getDiscount3());

        // Slab 4
        dto.setLimit4(tariff.getLimit4());
        dto.setRate4(tariff.getRate4());
        dto.setFixedCharge4(tariff.getFixedCharge4());
        dto.setFuelCharge4(tariff.getFuelCharge4());
        dto.setServiceCharge4(tariff.getServiceCharge4());
        dto.setFuelStatus4(tariff.getFuelStatus4());
        dto.setDiscount4(tariff.getDiscount4());

        // Slab 5
        dto.setLimit5(tariff.getLimit5());
        dto.setRate5(tariff.getRate5());
        dto.setFixedCharge5(tariff.getFixedCharge5());
        dto.setFuelCharge5(tariff.getFuelCharge5());
        dto.setServiceCharge5(tariff.getServiceCharge5());
        dto.setFuelStatus5(tariff.getFuelStatus5());
        dto.setDiscount5(tariff.getDiscount5());

        // Slab 6
        dto.setLimit6(tariff.getLimit6());
        dto.setRate6(tariff.getRate6());
        dto.setFixedCharge6(tariff.getFixedCharge6());
        dto.setFuelCharge6(tariff.getFuelCharge6());
        dto.setServiceCharge6(tariff.getServiceCharge6());
        dto.setFuelStatus6(tariff.getFuelStatus6());
        dto.setDiscount6(tariff.getDiscount6());

        // Slab 7
        dto.setLimit7(tariff.getLimit7());
        dto.setRate7(tariff.getRate7());
        dto.setFixedCharge7(tariff.getFixedCharge7());
        dto.setFuelCharge7(tariff.getFuelCharge7());
        dto.setServiceCharge7(tariff.getServiceCharge7());
        dto.setFuelStatus7(tariff.getFuelStatus7());
        dto.setDiscount7(tariff.getDiscount7());

        // Slab 8
        dto.setLimit8(tariff.getLimit8());
        dto.setRate8(tariff.getRate8());
        dto.setFixedCharge8(tariff.getFixedCharge8());
        dto.setFuelCharge8(tariff.getFuelCharge8());
        dto.setServiceCharge8(tariff.getServiceCharge8());
        dto.setFuelStatus8(tariff.getFuelStatus8());
        dto.setDiscount8(tariff.getDiscount8());

        // Slab 9
        dto.setLimit9(tariff.getLimit9());
        dto.setRate9(tariff.getRate9());
        dto.setFixedCharge9(tariff.getFixedCharge9());
        dto.setFuelCharge9(tariff.getFuelCharge9());
        dto.setServiceCharge9(tariff.getServiceCharge9());
        dto.setFuelStatus9(tariff.getFuelStatus9());
        dto.setDiscount9(tariff.getDiscount9());

        return dto;
    }

    // ================================================================
    // CONVERT DTO TO ENTITY
    // ================================================================
    private Tariff convertToEntity(TariffDTO dto) {
        Tariff tariff = new Tariff();
        tariff.setTariff(dto.getTariff());
        tariff.setRateStatus(dto.getRateStatus());
        tariff.setRecordStatus(dto.getRecordStatus());
        tariff.setFrmDate(dto.getFromDate());  // ✅ Maps fromDate to frmDate
        tariff.setToDate(dto.getToDate());
        tariff.setMinCharge(dto.getMinCharge());
        tariff.setNumberOfSlabs(dto.getNumberOfSlabs());

        // Slab 1
        tariff.setLimit1(dto.getLimit1());
        tariff.setRate1(dto.getRate1());
        tariff.setFixedCharge1(dto.getFixedCharge1());
        tariff.setFuelCharge1(dto.getFuelCharge1());
        tariff.setServiceCharge1(dto.getServiceCharge1());
        tariff.setFuelStatus1(dto.getFuelStatus1());
        tariff.setDiscount1(dto.getDiscount1());

        // Slab 2
        tariff.setLimit2(dto.getLimit2());
        tariff.setRate2(dto.getRate2());
        tariff.setFixedCharge2(dto.getFixedCharge2());
        tariff.setFuelCharge2(dto.getFuelCharge2());
        tariff.setServiceCharge2(dto.getServiceCharge2());
        tariff.setFuelStatus2(dto.getFuelStatus2());
        tariff.setDiscount2(dto.getDiscount2());

        // Slab 3
        tariff.setLimit3(dto.getLimit3());
        tariff.setRate3(dto.getRate3());
        tariff.setFixedCharge3(dto.getFixedCharge3());
        tariff.setFuelCharge3(dto.getFuelCharge3());
        tariff.setServiceCharge3(dto.getServiceCharge3());
        tariff.setFuelStatus3(dto.getFuelStatus3());
        tariff.setDiscount3(dto.getDiscount3());

        // Slab 4
        tariff.setLimit4(dto.getLimit4());
        tariff.setRate4(dto.getRate4());
        tariff.setFixedCharge4(dto.getFixedCharge4());
        tariff.setFuelCharge4(dto.getFuelCharge4());
        tariff.setServiceCharge4(dto.getServiceCharge4());
        tariff.setFuelStatus4(dto.getFuelStatus4());
        tariff.setDiscount4(dto.getDiscount4());

        // Slab 5
        tariff.setLimit5(dto.getLimit5());
        tariff.setRate5(dto.getRate5());
        tariff.setFixedCharge5(dto.getFixedCharge5());
        tariff.setFuelCharge5(dto.getFuelCharge5());
        tariff.setServiceCharge5(dto.getServiceCharge5());
        tariff.setFuelStatus5(dto.getFuelStatus5());
        tariff.setDiscount5(dto.getDiscount5());

        // Slab 6
        tariff.setLimit6(dto.getLimit6());
        tariff.setRate6(dto.getRate6());
        tariff.setFixedCharge6(dto.getFixedCharge6());
        tariff.setFuelCharge6(dto.getFuelCharge6());
        tariff.setServiceCharge6(dto.getServiceCharge6());
        tariff.setFuelStatus6(dto.getFuelStatus6());
        tariff.setDiscount6(dto.getDiscount6());

        // Slab 7
        tariff.setLimit7(dto.getLimit7());
        tariff.setRate7(dto.getRate7());
        tariff.setFixedCharge7(dto.getFixedCharge7());
        tariff.setFuelCharge7(dto.getFuelCharge7());
        tariff.setServiceCharge7(dto.getServiceCharge7());
        tariff.setFuelStatus7(dto.getFuelStatus7());
        tariff.setDiscount7(dto.getDiscount7());

        // Slab 8
        tariff.setLimit8(dto.getLimit8());
        tariff.setRate8(dto.getRate8());
        tariff.setFixedCharge8(dto.getFixedCharge8());
        tariff.setFuelCharge8(dto.getFuelCharge8());
        tariff.setServiceCharge8(dto.getServiceCharge8());
        tariff.setFuelStatus8(dto.getFuelStatus8());
        tariff.setDiscount8(dto.getDiscount8());

        // Slab 9
        tariff.setLimit9(dto.getLimit9());
        tariff.setRate9(dto.getRate9());
        tariff.setFixedCharge9(dto.getFixedCharge9());
        tariff.setFuelCharge9(dto.getFuelCharge9());
        tariff.setServiceCharge9(dto.getServiceCharge9());
        tariff.setFuelStatus9(dto.getFuelStatus9());
        tariff.setDiscount9(dto.getDiscount9());

        return tariff;
    }

    // ================================================================
    // GET LATEST ACTIVE TARIFFS ONLY
    // ================================================================
    public List<TariffDTO> getAllTariffs() {
        // Get all tariffs with to_date = NULL
        List<Tariff> allActiveTariffs = tariffRepository.findByToDateIsNull();
        
        System.out.println("=== GET ALL TARIFFS ===");
        System.out.println("Total active tariffs found: " + allActiveTariffs.size());
        
        // Group by tariff number and keep only the latest (by from_date)
        Map<Short, Tariff> latestTariffs = new HashMap<>();
        
        for (Tariff tariff : allActiveTariffs) {
            Short tariffNum = tariff.getTariff();
            
            System.out.println("Processing tariff " + tariffNum + " from " + tariff.getFrmDate() + " to " + tariff.getToDate());
            
            if (!latestTariffs.containsKey(tariffNum)) {
                // First time seeing this tariff number
                latestTariffs.put(tariffNum, tariff);
                System.out.println("  -> Added as first entry");
            } else {
                // Compare with existing
                Tariff existing = latestTariffs.get(tariffNum);
                
                if (tariff.getFrmDate().after(existing.getFrmDate())) {
                    // This one is newer
                    latestTariffs.put(tariffNum, tariff);
                    System.out.println("  -> Replaced with newer (from " + existing.getFrmDate() + " to " + tariff.getFrmDate() + ")");
                } else {
                    System.out.println("  -> Skipped (older than " + existing.getFrmDate() + ")");
                }
            }
        }
        
        System.out.println("Final count: " + latestTariffs.size() + " latest tariffs");
        
        // Convert to list and sort by tariff number
        List<Tariff> result = new ArrayList<>(latestTariffs.values());
        result.sort(Comparator.comparing(Tariff::getTariff));
        
        return result.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ================================================================
    // GET ALL ACTIVE TARIFFS (to_date = NULL)
    // Used by Live Tariffs page to show only current tariffs
    // ================================================================
    public List<TariffDTO> getAllActiveTariffs() {
        List<Tariff> tariffs = tariffRepository.findByToDateIsNull();

        // ✅ Sort by tariff ID in ascending order (11, 13, 14, 15... 93)
        tariffs.sort(Comparator.comparing(Tariff::getTariff));

        return tariffs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ================================================================
    // GET ALL ENDED TARIFFS (to_date != NULL)
    // Used by Tariff History page to show ended live tariffs
    // ================================================================
    public List<TariffDTO> getAllEndedTariffs() {
        List<Tariff> tariffs = tariffRepository.findByToDateIsNotNull();

        // ✅ Sort by tariff ID to ensure consistent order
        tariffs.sort(Comparator.comparing(Tariff::getTariff));

        return tariffs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ================================================================
    // GET TARIFF BY TARIFF NUMBER AND FROM DATE
    // ================================================================
    public Optional<TariffDTO> getTariffByTariffAndFromDate(Short tariff, Date fromDate) {
        Optional<Tariff> tariffEntity = tariffRepository.findByTariffAndFrmDate(tariff, fromDate);
        return tariffEntity.map(this::convertToDTO);
    }

    // ================================================================
    // SAVE NEW TARIFF
    // ================================================================
    @Transactional
    public TariffDTO saveTariff(TariffDTO tariffDTO) {
        if (tariffDTO.getTariff() == null || tariffDTO.getFromDate() == null) {
            throw new IllegalArgumentException("Tariff ID and From Date cannot be null");
        }
        Tariff tariff = convertToEntity(tariffDTO);
        return convertToDTO(tariffRepository.save(tariff));
    }

    // ================================================================
    // UPDATE TARIFF (by tariff number and from date)
    // ================================================================
    @Transactional
    public TariffDTO updateTariff(Short tariffId, Date fromDate, TariffDTO tariffDTO) {
        Optional<Tariff> existingOptional = tariffRepository.findByTariffAndFrmDate(tariffId, fromDate);

        if (existingOptional.isPresent()) {
            Tariff existing = existingOptional.get();

            // Update only non-null fields
            if (tariffDTO.getRateStatus() != null) {
                existing.setRateStatus(tariffDTO.getRateStatus());
            }
            if (tariffDTO.getRecordStatus() != null) {
                existing.setRecordStatus(tariffDTO.getRecordStatus());
            }
            
            // ✅ CRITICAL: Always update toDate (for ending tariffs)
            existing.setToDate(tariffDTO.getToDate());
            
            if (tariffDTO.getMinCharge() != null) {
                existing.setMinCharge(tariffDTO.getMinCharge());
            }
            if (tariffDTO.getNumberOfSlabs() != null) {
                existing.setNumberOfSlabs(tariffDTO.getNumberOfSlabs());
            }

            // Update slab fields (only if not null)
            if (tariffDTO.getLimit1() != null) existing.setLimit1(tariffDTO.getLimit1());
            if (tariffDTO.getRate1() != null) existing.setRate1(tariffDTO.getRate1());
            if (tariffDTO.getFixedCharge1() != null) existing.setFixedCharge1(tariffDTO.getFixedCharge1());
            
            if (tariffDTO.getLimit2() != null) existing.setLimit2(tariffDTO.getLimit2());
            if (tariffDTO.getRate2() != null) existing.setRate2(tariffDTO.getRate2());
            if (tariffDTO.getFixedCharge2() != null) existing.setFixedCharge2(tariffDTO.getFixedCharge2());
            
            if (tariffDTO.getLimit3() != null) existing.setLimit3(tariffDTO.getLimit3());
            if (tariffDTO.getRate3() != null) existing.setRate3(tariffDTO.getRate3());
            if (tariffDTO.getFixedCharge3() != null) existing.setFixedCharge3(tariffDTO.getFixedCharge3());
            
            if (tariffDTO.getLimit4() != null) existing.setLimit4(tariffDTO.getLimit4());
            if (tariffDTO.getRate4() != null) existing.setRate4(tariffDTO.getRate4());
            if (tariffDTO.getFixedCharge4() != null) existing.setFixedCharge4(tariffDTO.getFixedCharge4());
            
            if (tariffDTO.getLimit5() != null) existing.setLimit5(tariffDTO.getLimit5());
            if (tariffDTO.getRate5() != null) existing.setRate5(tariffDTO.getRate5());
            if (tariffDTO.getFixedCharge5() != null) existing.setFixedCharge5(tariffDTO.getFixedCharge5());
            
            if (tariffDTO.getLimit6() != null) existing.setLimit6(tariffDTO.getLimit6());
            if (tariffDTO.getRate6() != null) existing.setRate6(tariffDTO.getRate6());
            if (tariffDTO.getFixedCharge6() != null) existing.setFixedCharge6(tariffDTO.getFixedCharge6());
            
            if (tariffDTO.getLimit7() != null) existing.setLimit7(tariffDTO.getLimit7());
            if (tariffDTO.getRate7() != null) existing.setRate7(tariffDTO.getRate7());
            if (tariffDTO.getFixedCharge7() != null) existing.setFixedCharge7(tariffDTO.getFixedCharge7());
            
            if (tariffDTO.getLimit8() != null) existing.setLimit8(tariffDTO.getLimit8());
            if (tariffDTO.getRate8() != null) existing.setRate8(tariffDTO.getRate8());
            if (tariffDTO.getFixedCharge8() != null) existing.setFixedCharge8(tariffDTO.getFixedCharge8());
            
            if (tariffDTO.getLimit9() != null) existing.setLimit9(tariffDTO.getLimit9());
            if (tariffDTO.getRate9() != null) existing.setRate9(tariffDTO.getRate9());
            if (tariffDTO.getFixedCharge9() != null) existing.setFixedCharge9(tariffDTO.getFixedCharge9());

            return convertToDTO(tariffRepository.save(existing));
        } else {
            throw new RuntimeException("Tariff not found: " + tariffId + " with fromDate: " + fromDate);
        }
    }

    // ================================================================
    // END ALL TARIFFS AND CREATE NEW ONES - MAIN FUNCTION
    // ================================================================
    @Transactional
    public EndAllTariffsResult endAllTariffsAndCreateNew() {
        Date today = new Date();
        
        System.out.println("=== ENDING ALL TARIFFS ===");
        System.out.println("Today's date: " + today);

        // ✅ Get ALL active tariffs (to_date = NULL)
        List<Tariff> allActiveTariffs = tariffRepository.findByToDateIsNull();
        System.out.println("Found " + allActiveTariffs.size() + " active tariffs");
        
        int endedCount = 0;
        int createdCount = 0;

        // ✅ STEP 1: End ALL active tariffs
        for (Tariff tariff : allActiveTariffs) {
            System.out.println("Ending tariff: " + tariff.getTariff() + " from " + tariff.getFrmDate());
            tariff.setToDate(today);
            tariffRepository.save(tariff);
            endedCount++;
        }

        // ✅ STEP 2: Get ALL tmp_tariff records (both Active and Inactive)
        List<TmpTariff> allTmpTariffs = tmpTariffRepository.findAll();
        System.out.println("Found " + allTmpTariffs.size() + " tmp_tariff records to upload");

        // ✅ Sort by tariff ID to ensure correct order in database
        allTmpTariffs.sort(Comparator.comparing(TmpTariff::getTariff));
        System.out.println("Sorted tmp_tariff records by tariff ID");

        // ✅ STEP 3: Create NEW tariff records from tmp_tariff (with block values)
        for (TmpTariff tmpTariff : allTmpTariffs) {
            Tariff newTariff = new Tariff();

            // ✅ Primary key
            newTariff.setTariff(tmpTariff.getTariff());
            newTariff.setFrmDate(today);

            // ✅ Metadata fields
            newTariff.setToDate(null);
            newTariff.setRateStatus('A');
            newTariff.setRecordStatus('A');
            newTariff.setMinCharge(tmpTariff.getMinCharge());
            newTariff.setNumberOfSlabs(tmpTariff.getNumberOfSlabs());

            // ✅ Copy Slab 1 from tmp_tariff
            newTariff.setLimit1(tmpTariff.getLimit1());
            newTariff.setRate1(tmpTariff.getRate1());
            newTariff.setFixedCharge1(tmpTariff.getFixedCharge1());
            newTariff.setFuelCharge1(tmpTariff.getFuelCharge1());
            newTariff.setServiceCharge1(tmpTariff.getServiceCharge1());
            newTariff.setFuelStatus1(tmpTariff.getFuelStatus1());
            newTariff.setDiscount1(tmpTariff.getDiscount1());

            // ✅ Copy Slab 2 from tmp_tariff
            newTariff.setLimit2(tmpTariff.getLimit2());
            newTariff.setRate2(tmpTariff.getRate2());
            newTariff.setFixedCharge2(tmpTariff.getFixedCharge2());
            newTariff.setFuelCharge2(tmpTariff.getFuelCharge2());
            newTariff.setServiceCharge2(tmpTariff.getServiceCharge2());
            newTariff.setFuelStatus2(tmpTariff.getFuelStatus2());
            newTariff.setDiscount2(tmpTariff.getDiscount2());

            // ✅ Copy Slab 3 from tmp_tariff
            newTariff.setLimit3(tmpTariff.getLimit3());
            newTariff.setRate3(tmpTariff.getRate3());
            newTariff.setFixedCharge3(tmpTariff.getFixedCharge3());
            newTariff.setFuelCharge3(tmpTariff.getFuelCharge3());
            newTariff.setServiceCharge3(tmpTariff.getServiceCharge3());
            newTariff.setFuelStatus3(tmpTariff.getFuelStatus3());
            newTariff.setDiscount3(tmpTariff.getDiscount3());

            // ✅ Copy Slab 4 from tmp_tariff
            newTariff.setLimit4(tmpTariff.getLimit4());
            newTariff.setRate4(tmpTariff.getRate4());
            newTariff.setFixedCharge4(tmpTariff.getFixedCharge4());
            newTariff.setFuelCharge4(tmpTariff.getFuelCharge4());
            newTariff.setServiceCharge4(tmpTariff.getServiceCharge4());
            newTariff.setFuelStatus4(tmpTariff.getFuelStatus4());
            newTariff.setDiscount4(tmpTariff.getDiscount4());

            // ✅ Copy Slab 5 from tmp_tariff
            newTariff.setLimit5(tmpTariff.getLimit5());
            newTariff.setRate5(tmpTariff.getRate5());
            newTariff.setFixedCharge5(tmpTariff.getFixedCharge5());
            newTariff.setFuelCharge5(tmpTariff.getFuelCharge5());
            newTariff.setServiceCharge5(tmpTariff.getServiceCharge5());
            newTariff.setFuelStatus5(tmpTariff.getFuelStatus5());
            newTariff.setDiscount5(tmpTariff.getDiscount5());

            // ✅ Copy Slab 6 from tmp_tariff
            newTariff.setLimit6(tmpTariff.getLimit6());
            newTariff.setRate6(tmpTariff.getRate6());
            newTariff.setFixedCharge6(tmpTariff.getFixedCharge6());
            newTariff.setFuelCharge6(tmpTariff.getFuelCharge6());
            newTariff.setServiceCharge6(tmpTariff.getServiceCharge6());
            newTariff.setFuelStatus6(tmpTariff.getFuelStatus6());
            newTariff.setDiscount6(tmpTariff.getDiscount6());

            // ✅ Copy Slab 7 from tmp_tariff
            newTariff.setLimit7(tmpTariff.getLimit7());
            newTariff.setRate7(tmpTariff.getRate7());
            newTariff.setFixedCharge7(tmpTariff.getFixedCharge7());
            newTariff.setFuelCharge7(tmpTariff.getFuelCharge7());
            newTariff.setServiceCharge7(tmpTariff.getServiceCharge7());
            newTariff.setFuelStatus7(tmpTariff.getFuelStatus7());
            newTariff.setDiscount7(tmpTariff.getDiscount7());

            // ✅ Copy Slab 8 from tmp_tariff
            newTariff.setLimit8(tmpTariff.getLimit8());
            newTariff.setRate8(tmpTariff.getRate8());
            newTariff.setFixedCharge8(tmpTariff.getFixedCharge8());
            newTariff.setFuelCharge8(tmpTariff.getFuelCharge8());
            newTariff.setServiceCharge8(tmpTariff.getServiceCharge8());
            newTariff.setFuelStatus8(tmpTariff.getFuelStatus8());
            newTariff.setDiscount8(tmpTariff.getDiscount8());

            // ✅ Copy Slab 9 from tmp_tariff
            newTariff.setLimit9(tmpTariff.getLimit9());
            newTariff.setRate9(tmpTariff.getRate9());
            newTariff.setFixedCharge9(tmpTariff.getFixedCharge9());
            newTariff.setFuelCharge9(tmpTariff.getFuelCharge9());
            newTariff.setServiceCharge9(tmpTariff.getServiceCharge9());
            newTariff.setFuelStatus9(tmpTariff.getFuelStatus9());
            newTariff.setDiscount9(tmpTariff.getDiscount9());

            String status = tmpTariff.getRecordStatus() == 'A' ? "Active" : "Inactive";
            System.out.println("Creating new tariff: " + tmpTariff.getTariff() + " (" + status + ") from " + today);
            tariffRepository.save(newTariff);
            createdCount++;
        }

        // ✅ NEW STEP: DELETE ended tariffs from database
        System.out.println("=== DELETING ENDED TARIFFS FROM DATABASE ===");
        for (Tariff tariff : allActiveTariffs) {
            System.out.println("Deleting tariff: " + tariff.getTariff() + " from " + tariff.getFrmDate() + " (ended: " + tariff.getToDate() + ")");
            tariffRepository.delete(tariff);
        }
        System.out.println("Deleted: " + allActiveTariffs.size() + " ended tariff record(s)");

        tariffRepository.flush();

        System.out.println("=== COMPLETED ===");
        System.out.println("Ended: " + endedCount);
        System.out.println("Created: " + createdCount);
        System.out.println("Deleted: " + allActiveTariffs.size());

        return new EndAllTariffsResult(endedCount, createdCount);
    }


    // ================================================================
    // END SPECIFIC TARIFFS AND CREATE NEW ONES - FOR ACTIVE/INACTIVE STATUS
    // ================================================================
    @Transactional
    public EndAllTariffsResult endSpecificTariffsAndCreateNew(List<Short> tariffIdsToEnd) {
        if (tariffIdsToEnd == null || tariffIdsToEnd.isEmpty()) {
            throw new IllegalArgumentException("Tariff IDs list cannot be null or empty");
        }

        Date today = new Date();

        System.out.println("=== ENDING SPECIFIC TARIFFS ===");
        System.out.println("Today's date: " + today);
        System.out.println("Tariff IDs to end: " + tariffIdsToEnd);

        // ✅ Get ALL active tariffs (to_date = NULL)
        List<Tariff> allActiveTariffs = tariffRepository.findByToDateIsNull();
        System.out.println("Found " + allActiveTariffs.size() + " active tariffs");

        // ✅ Filter to only those in the provided list
        List<Tariff> tariffsToEnd = allActiveTariffs.stream()
            .filter(t -> tariffIdsToEnd.contains(t.getTariff()))
            .collect(Collectors.toList());

        System.out.println("Filtered to " + tariffsToEnd.size() + " tariffs to end");

        int endedCount = 0;
        int createdCount = 0;

        // ✅ STEP 1: End filtered tariffs
        for (Tariff tariff : tariffsToEnd) {
            System.out.println("Ending tariff: " + tariff.getTariff() + " from " + tariff.getFrmDate());
            tariff.setToDate(today);
            tariffRepository.save(tariff);
            endedCount++;
        }

        // ✅ STEP 2: Get ALL tmp_tariff records (both Active and Inactive)
        List<TmpTariff> allTmpTariffs = tmpTariffRepository.findAll();
        System.out.println("Found " + allTmpTariffs.size() + " tmp_tariff records");

        // ✅ Filter to only those in the tariffIdsToEnd list
        List<TmpTariff> tmpTariffsToUpload = allTmpTariffs.stream()
            .filter(t -> tariffIdsToEnd.contains(t.getTariff()))
            .collect(Collectors.toList());

        // ✅ Sort by tariff ID to ensure correct order in database
        tmpTariffsToUpload.sort(Comparator.comparing(TmpTariff::getTariff));

        System.out.println("Uploading " + tmpTariffsToUpload.size() + " tmp_tariff records in sorted order");

        // ✅ STEP 3: Create NEW tariff records WITH BLOCK VALUES from tmp_tariff
        for (TmpTariff tmpTariff : tmpTariffsToUpload) {
            Tariff newTariff = new Tariff();

            // Primary key
            newTariff.setTariff(tmpTariff.getTariff());
            newTariff.setFrmDate(today);

            // Metadata
            newTariff.setToDate(null);
            newTariff.setRateStatus('A');
            newTariff.setRecordStatus('A');
            newTariff.setMinCharge(tmpTariff.getMinCharge());
            newTariff.setNumberOfSlabs(tmpTariff.getNumberOfSlabs());

            // Copy ALL slabs from tmp_tariff
            newTariff.setLimit1(tmpTariff.getLimit1());
            newTariff.setRate1(tmpTariff.getRate1());
            newTariff.setFixedCharge1(tmpTariff.getFixedCharge1());
            newTariff.setFuelCharge1(tmpTariff.getFuelCharge1());
            newTariff.setServiceCharge1(tmpTariff.getServiceCharge1());
            newTariff.setFuelStatus1(tmpTariff.getFuelStatus1());
            newTariff.setDiscount1(tmpTariff.getDiscount1());

            newTariff.setLimit2(tmpTariff.getLimit2());
            newTariff.setRate2(tmpTariff.getRate2());
            newTariff.setFixedCharge2(tmpTariff.getFixedCharge2());
            newTariff.setFuelCharge2(tmpTariff.getFuelCharge2());
            newTariff.setServiceCharge2(tmpTariff.getServiceCharge2());
            newTariff.setFuelStatus2(tmpTariff.getFuelStatus2());
            newTariff.setDiscount2(tmpTariff.getDiscount2());

            newTariff.setLimit3(tmpTariff.getLimit3());
            newTariff.setRate3(tmpTariff.getRate3());
            newTariff.setFixedCharge3(tmpTariff.getFixedCharge3());
            newTariff.setFuelCharge3(tmpTariff.getFuelCharge3());
            newTariff.setServiceCharge3(tmpTariff.getServiceCharge3());
            newTariff.setFuelStatus3(tmpTariff.getFuelStatus3());
            newTariff.setDiscount3(tmpTariff.getDiscount3());

            newTariff.setLimit4(tmpTariff.getLimit4());
            newTariff.setRate4(tmpTariff.getRate4());
            newTariff.setFixedCharge4(tmpTariff.getFixedCharge4());
            newTariff.setFuelCharge4(tmpTariff.getFuelCharge4());
            newTariff.setServiceCharge4(tmpTariff.getServiceCharge4());
            newTariff.setFuelStatus4(tmpTariff.getFuelStatus4());
            newTariff.setDiscount4(tmpTariff.getDiscount4());

            newTariff.setLimit5(tmpTariff.getLimit5());
            newTariff.setRate5(tmpTariff.getRate5());
            newTariff.setFixedCharge5(tmpTariff.getFixedCharge5());
            newTariff.setFuelCharge5(tmpTariff.getFuelCharge5());
            newTariff.setServiceCharge5(tmpTariff.getServiceCharge5());
            newTariff.setFuelStatus5(tmpTariff.getFuelStatus5());
            newTariff.setDiscount5(tmpTariff.getDiscount5());

            newTariff.setLimit6(tmpTariff.getLimit6());
            newTariff.setRate6(tmpTariff.getRate6());
            newTariff.setFixedCharge6(tmpTariff.getFixedCharge6());
            newTariff.setFuelCharge6(tmpTariff.getFuelCharge6());
            newTariff.setServiceCharge6(tmpTariff.getServiceCharge6());
            newTariff.setFuelStatus6(tmpTariff.getFuelStatus6());
            newTariff.setDiscount6(tmpTariff.getDiscount6());

            newTariff.setLimit7(tmpTariff.getLimit7());
            newTariff.setRate7(tmpTariff.getRate7());
            newTariff.setFixedCharge7(tmpTariff.getFixedCharge7());
            newTariff.setFuelCharge7(tmpTariff.getFuelCharge7());
            newTariff.setServiceCharge7(tmpTariff.getServiceCharge7());
            newTariff.setFuelStatus7(tmpTariff.getFuelStatus7());
            newTariff.setDiscount7(tmpTariff.getDiscount7());

            newTariff.setLimit8(tmpTariff.getLimit8());
            newTariff.setRate8(tmpTariff.getRate8());
            newTariff.setFixedCharge8(tmpTariff.getFixedCharge8());
            newTariff.setFuelCharge8(tmpTariff.getFuelCharge8());
            newTariff.setServiceCharge8(tmpTariff.getServiceCharge8());
            newTariff.setFuelStatus8(tmpTariff.getFuelStatus8());
            newTariff.setDiscount8(tmpTariff.getDiscount8());

            newTariff.setLimit9(tmpTariff.getLimit9());
            newTariff.setRate9(tmpTariff.getRate9());
            newTariff.setFixedCharge9(tmpTariff.getFixedCharge9());
            newTariff.setFuelCharge9(tmpTariff.getFuelCharge9());
            newTariff.setServiceCharge9(tmpTariff.getServiceCharge9());
            newTariff.setFuelStatus9(tmpTariff.getFuelStatus9());
            newTariff.setDiscount9(tmpTariff.getDiscount9());

            String status = tmpTariff.getRecordStatus() == 'A' ? "Active" : "Inactive";
            System.out.println("Creating tariff " + tmpTariff.getTariff() + " (" + status + ") with block values from tmp_tariff");
            tariffRepository.save(newTariff);
            createdCount++;
        }

        // ✅ NEW STEP: DELETE ended tariffs from database
        System.out.println("=== DELETING ENDED TARIFFS FROM DATABASE ===");
        for (Tariff tariff : tariffsToEnd) {
            System.out.println("Deleting tariff: " + tariff.getTariff() + " from " + tariff.getFrmDate() + " (ended: " + tariff.getToDate() + ")");
            tariffRepository.delete(tariff);
        }
        System.out.println("Deleted: " + tariffsToEnd.size() + " ended tariff record(s)");

        tariffRepository.flush();

        System.out.println("=== COMPLETED ===");
        System.out.println("Ended: " + endedCount);
        System.out.println("Created: " + createdCount);
        System.out.println("Deleted: " + tariffsToEnd.size());

        return new EndAllTariffsResult(endedCount, createdCount);
    }


    // ================================================================
    // REACTIVATE ALL TARIFFS - ✅ FIXED: Don't clear from_date
    // ================================================================
    @Transactional
    public ReactivateAllTariffsResult reactivateAllTariffs() {
        System.out.println("=== REACTIVATING ALL TARIFFS ===");

        // Step 1: Get all ended tariffs (to_date != NULL)
        List<Tariff> endedTariffs = tariffRepository.findByToDateIsNotNull();
        System.out.println("Found " + endedTariffs.size() + " ended tariffs");

        int reactivatedCount = 0;
        int deletedCount = 0;

        // Step 2: Reactivate all ended tariffs (set to_date = NULL)
        for (Tariff tariff : endedTariffs) {
            System.out.println("Reactivating tariff: " + tariff.getTariff() + 
                             " from " + tariff.getFrmDate() + 
                             " (was ended on " + tariff.getToDate() + ")");
            
            // ✅ CRITICAL FIX: Only clear to_date, NEVER touch from_date!
            tariff.setToDate(null);
            tariffRepository.save(tariff);
            reactivatedCount++;
            
            System.out.println("  -> Reactivated. from_date remains: " + tariff.getFrmDate());
        }

        // Step 3: Delete the new empty tariff records (created today)
        Date today = new Date();
        List<Tariff> todayTariffs = tariffRepository.findByFrmDate(today);
        
        System.out.println("Found " + todayTariffs.size() + " tariffs created today");
        
        for (Tariff tariff : todayTariffs) {
            // Only delete if it's an empty tariff
            if (isEmptyTariff(tariff)) {
                System.out.println("Deleting empty tariff: " + tariff.getTariff() + " from " + today);
                tariffRepository.delete(tariff);
                deletedCount++;
            } else {
                System.out.println("Keeping non-empty tariff: " + tariff.getTariff() + " from " + today);
            }
        }

        tariffRepository.flush();
        
        System.out.println("=== COMPLETED ===");
        System.out.println("Reactivated: " + reactivatedCount);
        System.out.println("Deleted: " + deletedCount);

        return new ReactivateAllTariffsResult(reactivatedCount, deletedCount);
    }

    // Helper method to check if tariff is empty
    private boolean isEmptyTariff(Tariff tariff) {
        return (tariff.getRate1() == null || tariff.getRate1().compareTo(BigDecimal.ZERO) == 0) &&
               (tariff.getRate2() == null || tariff.getRate2().compareTo(BigDecimal.ZERO) == 0);
    }

    // Result class
    public static class ReactivateAllTariffsResult {
        private int reactivatedCount;
        private int deletedCount;

        public ReactivateAllTariffsResult(int reactivatedCount, int deletedCount) {
            this.reactivatedCount = reactivatedCount;
            this.deletedCount = deletedCount;
        }

        public int getReactivatedCount() { return reactivatedCount; }
        public int getDeletedCount() { return deletedCount; }
    }


    // ================================================================
    // TRANSFER TO TMP_TARIFF - Copy all live tariffs to tmp_tariff
    // ================================================================
    @Transactional
    public int transferToTmpTariff() {
        System.out.println("=== TRANSFER TO TMP_TARIFF ===");

        // Step 1: Delete all existing tmp_tariff records
        tmpTariffRepository.deleteAll();
        tmpTariffRepository.flush();
        System.out.println("Cleared all existing tmp_tariff records");

        // Step 2: Get all active tariffs (to_date = NULL)
        List<Tariff> allActiveTariffs = tariffRepository.findByToDateIsNull();
        System.out.println("Found " + allActiveTariffs.size() + " active tariffs");

        // Step 3: Group by tariff number and keep only the latest (by frm_date)
        Map<Short, Tariff> latestTariffs = new HashMap<>();
        for (Tariff tariff : allActiveTariffs) {
            Short tariffNum = tariff.getTariff();

            if (!latestTariffs.containsKey(tariffNum)) {
                latestTariffs.put(tariffNum, tariff);
            } else {
                Tariff existing = latestTariffs.get(tariffNum);
                if (tariff.getFrmDate().after(existing.getFrmDate())) {
                    latestTariffs.put(tariffNum, tariff);
                }
            }
        }

        System.out.println("Identified " + latestTariffs.size() + " unique latest tariffs");

        // Step 4: Convert to sorted list
        List<Tariff> sortedTariffs = new ArrayList<>(latestTariffs.values());
        sortedTariffs.sort(Comparator.comparing(Tariff::getTariff));
        System.out.println("Sorted " + sortedTariffs.size() + " tariffs by ID");

        // Step 5: Copy to tmp_tariff in sorted order
        int count = 0;
        for (Tariff tariff : sortedTariffs) {
            TmpTariff tmpTariff = new TmpTariff();

            // Copy all fields
            tmpTariff.setTariff(tariff.getTariff());
            tmpTariff.setRateStatus(tariff.getRateStatus());
            tmpTariff.setRecordStatus(tariff.getRecordStatus());
            tmpTariff.setFromDate(tariff.getFrmDate());
            tmpTariff.setToDate(tariff.getToDate());
            tmpTariff.setMinCharge(tariff.getMinCharge());
            tmpTariff.setNumberOfSlabs(tariff.getNumberOfSlabs());

            // Slab 1
            tmpTariff.setLimit1(tariff.getLimit1());
            tmpTariff.setRate1(tariff.getRate1());
            tmpTariff.setFixedCharge1(tariff.getFixedCharge1());
            tmpTariff.setFuelCharge1(tariff.getFuelCharge1());
            tmpTariff.setServiceCharge1(tariff.getServiceCharge1());
            tmpTariff.setFuelStatus1(tariff.getFuelStatus1());
            tmpTariff.setDiscount1(tariff.getDiscount1());

            // Slab 2
            tmpTariff.setLimit2(tariff.getLimit2());
            tmpTariff.setRate2(tariff.getRate2());
            tmpTariff.setFixedCharge2(tariff.getFixedCharge2());
            tmpTariff.setFuelCharge2(tariff.getFuelCharge2());
            tmpTariff.setServiceCharge2(tariff.getServiceCharge2());
            tmpTariff.setFuelStatus2(tariff.getFuelStatus2());
            tmpTariff.setDiscount2(tariff.getDiscount2());

            // Slab 3
            tmpTariff.setLimit3(tariff.getLimit3());
            tmpTariff.setRate3(tariff.getRate3());
            tmpTariff.setFixedCharge3(tariff.getFixedCharge3());
            tmpTariff.setFuelCharge3(tariff.getFuelCharge3());
            tmpTariff.setServiceCharge3(tariff.getServiceCharge3());
            tmpTariff.setFuelStatus3(tariff.getFuelStatus3());
            tmpTariff.setDiscount3(tariff.getDiscount3());

            // Slab 4
            tmpTariff.setLimit4(tariff.getLimit4());
            tmpTariff.setRate4(tariff.getRate4());
            tmpTariff.setFixedCharge4(tariff.getFixedCharge4());
            tmpTariff.setFuelCharge4(tariff.getFuelCharge4());
            tmpTariff.setServiceCharge4(tariff.getServiceCharge4());
            tmpTariff.setFuelStatus4(tariff.getFuelStatus4());
            tmpTariff.setDiscount4(tariff.getDiscount4());

            // Slab 5
            tmpTariff.setLimit5(tariff.getLimit5());
            tmpTariff.setRate5(tariff.getRate5());
            tmpTariff.setFixedCharge5(tariff.getFixedCharge5());
            tmpTariff.setFuelCharge5(tariff.getFuelCharge5());
            tmpTariff.setServiceCharge5(tariff.getServiceCharge5());
            tmpTariff.setFuelStatus5(tariff.getFuelStatus5());
            tmpTariff.setDiscount5(tariff.getDiscount5());

            // Slab 6
            tmpTariff.setLimit6(tariff.getLimit6());
            tmpTariff.setRate6(tariff.getRate6());
            tmpTariff.setFixedCharge6(tariff.getFixedCharge6());
            tmpTariff.setFuelCharge6(tariff.getFuelCharge6());
            tmpTariff.setServiceCharge6(tariff.getServiceCharge6());
            tmpTariff.setFuelStatus6(tariff.getFuelStatus6());
            tmpTariff.setDiscount6(tariff.getDiscount6());

            // Slab 7
            tmpTariff.setLimit7(tariff.getLimit7());
            tmpTariff.setRate7(tariff.getRate7());
            tmpTariff.setFixedCharge7(tariff.getFixedCharge7());
            tmpTariff.setFuelCharge7(tariff.getFuelCharge7());
            tmpTariff.setServiceCharge7(tariff.getServiceCharge7());
            tmpTariff.setFuelStatus7(tariff.getFuelStatus7());
            tmpTariff.setDiscount7(tariff.getDiscount7());

            // Slab 8
            tmpTariff.setLimit8(tariff.getLimit8());
            tmpTariff.setRate8(tariff.getRate8());
            tmpTariff.setFixedCharge8(tariff.getFixedCharge8());
            tmpTariff.setFuelCharge8(tariff.getFuelCharge8());
            tmpTariff.setServiceCharge8(tariff.getServiceCharge8());
            tmpTariff.setFuelStatus8(tariff.getFuelStatus8());
            tmpTariff.setDiscount8(tariff.getDiscount8());

            // Slab 9
            tmpTariff.setLimit9(tariff.getLimit9());
            tmpTariff.setRate9(tariff.getRate9());
            tmpTariff.setFixedCharge9(tariff.getFixedCharge9());
            tmpTariff.setFuelCharge9(tariff.getFuelCharge9());
            tmpTariff.setServiceCharge9(tariff.getServiceCharge9());
            tmpTariff.setFuelStatus9(tariff.getFuelStatus9());
            tmpTariff.setDiscount9(tariff.getDiscount9());

            tmpTariffRepository.save(tmpTariff);
            count++;

            System.out.println("Transferred tariff " + tariff.getTariff() + " from " + tariff.getFrmDate());
        }

        tmpTariffRepository.flush();

        System.out.println("=== TRANSFER COMPLETED ===");
        System.out.println("Transferred " + count + " tariff(s) to tmp_tariff");

        return count;
    }

    // ================================================================
    // RESULT CLASS
    // ================================================================
    public static class EndAllTariffsResult {
        private int endedCount;
        private int createdCount;

        public EndAllTariffsResult(int endedCount, int createdCount) {
            this.endedCount = endedCount;
            this.createdCount = createdCount;
        }

        public int getEndedCount() { return endedCount; }
        public int getCreatedCount() { return createdCount; }
    }
}