package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.PendingMeterReadingsDTO;
import com.example.SPSProjectBackend.dto.PendingMeterReadingsDTO.*;
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
public class PendingMeterReadingsService {

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private HsbAreaRepository areaRepository;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Autowired
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    @Autowired
    private NcreBillCycleRepository ncreBillCycleRepository;

    @Autowired
    private TariffRateResolver tariffRateResolver;

    /**
     * Get all pending meter readings for an area (with or without specific bill cycle)
     */
    public PendingMeterReadingResponse getPendingMeterReadingsForArea(String sessionId, String userId, 
                                                                     String areaCode, String billCycle) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            // Get target bill cycle (active if not provided)
            String targetBillCycle = getTargetBillCycle(billCycle);

            // Get all NCRE developers in the area from ncre_developers
            List<NcreDeveloper> areaDevelopers = ncreDeveloperRepository.findByAreaCodeTrimmed(areaCode);
            
            // Include developers with existing readings so they can be reviewed and edited.
            List<PendingMeterReadingDetailsDTO> pendingReadings = new ArrayList<>();
            
            for (NcreDeveloper developer : areaDevelopers) {
                if (developer.getAccNbr() == null || developer.getAccNbr().trim().isEmpty()) {
                    continue;
                }
                String accNbr = developer.getAccNbr().trim();
                PendingMeterReadingDetailsDTO pendingReading = getPendingReadingDetails(
                    accNbr, areaCode, targetBillCycle, developer);
                pendingReadings.add(pendingReading);
            }

