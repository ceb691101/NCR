package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.MeterReadingInfoDTO;
import com.example.SPSProjectBackend.dto.MeterReadingInfoDTO.*;
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
@Transactional(readOnly = true)
public class MeterReadingInfoService {

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;

    @Autowired
    private MonTotRepository monTotRepository;

    @Autowired
    private TmpMonTotRepository tmpMonTotRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private NcreBillCycleRepository ncreBillCycleRepository;

    @Autowired
    private HsbAreaRepository areaRepository;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private HsbLocationService locationService;

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Autowired
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    @Autowired
    private TariffRateResolver tariffRateResolver;

    /**
     * Get meter reading information for a single customer
     */
    public MeterReadingInfoResponse getMeterReadingInfo(String sessionId, String userId, 
                                                       String accountNumber, String areaCode, 
                                                       String billCycle) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            Optional<NcreDeveloper> developerOpt = findDeveloper(accountNumber);
            if (!developerOpt.isPresent()) {
                return createErrorResponse("Customer not found with account number: " + accountNumber);
            }

            NcreDeveloper developer = developerOpt.get();

            // Validate area code matches the developer's area
            if (areaCode != null && !areaCode.trim().equals(trim(developer.getArea()))) {
                return createErrorResponse("Customer does not belong to area: " + areaCode);
            }

            String targetAreaCode = areaCode != null ? areaCode.trim() : trim(developer.getArea());
            if (targetAreaCode == null || targetAreaCode.isEmpty()) {
                return createErrorResponse("Area code could not be determined for account: " + accountNumber);
            }

            // Get active bill cycle if not provided
            String targetBillCycle = billCycle;
            if (targetBillCycle == null) {
                Optional<Integer> activeBillCycleOpt = ncreBillCycleRepository.findCurrentBillCycleNumber();
                if (!activeBillCycleOpt.isPresent()) {
                    return createErrorResponse("No active bill cycle found in dbadmin.ncre_bill_cycle (is_current = 1)");
                }
                targetBillCycle = activeBillCycleOpt.get().toString();
            }

            // Get meter reading information
            MeterReadingInfoDetailsDTO readingInfo = getMeterReadingDetails(developer, targetAreaCode, targetBillCycle);

