// FILE: src/main/java/com/example/SPSProjectBackend/service/InsertNewReadingService.java
package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.InsertNewReadingDTO;
import com.example.SPSProjectBackend.dto.InsertNewReadingDTO.*;
import com.example.SPSProjectBackend.model.*;
import com.example.SPSProjectBackend.repository.*;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class InsertNewReadingService {

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;

    @Autowired
    private TmpMonTotRepository tmpMonTotRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Autowired
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    /**
     * Insert new meter readings for a customer
     * This creates records in both tmp_rdngs and tmp_mon_tot tables
     */
    public InsertNewReadingResponse insertNewMeterReadings(InsertNewReadingRequest request) {
        try {
            // Validate session and access
            validateSessionAndAccess(request.getSessionId(), request.getUserId(), request.getAreaCode());

            // Validate required fields
            validateInsertRequest(request);

            // Get developer information from ncre_developers
            Optional<NcreDeveloper> developerOpt = ncreDeveloperRepository.findByAccNbrTrimmed(request.getAccountNumber());
            if (!developerOpt.isPresent()) {
                return createErrorResponse("NCRE Developer not found for account: " + request.getAccountNumber());
            }
            NcreDeveloper developer = developerOpt.get();

            // Validate area code matches developer's area (when the developer has an area set)
            if (developer.getArea() != null && !developer.getArea().trim().isEmpty()
                    && !request.getAreaCode().equals(developer.getArea().trim())) {
                return createErrorResponse("Customer does not belong to area: " + request.getAreaCode());
            }

            // Check if customer already has readings for this bill cycle in ncre_inv_rdngs
            List<NcreInvRdngs> existingList = ncreInvRdngsRepository.findByAccNbrAndAreaCdAndAddedBlcyTrimmed(
                request.getAccountNumber(), request.getAreaCode(), request.getBillCycle());
            
            if (!existingList.isEmpty()) {
                return createErrorResponse("Customer already has readings for bill cycle: " + request.getBillCycle());
            }

            // Meter number comes from the developer record
            String meterNumber = developer.getMtrNbr();

            // Create new NcreInvRdngs entity
            NcreInvRdngs reading = new NcreInvRdngs();
            reading.setAccNbr(request.getAccountNumber().trim());
            reading.setAreaCd(request.getAreaCode().trim());
            reading.setAddedBlcy(request.getBillCycle().trim());
            reading.setMtrNbr(meterNumber != null ? meterNumber.trim() : "");
            reading.setRdngDate(request.getReadingDate());
            reading.setEnteredDtime(new Date());
            reading.setUserId(request.getUserId());

            List<String> insertedMeterTypes = new ArrayList<>();

            // Set register values based on incoming readings
            for (MeterReadingInsertDTO insertDTO : request.getMeterReadings()) {
                if (insertDTO.getPresentReading() != null) {
                    BigDecimal val = insertDTO.getPresentReading();
                    String type = insertDTO.getMeterType().trim();
                    insertedMeterTypes.add(type);
                    
                    switch (type) {
                        case "KWD":
                            reading.setKwhR1(val);
                            break;
                        case "KWP":
                            reading.setKwhR2(val);
                            break;
                        case "KWO":
                            reading.setKwhR3(val);
                            break;
                        case "KWT":
                            reading.setKwhTot(val);
                            break;
                    }
                }
            }

            // Save to database
            ncreInvRdngsRepository.save(reading);

            return createSuccessResponse(
                "New meter readings inserted successfully", 
                insertedMeterTypes.size(),
                insertedMeterTypes
            );

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Failed to insert new meter readings: " + e.getMessage());
        }
    }



    /**
     * Validate the insert request
     */
    private void validateInsertRequest(InsertNewReadingRequest request) {
        if (request.getAccountNumber() == null || request.getAccountNumber().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }
        if (request.getAreaCode() == null || request.getAreaCode().trim().isEmpty()) {
            throw new RuntimeException("Area code is required");
        }
        if (request.getBillCycle() == null || request.getBillCycle().trim().isEmpty()) {
            throw new RuntimeException("Bill cycle is required");
        }
        if (request.getReadingDate() == null) {
            throw new RuntimeException("Reading date is required");
        }
        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            throw new RuntimeException("Session ID is required");
        }
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new RuntimeException("User ID is required");
        }
        if (request.getMeterReadings() == null || request.getMeterReadings().isEmpty()) {
            throw new RuntimeException("At least one meter reading is required");
        }
        
        // Validate reading date is not in the future
        if (isFutureDate(request.getReadingDate())) {
            throw new RuntimeException("Reading date cannot be a future date");
        }
        
        // Validate at least one meter reading has present reading
        boolean hasValidReading = false;
        for (MeterReadingInsertDTO reading : request.getMeterReadings()) {
            if (reading.getPresentReading() != null) {
                hasValidReading = true;
                break;
            }
        }
        
        if (!hasValidReading) {
            throw new RuntimeException("At least one meter reading must have a present reading value");
        }
    }

    /**
     * Check if date is in the future
     */
    private boolean isFutureDate(Date date) {
        return date.after(new Date());
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

    // Response creation methods
    private InsertNewReadingResponse createSuccessResponse(String message, 
                                                          Integer insertedCount,
                                                          List<String> insertedMeterTypes) {
        InsertNewReadingResponse response = new InsertNewReadingResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setInsertedCount(insertedCount);
        response.setMeterReadingsInserted(insertedMeterTypes);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private InsertNewReadingResponse createErrorResponse(String message) {
        InsertNewReadingResponse response = new InsertNewReadingResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }
}