            return createSuccessResponse("Pending meter readings retrieved successfully", pendingReadings);

        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve pending meter readings: " + e.getMessage());
        }
    }

    /**
     * Get single pending meter reading for a customer in an area
     */
    public SinglePendingReadingResponse getPendingMeterReadingForCustomer(String sessionId, String userId, String accountNumber, String areaCode, String billCycle) {
        return getPendingMeterReadingForCustomer(sessionId, userId, accountNumber, areaCode, billCycle, null);
    }

    public SinglePendingReadingResponse getPendingMeterReadingForCustomer(String sessionId, String userId, String accountNumber, String areaCode, String billCycle, Short folioNo) {
        try {
            // Validate session and access
            if (areaCode != null) {
                validateSessionAndAccess(sessionId, userId, areaCode);
            }

            Optional<NcreDeveloper> developerOpt = Optional.empty();
            String resolvedAccountNumber = accountNumber;
            String resolvedAreaCode = areaCode;

            if (folioNo != null) {
                developerOpt = ncreDeveloperRepository.findByFolioNo(folioNo);
                if (!developerOpt.isPresent()) {
                    return createSingleErrorResponse("Developer not found with folio number: " + folioNo);
                }
                NcreDeveloper dev = developerOpt.get();
                resolvedAccountNumber = dev.getAccNbr();
                if (dev.getArea() != null && !dev.getArea().trim().isEmpty()) {
                    resolvedAreaCode = dev.getArea().trim();
                }
                if (areaCode != null && resolvedAreaCode != null && !resolvedAreaCode.equals(areaCode)) {
                    validateSessionAndAccess(sessionId, userId, resolvedAreaCode);
                } else if (resolvedAreaCode != null) {
                    validateSessionAndAccess(sessionId, userId, resolvedAreaCode);
                }
            } else if (accountNumber != null) {
                if (areaCode == null) {
                    return createSingleErrorResponse("Area code is required");
                }
                developerOpt = ncreDeveloperRepository.findByAccNbrTrimmed(accountNumber);
            }

            // Customer data is sourced from ncre_developers only
            if (developerOpt.isEmpty()) {
                return createSingleErrorResponse("Developer not found with account number: " + resolvedAccountNumber);
            }

            NcreDeveloper developer = developerOpt.get();

            // Resolve the area code from the developer record
            if (resolvedAreaCode == null || resolvedAreaCode.trim().isEmpty()) {
                if (developer.getArea() != null && !developer.getArea().trim().isEmpty()) {
                    resolvedAreaCode = developer.getArea().trim();
                } else {
                    return createSingleErrorResponse("Area code could not be determined for account number: " + resolvedAccountNumber);
                }
            }

            // Get target bill cycle (active if not provided)
            String targetBillCycle = getTargetBillCycle(billCycle);

            // Return any existing cycle readings as editable form values.
            PendingMeterReadingDetailsDTO pendingReading = getPendingReadingDetails(resolvedAccountNumber, resolvedAreaCode, targetBillCycle, developer);

            return createSingleSuccessResponse("Pending meter reading retrieved successfully", pendingReading);

        } catch (Exception e) {
            return createSingleErrorResponse("Failed to retrieve pending meter reading: " + e.getMessage());
        }
    }

    /**
     * Get pending meter reading details
     */
    private PendingMeterReadingDetailsDTO getPendingReadingDetails(String accountNumber, String areaCode, String billCycle, NcreDeveloper providedDeveloper) {
        PendingMeterReadingDetailsDTO dto = new PendingMeterReadingDetailsDTO();

        // Basic information
        dto.setAccountNumber(accountNumber);
        dto.setAreaCode(areaCode);
        dto.setCurrentBillCycle(billCycle);
        // Bill cycle month/year (from ncre_bill_cycle) for the "Bill Month" display
        applyBillCycleInfo(dto, billCycle);

        // Load developer details from NcreDeveloper
        NcreDeveloper developer = providedDeveloper;
        if (developer == null && accountNumber != null) {
            developer = ncreDeveloperRepository.findByAccNbrTrimmed(accountNumber).orElse(null);
        }
        if (developer != null) {
            dto.setFolioNo(developer.getFolioNo());
            dto.setTariffType(developer.getTariffType());
            // Tariff rate from ncre_dev_year_tariff_rates (prv/cur based on active bill month)
            String resolvedTariffRate = tariffRateResolver.resolveRate(developer.getFolioNo(), billCycle);
            dto.setTariffValue(resolvedTariffRate != null ? resolvedTariffRate : developer.getInitialTariff());
            dto.setMeterNumber(developer.getMtrNbr());
            dto.setCustomerCategory(developer.getCusCat());
            dto.setAccountNumber(developer.getAccNbr() != null ? developer.getAccNbr().trim() : accountNumber);
            dto.setResponsibleEe(developer.getResponsibleEe());
            dto.setAcceptRu(developer.getAcceptRu());
            dto.setDeveloperName(developer.getDeveloperName());
            dto.setFacilityName(developer.getFacilityName());
            if (developer.getArea() != null && !developer.getArea().trim().isEmpty() && (dto.getAreaCode() == null || dto.getAreaCode().trim().isEmpty())) {
                dto.setAreaCode(developer.getArea().trim());
            }
        }

        // Resolve the effective area code (from developer if not otherwise supplied)
        String effectiveAreaCode = dto.getAreaCode() != null && !dto.getAreaCode().trim().isEmpty()
                ? dto.getAreaCode().trim()
                : (areaCode != null && !areaCode.trim().isEmpty() ? areaCode.trim() : null);

        // Get area name from the areas table
        if (effectiveAreaCode != null) {
            areaRepository.findByAreaCode(effectiveAreaCode)
                    .ifPresent(area -> dto.setAreaName(area.getAreaName()));
            dto.setAreaCode(effectiveAreaCode);
        }

        // Get previous reading information
        setPreviousReadingInfo(dto.getAccountNumber(), effectiveAreaCode, dto);

        // Calculate billCycleDate as exactly 1 month after previousReadingDate
        if (dto.getPreviousReadingDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dto.getPreviousReadingDate());
            cal.add(Calendar.MONTH, 1);
            dto.setBillCycleDate(cal.getTime());
        } else {
            // Fallback: Get bill cycle date from config table if previous reading date is missing
            if (effectiveAreaCode != null) {
                try {
                    Integer billCycleInt = Integer.parseInt(billCycle);
                    billCycleConfigRepository.findByAreaCodeAndBillCycle(effectiveAreaCode, billCycleInt)
                            .ifPresent(config -> {
                                if (config.getEnteredDate() != null) {
                                    dto.setBillCycleDate(Date.from(config.getEnteredDate().atZone(java.time.ZoneId.systemDefault()).toInstant()));
                                }
                            });
                } catch (NumberFormatException e) {
                    System.err.println("Invalid bill cycle format: " + billCycle);
                }
            }
        }

        // Get meter types and details using resolved account number and meter number
        setMeterTypesDetails(dto.getAccountNumber(), effectiveAreaCode, dto.getMeterNumber(), billCycle, dto);

        return dto;
    }

    private void setPreviousReadingInfo(String accountNumber, String areaCode, PendingMeterReadingDetailsDTO dto) {
        try {
            // Find the latest previous reading from ncre_inv_rdngs table using resolved account number
            List<NcreInvRdngs> previousReadings = ncreInvRdngsRepository.findAllByAccNbrAndAreaCdTrimmed(accountNumber, areaCode);
            
            if (!previousReadings.isEmpty()) {
                // Sort by reading date desc
                previousReadings.sort((r1, r2) -> {
                    if (r1.getRdngDate() == null) return 1;
                    if (r2.getRdngDate() == null) return -1;
                    return r2.getRdngDate().compareTo(r1.getRdngDate());
                });
                
                NcreInvRdngs latestReading = previousReadings.get(0);
                dto.setPreviousReadingDate(latestReading.getRdngDate());
            }
        } catch (Exception e) {
            System.err.println("Error getting previous reading info for customer " + accountNumber + ": " + e.getMessage());
        }
    }

    private void setMeterTypesDetails(String accNbr, String areaCode, String meterNumber, String billCycle, PendingMeterReadingDetailsDTO dto) {
        List<MeterTypePendingDTO> meterTypes = new ArrayList<>();
        String mtrNbr = meterNumber != null ? meterNumber : "";
        
        try {
            List<NcreInvRdngs> currentReadings = ncreInvRdngsRepository
                .findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed(accNbr, areaCode, billCycle);
            NcreInvRdngs currentReading = currentReadings.stream()
                .filter(reading -> mtrNbr.equals(reading.getMtrNbr()))
                .findFirst()
                .orElse(currentReadings.isEmpty() ? null : currentReadings.get(0));

            dto.setHasReading(currentReading != null);
            dto.setReadingStatus(currentReading != null ? "RECEIVED" : "PENDING");
            dto.setReadingDate(currentReading != null ? currentReading.getRdngDate() : null);

            String[] registerTypes = {"KWD", "KWP", "KWO", "KWT"};
            
            for (String type : registerTypes) {
                MeterTypePendingDTO meterTypeDTO = new MeterTypePendingDTO();
                meterTypeDTO.setMeterType(type);
                meterTypeDTO.setMeterNumber(mtrNbr);
                meterTypeDTO.setPresentReading(getPresentReading(currentReading, type));
                
                // Get previous reading for this specific meter type using resolved account number
                BigDecimal previousReading = getPreviousReadingForMeterType(accNbr, areaCode, type, billCycle);
                meterTypeDTO.setPreviousReading(previousReading != null ? previousReading : BigDecimal.ZERO);
                meterTypeDTO.setUnits(BigDecimal.ZERO); // Energy sent to grid defaults to 0 for pending
                
                meterTypes.add(meterTypeDTO);
            }
            
            dto.setMeterTypes(meterTypes);
            
        } catch (Exception e) {
            System.err.println("Error setting meter types for customer " + accNbr + ": " + e.getMessage());
            e.printStackTrace();
            dto.setMeterTypes(new ArrayList<>());
        }
    }

    private BigDecimal getPresentReading(NcreInvRdngs reading, String meterType) {
        if (reading == null) {
            return null;
        }

        switch (meterType) {
            case "KWD":
                return reading.getKwhR1();
            case "KWP":
                return reading.getKwhR2();
            case "KWO":
                return reading.getKwhR3();
            case "KWT":
                return reading.getKwhTot();
            default:
                return null;
        }
    }

    /**
     * Get previous reading for specific meter type with improved error handling
     */
    private BigDecimal getPreviousReadingForMeterType(String accNbr, String areaCd, String meterType, String billCycle) {
        if (meterType == null) {
            return BigDecimal.ZERO;
        }
        
        try {
            // Find all readings from ncre_inv_rdngs table for account and area code
            List<NcreInvRdngs> allReadings = ncreInvRdngsRepository.findByAccNbrAndAreaCdTrimmed(accNbr, areaCd);
            
            // Filter readings to only those with bill cycle less than the target bill cycle
            List<NcreInvRdngs> previousReadings = allReadings.stream()
                .filter(r -> {
                    try {
                        if (billCycle == null || billCycle.trim().isEmpty()) {
                            return true;
                        }
                        return Integer.parseInt(r.getAddedBlcy().trim()) < Integer.parseInt(billCycle.trim());
                    } catch (Exception e) {
                        return true;
                    }
                })
                .sorted((r1, r2) -> {
                    if (r1.getRdngDate() == null) return 1;
                    if (r2.getRdngDate() == null) return -1;
                    return r2.getRdngDate().compareTo(r1.getRdngDate());
                })
                .collect(Collectors.toList());
            
            if (!previousReadings.isEmpty()) {
                NcreInvRdngs latest = previousReadings.get(0);
                String trimmedType = meterType.trim();
                
                if (trimmedType.equals("KWO") && latest.getKwhR3() != null) {
                    return latest.getKwhR3();
                } else if (trimmedType.equals("KWD") && latest.getKwhR1() != null) {
                    return latest.getKwhR1();
                } else if (trimmedType.equals("KWP") && latest.getKwhR2() != null) {
                    return latest.getKwhR2();
                } else if (trimmedType.equals("KWT") && latest.getKwhTot() != null) {
                    return latest.getKwhTot();
                }
            } else {
                System.out.println("No previous readings found for account: " + accNbr + ", meter type: " + meterType);
            }
        } catch (Exception e) {
            System.err.println("Error getting previous reading for meter type " + meterType + ": " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Get target bill cycle (provided, otherwise the current cycle from ncre_bill_cycle)
     */
    private String getTargetBillCycle(String billCycle) {
        if (billCycle != null && !billCycle.trim().isEmpty()) {
            return billCycle;
        }
        
        // The active bill cycle is the single record in ncre_bill_cycle with is_current = 1
        Integer currentCycleNumber = ncreBillCycleRepository.findCurrentBillCycleNumber()
                .orElseThrow(() -> new RuntimeException("No current bill cycle found in ncre_bill_cycle (is_current = 1)"));
        
        return String.valueOf(currentCycleNumber);
    }

    /**
     * Populate bill month / bill year for the given bill cycle from ncre_bill_cycle
     */
    private void applyBillCycleInfo(PendingMeterReadingDetailsDTO dto, String billCycle) {
        if (billCycle == null || billCycle.trim().isEmpty()) {
            return;
        }
        try {
            Short billCycleNo = Short.valueOf(billCycle.trim());
            ncreBillCycleRepository.findById(billCycleNo).ifPresent(bc -> {
                dto.setBillMonth(bc.getBillMonth());
                dto.setBillYear(bc.getBillYear());
            });
        } catch (NumberFormatException e) {
            System.err.println("Bill cycle is not numeric, cannot resolve month/year: " + billCycle);
        }
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
    private PendingMeterReadingResponse createSuccessResponse(String message, List<PendingMeterReadingDetailsDTO> pendingReadings) {
        PendingMeterReadingResponse response = new PendingMeterReadingResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setPendingReadings(pendingReadings);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private PendingMeterReadingResponse createErrorResponse(String message) {
        PendingMeterReadingResponse response = new PendingMeterReadingResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private SinglePendingReadingResponse createSingleSuccessResponse(String message, PendingMeterReadingDetailsDTO pendingReading) {
        SinglePendingReadingResponse response = new SinglePendingReadingResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setPendingReading(pendingReading);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private SinglePendingReadingResponse createSingleErrorResponse(String message) {
        SinglePendingReadingResponse response = new SinglePendingReadingResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }
}
