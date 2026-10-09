package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.TmpReadingsDTO;
import com.example.SPSProjectBackend.model.TmpReadings;
import com.example.SPSProjectBackend.model.TmpReadingsId;
import com.example.SPSProjectBackend.repository.TmpReadingsRepository;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import com.example.SPSProjectBackend.model.NcreBillCycle;
import com.example.SPSProjectBackend.repository.NcreBillCycleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SPSProjectBackend.dto.NcreReadingsDTO;
import com.example.SPSProjectBackend.model.NcreInvRdngs;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.model.NcreInvoiceCreate;
import com.example.SPSProjectBackend.repository.NcreInvRdngsRepository;
import com.example.SPSProjectBackend.repository.NcreInvoiceCreateRepository;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Objects;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TmpReadingsService {

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;
    
    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private NcreBillCycleRepository ncreBillCycleRepository;

    @Autowired
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    @Autowired
    private com.example.SPSProjectBackend.repository.NcreDeveloperRepository ncreDeveloperRepository;

    @Autowired
    private com.example.SPSProjectBackend.repository.BulkCustomerRepository bulkCustomerRepository;

    @Autowired
    private com.example.SPSProjectBackend.repository.NcreInvoiceCreateRepository ncreInvoiceCreateRepository;

    // Get all readings with active bill cycle filter (default behavior) - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getAllReadings() {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findAllWithActiveBillCycle();
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all readings: " + e.getMessage(), e);
        }
    }
    
    // Get all readings without bill cycle filter (for historical access) - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getAllReadingsWithoutFilter() {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findAll();
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all readings without filter: " + e.getMessage(), e);
        }
    }

    // Get readings by account number with active bill cycle filter (default behavior) - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAccNbr(String accNbr) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByAccNbrWithActiveBillCycle(accNbr);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings for account: " + accNbr + " - " + e.getMessage(), e);
        }
    }
    
    // Get readings by account number without bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAccNbrWithoutFilter(String accNbr) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByAccNbr(accNbr);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings for account without filter: " + accNbr + " - " + e.getMessage(), e);
        }
    }

    // Get readings by account number and date with active bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAccNbrAndDate(String accNbr, Date rdngDate) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByAccNbrAndRdngDateWithActiveBillCycle(accNbr, rdngDate);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings: " + e.getMessage(), e);
        }
    }
    
    // Get readings by account number and date without filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAccNbrAndDateWithoutFilter(String accNbr, Date rdngDate) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByAccNbrAndRdngDate(accNbr, rdngDate);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings without filter: " + e.getMessage(), e);
        }
    }

    // Get readings by area code with active bill cycle filter (default behavior) - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAreaCd(String areaCd) {
        try {
            // Clean area code input
            String cleanAreaCd = areaCd != null ? areaCd.trim() : "";
            
            // Get active bill cycle from dbadmin.ncre_bill_cycle
            Optional<Integer> activeBillCycleOpt = getActiveBillCycleForArea(cleanAreaCd);
            
            if (!activeBillCycleOpt.isPresent()) {
                System.out.println("DEBUG: No active bill cycle found for area " + cleanAreaCd);
                return new ArrayList<>();
            }
            
            // Convert integer bill cycle to string for comparison with added_blcy
            Integer activeBillCycle = activeBillCycleOpt.get();
            String activeBillCycleStr = String.valueOf(activeBillCycle);
            
            System.out.println("DEBUG: Area=" + cleanAreaCd + ", Active Bill Cycle=" + activeBillCycleStr);
            
            // Use native query with proper string comparison
            List<TmpReadings> readings = tmpReadingsRepository.findByAreaCdAndBillCycle(cleanAreaCd, activeBillCycleStr);
            
            // Debug logging
            System.out.println("DEBUG: Found " + readings.size() + " readings for area " + cleanAreaCd + " and bill cycle " + activeBillCycleStr);
            
            /*
            if (readings.isEmpty()) {
                // Try alternative query using the active bill cycle filter
                System.out.println("DEBUG: Trying alternative query for area " + cleanAreaCd);
                readings = tmpReadingsRepository.findByAreaCdWithActiveBillCycle(cleanAreaCd);
                System.out.println("DEBUG: Alternative query found " + readings.size() + " readings");
            }
            */
            
            return readings.stream()
                    .filter(reading -> reading != null)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("ERROR in getReadingsByAreaCd: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve readings by area code: " + areaCd + " - " + e.getMessage(), e);
        }
    }
    
    // Get readings by area code without bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAreaCdWithoutFilter(String areaCd) {
        try {
            String cleanAreaCd = areaCd != null ? areaCd.trim() : "";
            List<TmpReadings> readings = tmpReadingsRepository.findByAreaCd(cleanAreaCd);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by area code without filter: " + areaCd + " - " + e.getMessage(), e);
        }
    }
    
    // Get readings by area code and specific bill cycle (for historical data) - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAreaCdAndBillCycle(String areaCd, String billCycle) {
        try {
            String cleanAreaCd = areaCd != null ? areaCd.trim() : "";
            String cleanBillCycle = billCycle != null ? billCycle.trim() : "";
            
            List<TmpReadings> readings = tmpReadingsRepository.findByAreaCdAndBillCycle(cleanAreaCd, cleanBillCycle);
            
            System.out.println("DEBUG: Area=" + cleanAreaCd + ", BillCycle=" + cleanBillCycle + ", Found=" + readings.size() + " readings");
            
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by area code and bill cycle: " + areaCd + ", " + billCycle + " - " + e.getMessage(), e);
        }
    }

    // Get readings by meter number with active bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByMtrNbr(String mtrNbr) {
        try {
            String cleanMtrNbr = mtrNbr != null ? mtrNbr.trim() : "";
            List<TmpReadings> readings = tmpReadingsRepository.findByMtrNbrWithActiveBillCycle(cleanMtrNbr);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by meter number: " + mtrNbr + " - " + e.getMessage(), e);
        }
    }
    
    // Get readings by meter number without filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByMtrNbrWithoutFilter(String mtrNbr) {
        try {
            String cleanMtrNbr = mtrNbr != null ? mtrNbr.trim() : "";
            List<TmpReadings> readings = tmpReadingsRepository.findByMtrNbr(cleanMtrNbr);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by meter number without filter: " + mtrNbr + " - " + e.getMessage(), e);
        }
    }

    // Get readings by date range with active bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByDateRange(Date startDate, Date endDate) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByDateRangeWithActiveBillCycle(startDate, endDate);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by date range: " + e.getMessage(), e);
        }
    }
    
    // Get readings by date range without filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByDateRangeWithoutFilter(Date startDate, Date endDate) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByDateRange(startDate, endDate);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by date range without filter: " + e.getMessage(), e);
        }
    }

    // Get latest readings for account with active bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getLatestReadingsByAccNbr(String accNbr) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findLatestReadingsByAccNbrWithActiveBillCycle(accNbr);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve latest readings: " + e.getMessage(), e);
        }
    }
    
    // Get latest readings for account without filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getLatestReadingsByAccNbrWithoutFilter(String accNbr) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findLatestReadingsByAccNbr(accNbr);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve latest readings without filter: " + e.getMessage(), e);
        }
    }
    
    // Get active bill cycle from dbadmin.ncre_bill_cycle table where is_current = 1
    @Transactional(readOnly = true)
    public Optional<Integer> getActiveBillCycleForArea(String areaCd) {
        try {
            return ncreBillCycleRepository.findCurrentBillCycleNumber();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Get specific reading - FIXED
    @Transactional(readOnly = true)
    public Optional<TmpReadingsDTO> getSpecificReading(String accNbr, String areaCd, String addedBlcy, 
                                                      Integer mtrSeq, String mtrType, Date rdngDate) {
        try {
            String cleanAccNbr = accNbr != null ? accNbr.trim() : "";
            String cleanAreaCd = areaCd != null ? areaCd.trim() : "";
            String cleanAddedBlcy = addedBlcy != null ? addedBlcy.trim() : "";
            String cleanMtrType = mtrType != null ? mtrType.trim() : "";
            
            Optional<TmpReadings> reading = tmpReadingsRepository.findSpecificReading(
                cleanAccNbr, cleanAreaCd, cleanAddedBlcy, mtrSeq, cleanMtrType, rdngDate);
            return reading.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve specific reading: " + e.getMessage(), e);
        }
    }

    // Create new reading - FIXED
    @Transactional
    public TmpReadingsDTO createReading(TmpReadingsDTO readingDTO) {
        try {
            // Validate required fields
            validateReading(readingDTO);

            // Check if reading already exists
            if (tmpReadingsRepository.existsReading(
                    readingDTO.getAccNbr(), readingDTO.getAreaCd(), readingDTO.getAddedBlcy(),
                    readingDTO.getMtrSeq(), readingDTO.getMtrType(), readingDTO.getRdngDate())) {
                throw new RuntimeException("Reading already exists for this combination");
            }

            TmpReadings reading = convertToEntity(readingDTO);
            
            // Set timestamps
            reading.setEnteredDtime(new Date());
            
            TmpReadings savedReading = tmpReadingsRepository.save(reading);
            tmpReadingsRepository.flush();
            
            return convertToDTO(savedReading);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create reading: " + e.getMessage(), e);
        }
    }

    // Update existing reading - FIXED
    @Transactional
    public TmpReadingsDTO updateReading(TmpReadingsDTO readingDTO) {
        try {
            // Validate required fields
            validateReading(readingDTO);

            Optional<TmpReadings> existingReading = tmpReadingsRepository.findSpecificReading(
                readingDTO.getAccNbr(), readingDTO.getAreaCd(), readingDTO.getAddedBlcy(),
                readingDTO.getMtrSeq(), readingDTO.getMtrType(), readingDTO.getRdngDate());

            if (!existingReading.isPresent()) {
                throw new RuntimeException("Reading not found");
            }

            TmpReadings reading = existingReading.get();
            
            // Update fields
            updateReadingFields(reading, readingDTO);
            
            // Set edit timestamp
            reading.setEditedDtime(new Date());
            if (readingDTO.getEditedUserId() != null) {
                reading.setEditedUserId(readingDTO.getEditedUserId());
            }

            TmpReadings updatedReading = tmpReadingsRepository.save(reading);
            tmpReadingsRepository.flush();
            
            return convertToDTO(updatedReading);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update reading: " + e.getMessage(), e);
        }
    }

    // Get readings with errors with active bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsWithErrors() {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findReadingsWithErrorsWithActiveBillCycle();
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings with errors: " + e.getMessage(), e);
        }
    }
    
    // Get readings with errors without filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsWithErrorsWithoutFilter() {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findReadingsWithErrors();
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings with errors without filter: " + e.getMessage(), e);
        }
    }

    // Get distinct account numbers - FIXED
    @Transactional(readOnly = true)
    public List<String> getDistinctAccNbrs() {
        try {
            return tmpReadingsRepository.findDistinctAccNbrs();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct account numbers: " + e.getMessage(), e);
        }
    }

    // Get distinct meter types - FIXED
    @Transactional(readOnly = true)
    public List<String> getDistinctMtrTypes() {
        try {
            return tmpReadingsRepository.findDistinctMtrTypes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct meter types: " + e.getMessage(), e);
        }
    }

    // Helper method to validate reading - FIXED
    private void validateReading(TmpReadingsDTO readingDTO) {
        if (readingDTO.getAccNbr() == null || readingDTO.getAccNbr().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }
        if (readingDTO.getAreaCd() == null || readingDTO.getAreaCd().trim().isEmpty()) {
            throw new RuntimeException("Area code is required");
        }
        if (readingDTO.getAddedBlcy() == null || readingDTO.getAddedBlcy().trim().isEmpty()) {
            throw new RuntimeException("Added block is required");
        }
        if (readingDTO.getMtrSeq() == null) {
            throw new RuntimeException("Meter sequence is required");
        }
        if (readingDTO.getMtrType() == null || readingDTO.getMtrType().trim().isEmpty()) {
            throw new RuntimeException("Meter type is required");
        }
        if (readingDTO.getRdngDate() == null) {
            throw new RuntimeException("Reading date is required");
        }
    }

    // Helper method to update reading fields - FIXED
    private void updateReadingFields(TmpReadings reading, TmpReadingsDTO dto) {
        if (dto.getInstId() != null) reading.setInstId(dto.getInstId().trim());
        if (dto.getPrvDate() != null) reading.setPrvDate(dto.getPrvDate());
        if (dto.getPrsntRdn() != null) reading.setPrsntRdn(dto.getPrsntRdn());
        if (dto.getPrvRdn() != null) reading.setPrvRdn(dto.getPrvRdn());
        if (dto.getMtrNbr() != null) reading.setMtrNbr(dto.getMtrNbr().trim());
        if (dto.getUnits() != null) reading.setUnits(dto.getUnits());
        if (dto.getRate() != null) reading.setRate(dto.getRate());
        if (dto.getComputedChg() != null) reading.setComputedChg(dto.getComputedChg());
        if (dto.getMntChg() != null) reading.setMntChg(dto.getMntChg());
        if (dto.getAcode() != null) reading.setAcode(dto.getAcode().trim());
        if (dto.getMFactor() != null) reading.setMFactor(dto.getMFactor());
        if (dto.getBillStat() != null) reading.setBillStat(dto.getBillStat().trim());
        if (dto.getErrStat() != null) reading.setErrStat(dto.getErrStat());
        if (dto.getMtrStat() != null) reading.setMtrStat(dto.getMtrStat().trim());
        if (dto.getRdnStat() != null) reading.setRdnStat(dto.getRdnStat().trim());
        if (dto.getUserId() != null) reading.setUserId(dto.getUserId().trim());
        if (dto.getEditedUserId() != null) reading.setEditedUserId(dto.getEditedUserId().trim());
    }

    // Convert Entity to DTO - FIXED
    private TmpReadingsDTO convertToDTO(TmpReadings reading) {
        if (reading == null) {
            return null;
        }
        
        TmpReadingsDTO dto = new TmpReadingsDTO();
        dto.setAccNbr(reading.getAccNbr());
        dto.setInstId(reading.getInstId());
        dto.setAreaCd(reading.getAreaCd());
        dto.setAddedBlcy(reading.getAddedBlcy());
        dto.setMtrSeq(reading.getMtrSeq());
        dto.setMtrType(reading.getMtrType());
        dto.setPrvDate(reading.getPrvDate());
        dto.setRdngDate(reading.getRdngDate());
        dto.setPrsntRdn(reading.getPrsntRdn());
        dto.setPrvRdn(reading.getPrvRdn());
        dto.setMtrNbr(reading.getMtrNbr());
        dto.setUnits(reading.getUnits());
        dto.setRate(reading.getRate());
        dto.setComputedChg(reading.getComputedChg());
        dto.setMntChg(reading.getMntChg());
        dto.setAcode(reading.getAcode());
        dto.setMFactor(reading.getMFactor());
        dto.setBillStat(reading.getBillStat());
        dto.setErrStat(reading.getErrStat());
        dto.setMtrStat(reading.getMtrStat());
        dto.setRdnStat(reading.getRdnStat());
        dto.setUserId(reading.getUserId());
        dto.setEnteredDtime(reading.getEnteredDtime());
        dto.setEditedUserId(reading.getEditedUserId());
        dto.setEditedDtime(reading.getEditedDtime());

        if (reading.getAccNbr() != null) {
            String trimmedAcc = reading.getAccNbr().trim();
            ncreDeveloperRepository.findByAccNbrTrimmed(trimmedAcc)
                    .ifPresent(dev -> {
                        dto.setResponsibleEe(dev.getResponsibleEe());
                        dto.setFolioNo(dev.getFolioNo());
                    });
            if (dto.getFolioNo() == null) {
                bulkCustomerRepository.findByAccNbr(trimmedAcc)
                        .ifPresent(bc -> {
                            if (bc.getFolioNo() != null) {
                                dto.setFolioNo(bc.getFolioNo());
                                ncreDeveloperRepository.findByFolioNo(bc.getFolioNo())
                                        .ifPresent(dev -> dto.setResponsibleEe(dev.getResponsibleEe()));
                            }
                        });
            }
        }
        return dto;
    }

    // Convert DTO to Entity - FIXED
    private TmpReadings convertToEntity(TmpReadingsDTO dto) {
        TmpReadings reading = new TmpReadings();
        reading.setAccNbr(dto.getAccNbr() != null ? dto.getAccNbr().trim() : null);
        reading.setInstId(dto.getInstId() != null ? dto.getInstId().trim() : null);
        reading.setAreaCd(dto.getAreaCd() != null ? dto.getAreaCd().trim() : null);
        reading.setAddedBlcy(dto.getAddedBlcy() != null ? dto.getAddedBlcy().trim() : null);
        reading.setMtrSeq(dto.getMtrSeq());
        reading.setMtrType(dto.getMtrType() != null ? dto.getMtrType().trim() : null);
        reading.setPrvDate(dto.getPrvDate());
        reading.setRdngDate(dto.getRdngDate());
        reading.setPrsntRdn(dto.getPrsntRdn());
        reading.setPrvRdn(dto.getPrvRdn());
        reading.setMtrNbr(dto.getMtrNbr() != null ? dto.getMtrNbr().trim() : null);
        reading.setUnits(dto.getUnits());
        reading.setRate(dto.getRate());
        reading.setComputedChg(dto.getComputedChg());
        reading.setMntChg(dto.getMntChg());
        reading.setAcode(dto.getAcode() != null ? dto.getAcode().trim() : null);
        reading.setMFactor(dto.getMFactor());
        reading.setBillStat(dto.getBillStat() != null ? dto.getBillStat().trim() : null);
        reading.setErrStat(dto.getErrStat());
        reading.setMtrStat(dto.getMtrStat() != null ? dto.getMtrStat().trim() : null);
        reading.setRdnStat(dto.getRdnStat() != null ? dto.getRdnStat().trim() : null);
        reading.setUserId(dto.getUserId() != null ? dto.getUserId().trim() : null);
        reading.setEnteredDtime(dto.getEnteredDtime());
        reading.setEditedUserId(dto.getEditedUserId() != null ? dto.getEditedUserId().trim() : null);
        reading.setEditedDtime(dto.getEditedDtime());
        return reading;
    }

    // ================== NCRE READINGS METHODS ==================

    private Map<String, NcreInvRdngs> getPreviousReadingsMap(String areaCd, String activeBillCycle) {
        try {
            int currentCycle = Integer.parseInt(activeBillCycle.trim());
            String previousCycle = String.valueOf(currentCycle - 1);
            List<NcreInvRdngs> prevList = ncreInvRdngsRepository.findAllByAreaCdAndAddedBlcyTrimmed(areaCd, previousCycle);
            return prevList.stream()
                    .filter(r -> r.getAccNbr() != null)
                    .collect(Collectors.toMap(r -> r.getAccNbr().trim(), r -> r, (r1, r2) -> r1));
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void populateTariffMaps(Map<String, String> tariffByAcc, Map<Short, String> tariffByFolio) {
        try {
            List<Object[]> rows = ncreDeveloperRepository.findAllDeveloperTariffDescriptions();
            if (rows != null) {
                for (Object[] row : rows) {
                    if (row != null && row.length >= 3) {
                        String acc = row[0] != null ? row[0].toString().trim() : null;
                        Short folio = null;
                        if (row[1] instanceof Number) {
                            folio = ((Number) row[1]).shortValue();
                        } else if (row[1] != null) {
                            try {
                                folio = Short.valueOf(row[1].toString().trim());
                            } catch (NumberFormatException ignored) {}
                        }
                        String tariffDesc = row[2] != null ? row[2].toString().trim() : null;
                        if (acc != null && tariffDesc != null) {
                            tariffByAcc.put(acc, tariffDesc);
                        }
                        if (folio != null && tariffDesc != null) {
                            tariffByFolio.put(folio, tariffDesc);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to load developer tariff descriptions: " + e.getMessage());
        }
    }

    private List<NcreReadingsDTO> mapToNcreReadingsDTOs(NcreInvRdngs current, NcreInvRdngs previous,
            Map<String, NcreDeveloper> devByAcc, Map<Short, NcreDeveloper> devByFolio,
            Map<String, String> tariffByAcc, Map<Short, String> tariffByFolio) {
        List<NcreReadingsDTO> dtos = new ArrayList<>();
        if (current == null) return dtos;

        Date prvDate = previous != null ? previous.getRdngDate() : null;

        // 1. KWD (Day) -> kwh_r1
        BigDecimal prsntKwd = current.getKwhR1() != null ? current.getKwhR1() : BigDecimal.ZERO;
        BigDecimal prvKwd = (previous != null && previous.getKwhR1() != null) ? previous.getKwhR1() : BigDecimal.ZERO;
        dtos.add(createNcreDTO(current, "KWD", prvDate, prsntKwd, prvKwd, devByAcc, devByFolio, tariffByAcc, tariffByFolio));

        // 2. KWP (Peak) -> kwh_r2
        BigDecimal prsntKwp = current.getKwhR2() != null ? current.getKwhR2() : BigDecimal.ZERO;
        BigDecimal prvKwp = (previous != null && previous.getKwhR2() != null) ? previous.getKwhR2() : BigDecimal.ZERO;
        dtos.add(createNcreDTO(current, "KWP", prvDate, prsntKwp, prvKwp, devByAcc, devByFolio, tariffByAcc, tariffByFolio));

        // 3. KWO (Off-Peak) -> kwh_r3
        BigDecimal prsntKwo = current.getKwhR3() != null ? current.getKwhR3() : BigDecimal.ZERO;
        BigDecimal prvKwo = (previous != null && previous.getKwhR3() != null) ? previous.getKwhR3() : BigDecimal.ZERO;
        dtos.add(createNcreDTO(current, "KWO", prvDate, prsntKwo, prvKwo, devByAcc, devByFolio, tariffByAcc, tariffByFolio));

        // 4. KWT (Total Export / RU) -> kwh_tot
        BigDecimal prsntKwt = current.getKwhTot() != null ? current.getKwhTot() : BigDecimal.ZERO;
        BigDecimal prvKwt = (previous != null && previous.getKwhTot() != null) ? previous.getKwhTot() : BigDecimal.ZERO;
        dtos.add(createNcreDTO(current, "KWT", prvDate, prsntKwt, prvKwt, devByAcc, devByFolio, tariffByAcc, tariffByFolio));

        return dtos;
    }

    private NcreReadingsDTO createNcreDTO(NcreInvRdngs current, String mtrType, Date prvDate, 
            BigDecimal prsntRdn, BigDecimal prvRdn,
            Map<String, NcreDeveloper> devByAcc, Map<Short, NcreDeveloper> devByFolio,
            Map<String, String> tariffByAcc, Map<Short, String> tariffByFolio) {
        BigDecimal units = prsntRdn.subtract(prvRdn);
        if (units.compareTo(BigDecimal.ZERO) < 0) {
            units = BigDecimal.ZERO;
        }
        NcreReadingsDTO dto = new NcreReadingsDTO();
        dto.setAccNbr(current.getAccNbr());
        dto.setAreaCd(current.getAreaCd());
        dto.setAddedBlcy(current.getAddedBlcy());
        dto.setMtrType(mtrType);
        dto.setPrvDate(prvDate);
        dto.setRdngDate(current.getRdngDate());
        dto.setPrsntRdn(prsntRdn);
        dto.setPrvRdn(prvRdn);
        dto.setMtrNbr(current.getMtrNbr());
        dto.setUnits(units);
        dto.setMtrStat(current.getMtrStat());
        dto.setRdnStat(current.getRdnStat());
        dto.setUserId(current.getUserId());
        dto.setEnteredDtime(current.getEnteredDtime());
        dto.setEditedUserId(current.getEditedUserId());
        dto.setEditedDtime(current.getEditedDtime());

        String trimmedAcc = current.getAccNbr() != null ? current.getAccNbr().trim() : null;
        NcreDeveloper dev = null;
        if (trimmedAcc != null && devByAcc != null && devByAcc.containsKey(trimmedAcc)) {
            dev = devByAcc.get(trimmedAcc);
            dto.setResponsibleEe(dev.getResponsibleEe());
            dto.setAcceptRu(dev.getAcceptRu());
            dto.setFolioNo(dev.getFolioNo());
        }

        String tariffDesc = null;
        if (trimmedAcc != null && tariffByAcc != null) {
            tariffDesc = tariffByAcc.get(trimmedAcc);
        }
        if (tariffDesc == null && dev != null && dev.getFolioNo() != null && tariffByFolio != null) {
            tariffDesc = tariffByFolio.get(dev.getFolioNo());
        }
        if (tariffDesc == null && dev != null && dev.getTariffType() != null) {
            tariffDesc = dev.getTariffType().trim();
        }
        dto.setTariffDesc(tariffDesc);
        dto.setTariffType(tariffDesc);

        return dto;
    }


    @Transactional(readOnly = true)
    public List<NcreReadingsDTO> getNcreReadingsByAreaCd(String areaCd) {
        return getNcreReadingsByAreaCd(areaCd, null, true);
    }

    @Transactional(readOnly = true)
    public List<NcreReadingsDTO> getNcreReadingsByAreaCd(String areaCd, String eeUserId) {
        return getNcreReadingsByAreaCd(areaCd, eeUserId, true);
    }

    @Transactional(readOnly = true)
    public List<NcreReadingsDTO> getNcreReadingsByAreaCd(String areaCd, String eeUserId, boolean excludeFinalized) {
        try {
            String cleanAreaCd = areaCd != null ? areaCd.trim() : "";
            Optional<Integer> activeBillCycleOpt = getActiveBillCycleForArea(cleanAreaCd);
            if (!activeBillCycleOpt.isPresent()) {
                return new ArrayList<>();
            }
            String activeBillCycleStr = String.valueOf(activeBillCycleOpt.get());
            return getNcreReadingsByAreaCdAndBillCycle(cleanAreaCd, activeBillCycleStr, eeUserId, excludeFinalized);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get NCRE readings by area code: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<NcreReadingsDTO> getNcreReadingsByAreaCdAndBillCycle(String areaCd, String billCycle) {
        return getNcreReadingsByAreaCdAndBillCycle(areaCd, billCycle, null, true);
    }

    @Transactional(readOnly = true)
    public List<NcreReadingsDTO> getNcreReadingsByAreaCdAndBillCycle(String areaCd, String billCycle, String eeUserId) {
        return getNcreReadingsByAreaCdAndBillCycle(areaCd, billCycle, eeUserId, true);
    }

    @Transactional(readOnly = true)
    public List<NcreReadingsDTO> getNcreReadingsByAreaCdAndBillCycle(String areaCd, String billCycle, String eeUserId, boolean excludeFinalized) {
        try {
            return getNcreReadingsByAreaCodesAndBillCycle(
                Collections.singletonList(areaCd), billCycle, eeUserId, excludeFinalized);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get NCRE readings by area and bill cycle: " + e.getMessage(), e);
        }
    }

    /**
     * Readings across several areas at once.
     *
     * The active bill cycle is the single global one from dbadmin.ncre_bill_cycle
     * (is_current = 1), so the same cycle is queried for every area. When no cycle is
     * current nothing is loaded rather than failing the whole request.
     */
    @Transactional(readOnly = true)
    public List<NcreReadingsDTO> getNcreReadingsByAreaCodes(List<String> areaCodes, String eeUserId,
                                                            boolean excludeFinalized) {
        List<NcreReadingsDTO> combined = new ArrayList<>();
        if (areaCodes == null || areaCodes.isEmpty()) {
            return combined;
        }

        Optional<Integer> activeBillCycleOpt = ncreBillCycleRepository.findCurrentBillCycleNumber();
        if (activeBillCycleOpt.isEmpty()) {
            return combined;
        }
        String sharedCycle = String.valueOf(activeBillCycleOpt.get());
        return getNcreReadingsByAreaCodesAndBillCycle(areaCodes, sharedCycle, eeUserId, excludeFinalized);
    }

    private List<NcreReadingsDTO> getNcreReadingsByAreaCodesAndBillCycle(List<String> areaCodes, String billCycle,
                                                                         String eeUserId, boolean excludeFinalized) {
        List<String> cleanAreaCodes = areaCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(areaCode -> !areaCode.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (cleanAreaCodes.isEmpty() || billCycle == null || billCycle.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String cleanBillCycle = billCycle.trim();

        List<NcreDeveloper> allDevs = ncreDeveloperRepository.findAll();
        Map<String, NcreDeveloper> devByAcc = allDevs.stream()
                .filter(dev -> dev.getAccNbr() != null)
                .collect(Collectors.toMap(dev -> dev.getAccNbr().trim(), dev -> dev, (first, ignored) -> first));
        Map<Short, NcreDeveloper> devByFolio = allDevs.stream()
                .filter(dev -> dev.getFolioNo() != null)
                .collect(Collectors.toMap(NcreDeveloper::getFolioNo, dev -> dev, (first, ignored) -> first));

        Map<String, String> tariffByAcc = new HashMap<>();
        Map<Short, String> tariffByFolio = new HashMap<>();
        populateTariffMaps(tariffByAcc, tariffByFolio);

        List<NcreInvRdngs> currentReadings = ncreInvRdngsRepository
                .findAllByAreaCodesAndAddedBlcyTrimmed(cleanAreaCodes, cleanBillCycle);
        Map<String, NcreInvRdngs> previousReadingsByAreaAndAccount = new HashMap<>();
        try {
            String previousCycle = String.valueOf(Integer.parseInt(cleanBillCycle) - 1);
            ncreInvRdngsRepository.findAllByAreaCodesAndAddedBlcyTrimmed(cleanAreaCodes, previousCycle)
                    .stream()
                    .filter(reading -> reading.getAreaCd() != null && reading.getAccNbr() != null)
                    .forEach(reading -> previousReadingsByAreaAndAccount.putIfAbsent(
                            readingKey(reading.getAreaCd(), reading.getAccNbr()), reading));
        } catch (NumberFormatException ignored) {
        }

        Map<Integer, Integer> isCreateByFolio = new HashMap<>();
        if (excludeFinalized) {
            try {
                ncreInvoiceCreateRepository.findByBillCycle(Integer.valueOf(cleanBillCycle)).stream()
                        .filter(record -> record.getFolioNo() != null && record.getIsCreate() != null)
                        .forEach(record -> isCreateByFolio.putIfAbsent(
                                record.getFolioNo(), record.getIsCreate()));
            } catch (NumberFormatException ignored) {
            }
        }

        List<NcreReadingsDTO> dtos = new ArrayList<>();
        for (NcreInvRdngs current : currentReadings) {
            if (current == null || current.getAccNbr() == null || current.getAreaCd() == null) {
                continue;
            }
            if (excludeFinalized) {
                NcreDeveloper developer = devByAcc.get(current.getAccNbr().trim());
                if (developer == null || developer.getFolioNo() == null
                        || !Integer.valueOf(0).equals(isCreateByFolio.get(developer.getFolioNo().intValue()))) {
                    continue;
                }
            }

            NcreInvRdngs previous = previousReadingsByAreaAndAccount.get(
                    readingKey(current.getAreaCd(), current.getAccNbr()));
            for (NcreReadingsDTO dto : mapToNcreReadingsDTOs(
                    current, previous, devByAcc, devByFolio, tariffByAcc, tariffByFolio)) {
                if (eeUserId == null || eeUserId.trim().isEmpty()
                        || com.example.SPSProjectBackend.util.SessionUtils.matchEE(dto.getResponsibleEe(), eeUserId)) {
                    dtos.add(dto);
                }
            }
        }
        return dtos;
    }

    private String readingKey(String areaCode, String accountNumber) {
        return areaCode.trim() + "_" + accountNumber.trim();
    }

    /**
     * All-cycles variant of {@link #getNcreReadingsByAreaCodes}.
     */
    @Transactional(readOnly = true)
    public List<NcreReadingsDTO> getNcreReadingsByAreaCodesWithoutFilter(List<String> areaCodes, String eeUserId,
                                                                         boolean excludeFinalized) {
        List<NcreReadingsDTO> combined = new ArrayList<>();
        if (areaCodes == null || areaCodes.isEmpty()) {
            return combined;
        }

        for (String areaCd : areaCodes) {
            try {
                combined.addAll(getNcreReadingsByAreaCdWithoutFilter(areaCd, eeUserId, excludeFinalized));
            } catch (Exception e) {
                System.err.println("Skipping area " + areaCd + " while loading all-cycle readings: " + e.getMessage());
            }
        }
        return combined;
    }

    @Transactional(readOnly = true)
    public List<NcreReadingsDTO> getNcreReadingsByAreaCdWithoutFilter(String areaCd) {
        return getNcreReadingsByAreaCdWithoutFilter(areaCd, null, true);
    }

    @Transactional(readOnly = true)
    public List<NcreReadingsDTO> getNcreReadingsByAreaCdWithoutFilter(String areaCd, String eeUserId) {
        return getNcreReadingsByAreaCdWithoutFilter(areaCd, eeUserId, true);
    }

    @Transactional(readOnly = true)
    public List<NcreReadingsDTO> getNcreReadingsByAreaCdWithoutFilter(String areaCd, String eeUserId, boolean excludeFinalized) {
        try {
            String cleanAreaCd = areaCd != null ? areaCd.trim() : "";
            List<NcreDeveloper> allDevs = ncreDeveloperRepository.findAll();
            Map<String, NcreDeveloper> devByAcc = allDevs.stream()
                    .filter(d -> d.getAccNbr() != null)
                    .collect(Collectors.toMap(d -> d.getAccNbr().trim(), d -> d, (d1, d2) -> d1));
            Map<Short, NcreDeveloper> devByFolio = allDevs.stream()
                    .filter(d -> d.getFolioNo() != null)
                    .collect(Collectors.toMap(NcreDeveloper::getFolioNo, d -> d, (d1, d2) -> d1));

            Map<String, String> tariffByAcc = new HashMap<>();
            Map<Short, String> tariffByFolio = new HashMap<>();
            populateTariffMaps(tariffByAcc, tariffByFolio);

            List<NcreInvRdngs> currentReadings = excludeFinalized
                    ? ncreInvRdngsRepository.findByAreaCdTrimmed(cleanAreaCd)
                    : ncreInvRdngsRepository.findAllByAreaCdTrimmed(cleanAreaCd);

            if (excludeFinalized) {
                List<NcreInvoiceCreate> allCreates = ncreInvoiceCreateRepository.findAll();
                Map<String, Integer> cycleFolioToIsCreate = allCreates.stream()
                        .filter(ic -> ic.getBillCycle() != null && ic.getFolioNo() != null && ic.getIsCreate() != null)
                        .collect(Collectors.toMap(
                                ic -> ic.getBillCycle() + "_" + ic.getFolioNo(),
                                NcreInvoiceCreate::getIsCreate,
                                (a, b) -> a));

                currentReadings = currentReadings.stream()
                        .filter(r -> {
                            if (r == null || r.getAccNbr() == null || r.getAddedBlcy() == null) return false;
                            NcreDeveloper dev = devByAcc.get(r.getAccNbr().trim());
                            if (dev == null || dev.getFolioNo() == null) return false;
                            String key = r.getAddedBlcy().trim() + "_" + dev.getFolioNo();
                            Integer isCreate = cycleFolioToIsCreate.get(key);
                            return isCreate != null && isCreate == 0;
                        })
                        .collect(Collectors.toList());
            }

            // Simple loop mapping where previous cycle is computed per record
            List<NcreReadingsDTO> dtos = new ArrayList<>();
            for (NcreInvRdngs current : currentReadings) {
                Map<String, NcreInvRdngs> prevMap = getPreviousReadingsMap(cleanAreaCd, current.getAddedBlcy());
                dtos.addAll(mapToNcreReadingsDTOs(current, prevMap.get(current.getAccNbr().trim()), devByAcc, devByFolio, tariffByAcc, tariffByFolio));
            }

            if (eeUserId != null && !eeUserId.trim().isEmpty()) {
                dtos = dtos.stream()
                        .filter(dto -> com.example.SPSProjectBackend.util.SessionUtils.matchEE(dto.getResponsibleEe(), eeUserId))
                        .collect(Collectors.toList());
            }

            return dtos;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get NCRE readings without filter: " + e.getMessage(), e);
        }
    }
}