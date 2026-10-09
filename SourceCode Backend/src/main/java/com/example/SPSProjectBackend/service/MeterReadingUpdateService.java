// FILE: src/main/java/com/example/SPSProjectBackend/service/MeterReadingUpdateService.java
package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.MeterReadingUpdateDTO;
import com.example.SPSProjectBackend.dto.MeterReadingDetailUpdateDTO;
import com.example.SPSProjectBackend.model.NcreInvRdngs;
import com.example.SPSProjectBackend.model.TmpReadings;
import com.example.SPSProjectBackend.model.TmpMonTot;
import com.example.SPSProjectBackend.repository.NcreInvRdngsRepository;
import com.example.SPSProjectBackend.repository.TmpReadingsRepository;
import com.example.SPSProjectBackend.repository.TmpMonTotRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MeterReadingUpdateService {

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;

    @Autowired
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    @Autowired
    private TmpMonTotRepository tmpMonTotRepository;

    @Autowired
    private SessionUtils sessionUtils;

    /**
     * Update meter readings and charges based on user edits
     * IMPORTANT: If reading_date is provided, it will update for ALL meters of that customer
     * for the specified bill cycle, not just the meters in the request
     */
    public UpdateResponse updateMeterReadings(MeterReadingUpdateDTO updateDTO) {
        try {
            // Validate session and access
            validateSessionAndAccess(updateDTO.getSessionId(), updateDTO.getUserId(), updateDTO.getAreaCode());

            // Validate required fields
            validateUpdateRequest(updateDTO);

            // Get existing reading record from ncre_inv_rdngs table
            List<NcreInvRdngs> ncreReadings = ncreInvRdngsRepository.findByAccNbrAndAreaCdAndAddedBlcyTrimmed(
                updateDTO.getAccountNumber().trim(),
                updateDTO.getAreaCode().trim(),
                updateDTO.getBillCycle().trim()
            );

            if (ncreReadings.isEmpty()) {
                return new UpdateResponse(false, "No meter readings found for this account and bill cycle");
            }

            NcreInvRdngs ncreReading = ncreReadings.get(0);
            Date currentTime = new Date();
            boolean hasUpdates = false;
            
            // 1. Update reading date if provided
            if (updateDTO.getReadingDate() != null) {
                if (ncreReading.getRdngDate() == null || !ncreReading.getRdngDate().equals(updateDTO.getReadingDate())) {
                    ncreReading.setRdngDate(updateDTO.getReadingDate());
                    hasUpdates = true;
                }
            }

            // 2. Update specific meter readings if provided
            if (updateDTO.getMeterReadings() != null && !updateDTO.getMeterReadings().isEmpty()) {
                for (MeterReadingDetailUpdateDTO meterUpdate : updateDTO.getMeterReadings()) {
                    if (meterUpdate.getPresentReading() != null) {
                        BigDecimal val = meterUpdate.getPresentReading();
                        String type = meterUpdate.getMeterType().trim().toUpperCase();
                        
                        switch (type) {
                            case "KWD":
                                if (ncreReading.getKwhR1() == null || ncreReading.getKwhR1().compareTo(val) != 0) {
                                    ncreReading.setKwhR1(val);
                                    hasUpdates = true;
                                }
                                break;
                            case "KWP":
                                if (ncreReading.getKwhR2() == null || ncreReading.getKwhR2().compareTo(val) != 0) {
                                    ncreReading.setKwhR2(val);
                                    hasUpdates = true;
                                }
                                break;
                            case "KWO":
                                if (ncreReading.getKwhR3() == null || ncreReading.getKwhR3().compareTo(val) != 0) {
                                    ncreReading.setKwhR3(val);
                                    hasUpdates = true;
                                }
                                break;
                            case "KWT":
                                if (ncreReading.getKwhTot() == null || ncreReading.getKwhTot().compareTo(val) != 0) {
                                    ncreReading.setKwhTot(val);
                                    hasUpdates = true;
                                }
                                break;
                        }
                    }
                }
            }

            // 3. Update tmp_mon_tot table if charge-related fields are provided (optional legacy compatibility)
            boolean monTotUpdated = false;
            if (updateDTO.getFixedCharge() != null || updateDTO.getMonthlyCharge() != null || 
                updateDTO.getVatAmount() != null || updateDTO.getTotalAmount() != null) {
                
                Optional<TmpMonTot> tmpMonTotOpt = tmpMonTotRepository.findByAccNbrAndBillCycle(
                    updateDTO.getAccountNumber(), updateDTO.getBillCycle());

                if (tmpMonTotOpt.isPresent()) {
                    monTotUpdated = updateTmpMonTot(tmpMonTotOpt.get(), updateDTO, updateDTO.getUserId(), currentTime);
                    if (monTotUpdated) {
                        hasUpdates = true;
                        // Save the updated TmpMonTot
                        tmpMonTotRepository.save(tmpMonTotOpt.get());
                    }
                }
            }

            // Save updated NcreInvRdngs
            if (hasUpdates) {
                ncreReading.setEditedUserId(updateDTO.getUserId());
                ncreReading.setEditedDtime(currentTime);
                ncreInvRdngsRepository.save(ncreReading);
                ncreInvRdngsRepository.flush();
            }

            if (!hasUpdates) {
                return new UpdateResponse(false, "No valid updates provided");
            }

            return new UpdateResponse(true, "Meter readings updated successfully."
                + (monTotUpdated ? " Charges updated." : ""));

        } catch (Exception e) {
            e.printStackTrace();
            return new UpdateResponse(false, "Failed to update meter readings: " + e.getMessage());
        }
    }

    /**
     * Update tmp_mon_tot table with new charges
     */
    private boolean updateTmpMonTot(TmpMonTot tmpMonTot, MeterReadingUpdateDTO updateDTO, 
                                   String userId, Date currentTime) {
        boolean hasUpdated = false;

        // Update fixed charge if provided
        if (updateDTO.getFixedCharge() != null && 
            (tmpMonTot.getFixedChg() == null || 
             tmpMonTot.getFixedChg().compareTo(updateDTO.getFixedCharge()) != 0)) {
            tmpMonTot.setFixedChg(updateDTO.getFixedCharge());
            hasUpdated = true;
        }

        // Update monthly charge if provided
        if (updateDTO.getMonthlyCharge() != null && 
            (tmpMonTot.getTotCharge() == null || 
             tmpMonTot.getTotCharge().compareTo(updateDTO.getMonthlyCharge()) != 0)) {
            tmpMonTot.setTotCharge(updateDTO.getMonthlyCharge());
            hasUpdated = true;
        }

        // Update VAT amount if provided
        if (updateDTO.getVatAmount() != null && 
            (tmpMonTot.getTotGst() == null || 
             tmpMonTot.getTotGst().compareTo(updateDTO.getVatAmount()) != 0)) {
            tmpMonTot.setTotGst(updateDTO.getVatAmount());
            hasUpdated = true;
        }

        // Update total amount if provided
        if (updateDTO.getTotalAmount() != null && 
            (tmpMonTot.getTotAmt() == null || 
             tmpMonTot.getTotAmt().compareTo(updateDTO.getTotalAmount()) != 0)) {
            tmpMonTot.setTotAmt(updateDTO.getTotalAmount());
            hasUpdated = true;
        }

        // If any field was updated, set edit timestamps
        if (hasUpdated) {
            tmpMonTot.setEditedDtime(currentTime);
            tmpMonTot.setEditedUserId(userId);
        }

        return hasUpdated;
    }

    /**
     * Validate the update request
     */
    private void validateUpdateRequest(MeterReadingUpdateDTO updateDTO) {
        if (updateDTO.getAccountNumber() == null || updateDTO.getAccountNumber().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }
        if (updateDTO.getAreaCode() == null || updateDTO.getAreaCode().trim().isEmpty()) {
            throw new RuntimeException("Area code is required");
        }
        if (updateDTO.getBillCycle() == null || updateDTO.getBillCycle().trim().isEmpty()) {
            throw new RuntimeException("Bill cycle is required");
        }
        if (updateDTO.getSessionId() == null || updateDTO.getSessionId().trim().isEmpty()) {
            throw new RuntimeException("Session ID is required");
        }
        if (updateDTO.getUserId() == null || updateDTO.getUserId().trim().isEmpty()) {
            throw new RuntimeException("User ID is required");
        }
        
        // Reading date is now optional since user might want to update only charges
        // But if provided, it will update for all meters
    }

    /**
     * Validate session and access rights
     */
    private void validateSessionAndAccess(String sessionId, String userId, String areaCode) {
        if (sessionId == null || userId == null) {
            throw new RuntimeException("Session ID and User ID are required");
        }

        Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId, userId);
        if (!userInfoOpt.isPresent()) {
            throw new RuntimeException("Invalid session or user not found");
        }

        SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
        
        // Check access based on user category
        if (areaCode != null) {
            boolean hasAccess = sessionUtils.hasAreaAccess(sessionId, userId, 
                    userInfo.getRegionCode(), userInfo.getProvinceCode(), areaCode);
            if (!hasAccess) {
                throw new RuntimeException("Access denied to area: " + areaCode);
            }
        }
    }

    /**
     * Response DTO for update operations
     */
    @Data
    public static class UpdateResponse {
        private Boolean success;
        private String message;
        private String timestamp;

        // Constructor with boolean and String parameters
        public UpdateResponse(Boolean success, String message) {
            this.success = success;
            this.message = message;
            this.timestamp = new Date().toString();
        }

        // Default constructor
        public UpdateResponse() {
            this.timestamp = new Date().toString();
        }
    }
}