            return createSuccessResponse("Meter reading information retrieved successfully", readingInfo);

        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve meter reading information: " + e.getMessage());
        }
    }

    /**
     * Get meter reading information for multiple customers in an area
     */
    public BulkMeterReadingResponse getBulkMeterReadingInfo(String sessionId, String userId, 
                                                           String areaCode, List<String> accountNumbers, 
                                                           String billCycle) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            // Get active bill cycle if not provided
            String targetBillCycle = billCycle;
            if (targetBillCycle == null) {
                Optional<Integer> activeBillCycleOpt = ncreBillCycleRepository.findCurrentBillCycleNumber();
                if (!activeBillCycleOpt.isPresent()) {
                    return createBulkErrorResponse("No active bill cycle found in dbadmin.ncre_bill_cycle (is_current = 1)");
                }
                targetBillCycle = activeBillCycleOpt.get().toString();
            }

            // Get area name
            String areaName = areaRepository.findByAreaCode(areaCode)
                    .map(HsbArea::getAreaName)
                    .orElse("");

            List<MeterReadingInfoDetailsDTO> meterReadings = new ArrayList<>();
            int customersWithReadings = 0;
            int customersWithoutReadings = 0;

            // If account numbers are provided, get specific customers, otherwise get all customers in area
            List<NcreDeveloper> developers;
            if (accountNumbers != null && !accountNumbers.isEmpty()) {
                developers = accountNumbers.stream()
                        .map(this::findDeveloper)
                        .flatMap(Optional::stream)
                        .filter(developer -> areaCode.trim().equals(trim(developer.getArea())))
                        .collect(Collectors.toList());
            } else {
                developers = ncreDeveloperRepository.findByAreaCodeTrimmed(areaCode);
            }

            for (NcreDeveloper developer : developers) {
                MeterReadingInfoDetailsDTO readingInfo = getMeterReadingDetails(developer, areaCode, targetBillCycle);
                meterReadings.add(readingInfo);

                if (readingInfo.getHasReading()) {
                    customersWithReadings++;
                } else {
                    customersWithoutReadings++;
                }
            }

            return createBulkSuccessResponse(areaCode, targetBillCycle, areaName, 
                    developers.size(), customersWithReadings, customersWithoutReadings, meterReadings);

        } catch (Exception e) {
            return createBulkErrorResponse("Failed to retrieve bulk meter reading information: " + e.getMessage());
        }
    }

    /**
     * Get meter reading details for a customer - UPDATED VERSION
     */
    private MeterReadingInfoDetailsDTO getMeterReadingDetails(NcreDeveloper developer, String areaCode, String billCycle) {
        MeterReadingInfoDetailsDTO dto = new MeterReadingInfoDetailsDTO();

        // 1. Basic customer information from ncre_developers
        String accountNumber = trim(developer.getAccNbr());
        dto.setAccountNumber(accountNumber);
        dto.setAreaCode(areaCode);
        dto.setAreaName(areaRepository.findByAreaCode(areaCode)
                .map(HsbArea::getAreaName)
                .map(this::trim)
                .orElse(""));
        dto.setCurrentBillCycle(billCycle);

        dto.setFolioNo(developer.getFolioNo());
        dto.setTariffType(developer.getTariffType());
        String resolvedTariffRate = tariffRateResolver.resolveRate(developer.getFolioNo(), billCycle);
        dto.setTariffValue(resolvedTariffRate != null ? resolvedTariffRate : developer.getInitialTariff());
        dto.setMeterNumber(developer.getMtrNbr());
        dto.setCustomerCategory(developer.getCusCat());
        dto.setAcceptRu(developer.getAcceptRu());

        // 3. Retrieve current & previous readings from ncre_inv_rdngs
        List<NcreInvRdngs> allReadings = ncreInvRdngsRepository.findByAccNbrAndAreaCdTrimmed(accountNumber, areaCode);
        
        NcreInvRdngs current = null;
        NcreInvRdngs previous = null;

        // Find current matching cycle
        for (NcreInvRdngs reading : allReadings) {
            if (reading.getAddedBlcy() != null && reading.getAddedBlcy().trim().equals(billCycle.trim())) {
                current = reading;
                break;
            }
        }

        // Find previous matching cycle (current_bill_cycle - 1)
        try {
            int currentCycle = Integer.parseInt(billCycle.trim());
            String previousCycleStr = String.valueOf(currentCycle - 1);
            for (NcreInvRdngs reading : allReadings) {
                if (reading.getAddedBlcy() != null && reading.getAddedBlcy().trim().equals(previousCycleStr)) {
                    previous = reading;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing bill cycle: " + e.getMessage());
        }

        // If previous is still null, take the first record in allReadings that is NOT the current record
        if (previous == null && !allReadings.isEmpty()) {
            for (NcreInvRdngs reading : allReadings) {
                if (current == null || !reading.getAddedBlcy().trim().equals(current.getAddedBlcy().trim())) {
                    previous = reading;
                    break;
                }
            }
        }

        // 4. Set dates in DTO
        if (current != null) {
            dto.setReadingDate(current.getRdngDate());
        }
        if (previous != null) {
            dto.setPreviousReadingDate(previous.getRdngDate());
            
            // Calculate billCycleDate as exactly 1 month after previousReadingDate
            if (previous.getRdngDate() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(previous.getRdngDate());
                cal.add(Calendar.MONTH, 1);
                dto.setBillCycleDate(cal.getTime());
            }
        }

        boolean hasReading = (current != null);
        dto.setHasReading(hasReading);
        dto.setReadingStatus(hasReading ? "RECEIVED" : "PENDING");

        // 5. Populate the four NCRE register DTOs: KWD, KWP, KWO, KWT
        List<MeterTypeDetailsDTO> meterTypes = new ArrayList<>();
        String mtrNbr = dto.getMeterNumber() != null ? dto.getMeterNumber() : "";

        // 1. KWD (Day) -> kwh_r1
        {
            BigDecimal present = (current != null) ? current.getKwhR1() : null;
            BigDecimal prev = (previous != null) ? previous.getKwhR1() : null;
            BigDecimal units = (present != null && prev != null) ? present.subtract(prev) : BigDecimal.ZERO;
            if (units.compareTo(BigDecimal.ZERO) < 0) units = BigDecimal.ZERO;
            meterTypes.add(new MeterTypeDetailsDTO("KWD", mtrNbr, present, prev, units));
        }

        // 2. KWP (Peak) -> kwh_r2
        {
            BigDecimal present = (current != null) ? current.getKwhR2() : null;
            BigDecimal prev = (previous != null) ? previous.getKwhR2() : null;
            BigDecimal units = (present != null && prev != null) ? present.subtract(prev) : BigDecimal.ZERO;
            if (units.compareTo(BigDecimal.ZERO) < 0) units = BigDecimal.ZERO;
            meterTypes.add(new MeterTypeDetailsDTO("KWP", mtrNbr, present, prev, units));
        }

        // 3. KWO (Off-Peak) -> kwh_r3
        {
            BigDecimal present = (current != null) ? current.getKwhR3() : null;
            BigDecimal prev = (previous != null) ? previous.getKwhR3() : null;
            BigDecimal units = (present != null && prev != null) ? present.subtract(prev) : BigDecimal.ZERO;
            if (units.compareTo(BigDecimal.ZERO) < 0) units = BigDecimal.ZERO;
            meterTypes.add(new MeterTypeDetailsDTO("KWO", mtrNbr, present, prev, units));
        }

        // 4. KWT (Total Export) -> kwh_tot
        {
            BigDecimal present = (current != null) ? current.getKwhTot() : null;
            BigDecimal prev = (previous != null) ? previous.getKwhTot() : null;
            BigDecimal units = (present != null && prev != null) ? present.subtract(prev) : BigDecimal.ZERO;
            if (units.compareTo(BigDecimal.ZERO) < 0) units = BigDecimal.ZERO;
            meterTypes.add(new MeterTypeDetailsDTO("KWT", mtrNbr, present, prev, units));
        }

        dto.setMeterTypes(meterTypes);
        return dto;
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
    private MeterReadingInfoResponse createSuccessResponse(String message, MeterReadingInfoDetailsDTO readingInfo) {
        MeterReadingInfoResponse response = new MeterReadingInfoResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setMeterReadingInfo(readingInfo);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private MeterReadingInfoResponse createErrorResponse(String message) {
        MeterReadingInfoResponse response = new MeterReadingInfoResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private BulkMeterReadingResponse createBulkSuccessResponse(String areaCode, String activeBillCycle, 
                                                             String areaName, int totalCustomers, 
                                                             int withReadings, int withoutReadings,
                                                             List<MeterReadingInfoDetailsDTO> readings) {
        BulkMeterReadingResponse response = new BulkMeterReadingResponse();
        response.setSuccess(true);
        response.setMessage("Bulk meter reading information retrieved successfully");
        response.setAreaCode(areaCode);
        response.setActiveBillCycle(activeBillCycle);
        response.setTotalCustomers(totalCustomers);
        response.setCustomersWithReadings(withReadings);
        response.setCustomersWithoutReadings(withoutReadings);
        response.setMeterReadings(readings);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private BulkMeterReadingResponse createBulkErrorResponse(String message) {
        BulkMeterReadingResponse response = new BulkMeterReadingResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }

    /**
     * Edit meter readings for a customer - FIXED VERSION
     */
    @Transactional
    public MeterReadingEditResponse editMeterReadings(String sessionId, String userId, 
                                                    String accountNumber, String areaCode, 
                                                    String billCycle, Date readingDate,
                                                    List<MeterReadingEditDTO> meterReadings) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            // Get customer info
            Optional<NcreDeveloper> developerOpt = findDeveloper(accountNumber);
            if (!developerOpt.isPresent()) {
                return createEditErrorResponse("Customer not found with account number: " + accountNumber);
            }
            NcreDeveloper developer = developerOpt.get();
            String resolvedAccountNumber = trim(developer.getAccNbr());

            // Validate area code matches the developer's area
            if (!areaCode.trim().equals(trim(developer.getArea()))) {
                return createEditErrorResponse("Customer does not belong to area: " + areaCode);
            }

            String meterNumber = developer.getMtrNbr();

            // Get existing active cycle reading from ncre_inv_rdngs
            List<NcreInvRdngs> existingList = ncreInvRdngsRepository.findByAccNbrAndAreaCdAndAddedBlcyTrimmed(
                resolvedAccountNumber, areaCode, billCycle);
            
            NcreInvRdngs reading;
            if (existingList.isEmpty()) {
                // If it doesn't exist, create a new one!
                reading = new NcreInvRdngs();
                reading.setAccNbr(resolvedAccountNumber);
                reading.setAreaCd(areaCode.trim());
                reading.setAddedBlcy(billCycle.trim());
                reading.setMtrNbr(meterNumber != null ? meterNumber.trim() : "");
                reading.setEnteredDtime(new Date());
                reading.setUserId(userId);
            } else {
                reading = existingList.get(0);
                reading.setEditedDtime(new Date());
                reading.setEditedUserId(userId);
            }

            // Update reading date
            if (readingDate != null) {
                reading.setRdngDate(readingDate);
            }

            // Update registers based on editDTOs
            for (MeterReadingEditDTO editDTO : meterReadings) {
                if (editDTO.getPresentReading() != null) {
                    BigDecimal val = new BigDecimal(editDTO.getPresentReading());
                    switch (editDTO.getMeterType().trim()) {
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

            // Get updated info
            MeterReadingInfoDetailsDTO updatedReadingInfo = getMeterReadingDetails(developer, areaCode, billCycle);

            return createEditSuccessResponse("Meter readings updated successfully", updatedReadingInfo);

        } catch (Exception e) {
            e.printStackTrace();
            return createEditErrorResponse("Failed to update meter readings: " + e.getMessage());
        }
    }

    // Response creation methods for edit operations
    private MeterReadingEditResponse createEditSuccessResponse(String message, MeterReadingInfoDetailsDTO readingInfo) {
        MeterReadingEditResponse response = new MeterReadingEditResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setUpdatedMeterReadingInfo(readingInfo);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private MeterReadingEditResponse createEditErrorResponse(String message) {
        MeterReadingEditResponse response = new MeterReadingEditResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private Optional<NcreDeveloper> findDeveloper(String accountNumber) {
        String normalizedAccountNumber = trim(accountNumber);
        if (normalizedAccountNumber == null || normalizedAccountNumber.isEmpty()) {
            return Optional.empty();
        }

        if (normalizedAccountNumber.regionMatches(true, 0, "PEND_", 0, 5)) {
            try {
                Short folioNo = Short.valueOf(normalizedAccountNumber.substring(5));
                return ncreDeveloperRepository.findByFolioNo(folioNo);
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        return ncreDeveloperRepository.findByAccNbrTrimmed(normalizedAccountNumber);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}