package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.ConsumptionHistoryDTO;
import com.example.SPSProjectBackend.dto.ConsumptionHistoryDTO.*;
import com.example.SPSProjectBackend.model.MonTot;
import com.example.SPSProjectBackend.model.BulkCustomer;
import com.example.SPSProjectBackend.repository.MonTotRepository;
import com.example.SPSProjectBackend.repository.BulkCustomerRepository;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ConsumptionHistoryService {

    @Autowired
    private MonTotRepository monTotRepository;

    @Autowired
    private BulkCustomerRepository bulkCustomerRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private SessionUtils sessionUtils;

    /**
     * Get consumption history for a single customer
     */
    public ConsumptionResponse getConsumptionHistory(String sessionId, String userId, 
                                                   String accountNumber, Integer cycleCount, 
                                                   String areaCode) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode, accountNumber);

            // Get customer information
            Optional<BulkCustomer> customerOpt = bulkCustomerRepository.findByAccNbr(accountNumber);
            if (!customerOpt.isPresent()) {
                return createErrorResponse("Customer not found with account number: " + accountNumber);
            }

            BulkCustomer customer = customerOpt.get();

            // Validate area code if provided
            if (areaCode != null && !areaCode.equals(customer.getAreaCd())) {
                return createErrorResponse("Customer does not belong to area: " + areaCode);
            }

            // Get consumption history
            ConsumptionSummaryDTO consumptionHistory = getConsumptionHistoryForCustomer(customer, cycleCount);

            return createSuccessResponse("Consumption history retrieved successfully", consumptionHistory);

        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve consumption history: " + e.getMessage());
        }
    }

    /**
     * Get consumption history for multiple customers
     */
    public MultipleAccountsResponse getConsumptionHistoryForMultipleAccounts(String sessionId, String userId, 
                                                                            List<String> accountNumbers, 
                                                                            Integer cycleCount, String areaCode) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode, null);

            // Validate cycle count
            if (!isValidCycleCount(cycleCount)) {
                return createMultipleAccountsErrorResponse("Invalid cycle count. Valid values are: 3, 6, 12");
            }

            List<ConsumptionSummaryDTO> histories = new ArrayList<>();
            int accountsWithData = 0;
            int accountsWithoutData = 0;

            for (String accountNumber : accountNumbers) {
                try {
                    Optional<BulkCustomer> customerOpt = bulkCustomerRepository.findByAccNbr(accountNumber);
                    if (customerOpt.isPresent()) {
                        BulkCustomer customer = customerOpt.get();
                        
                        // Validate area code if provided
                        if (areaCode != null && !areaCode.equals(customer.getAreaCd())) {
                            // Customer doesn't belong to specified area, skip or mark as error
                            continue;
                        }
                        
                        ConsumptionSummaryDTO history = getConsumptionHistoryForCustomer(customer, cycleCount);
                        histories.add(history);
                        accountsWithData++;
                    } else {
                        accountsWithoutData++;
                    }
                } catch (Exception e) {
                    accountsWithoutData++;
                    // Log error but continue with other accounts
                    System.err.println("Error processing account " + accountNumber + ": " + e.getMessage());
                }
            }

            return createMultipleAccountsSuccessResponse(
                "Consumption history retrieved for multiple accounts",
                accountNumbers.size(),
                accountsWithData,
                accountsWithoutData,
                histories
            );

        } catch (Exception e) {
            return createMultipleAccountsErrorResponse("Failed to retrieve consumption history: " + e.getMessage());
        }
    }

    /**
     * Get consumption history for a customer
     */
    private ConsumptionSummaryDTO getConsumptionHistoryForCustomer(BulkCustomer customer, Integer cycleCount) {
        // Get all bill cycles for this customer, ordered descending (latest first)
        List<MonTot> allMonTotRecords = monTotRepository.findByAccNbr(customer.getAccNbr());
        
        if (allMonTotRecords.isEmpty()) {
            return createEmptyConsumptionSummary(customer);
        }

        // Sort by bill cycle descending (latest first)
        allMonTotRecords.sort((a, b) -> {
            try {
                Integer cycleA = Integer.parseInt(a.getBillCycle().trim());
                Integer cycleB = Integer.parseInt(b.getBillCycle().trim());
                return cycleB.compareTo(cycleA);
            } catch (NumberFormatException e) {
                return b.getBillCycle().compareTo(a.getBillCycle());
            }
        });

        // Get the latest bill cycle
        String latestBillCycle = allMonTotRecords.get(0).getBillCycle();

        // Determine how many cycles to retrieve
        int cyclesToRetrieve = Math.min(cycleCount != null ? cycleCount : 12, allMonTotRecords.size());

        // Get the required number of cycles
        List<MonTot> selectedRecords = allMonTotRecords.subList(0, cyclesToRetrieve);

        // Convert to DTOs
        List<ConsumptionCycleDTO> cycles = selectedRecords.stream()
                .map(this::convertToConsumptionCycleDTO)
                .collect(Collectors.toList());

        // Create summary
        ConsumptionSummaryDTO summary = new ConsumptionSummaryDTO();
        summary.setAccountNumber(customer.getAccNbr());
        summary.setCustomerName(customer.getName());
        summary.setTariff(customer.getTariff());
        summary.setAreaCode(customer.getAreaCd());
        summary.setLatestBillCycle(latestBillCycle);
        summary.setBillCycleCount(cycles.size());
        summary.setCycles(cycles);

        // Calculate totals
        calculateTotalsAndAverages(summary, cycles);

        return summary;
    }

    /**
     * Convert MonTot entity to ConsumptionCycleDTO
     */
    private ConsumptionCycleDTO convertToConsumptionCycleDTO(MonTot monTot) {
        ConsumptionCycleDTO dto = new ConsumptionCycleDTO();
        
        dto.setBillCycle(monTot.getBillCycle());
        dto.setOffPeakConsumption(monTot.getTotUntsKwo() != null ? monTot.getTotUntsKwo() : BigDecimal.ZERO);
        dto.setDayConsumption(monTot.getTotUntsKwd() != null ? monTot.getTotUntsKwd() : BigDecimal.ZERO);
        dto.setPeakConsumption(monTot.getTotUntsKwp() != null ? monTot.getTotUntsKwp() : BigDecimal.ZERO);
        dto.setKvaConsumption(monTot.getTotKva() != null ? monTot.getTotKva() : BigDecimal.ZERO);
        dto.setTotalCharge(monTot.getTotCharge() != null ? monTot.getTotCharge() : BigDecimal.ZERO);
        dto.setFixedCharge(monTot.getFixedChg() != null ? monTot.getFixedChg() : BigDecimal.ZERO);
        dto.setVatAmount(monTot.getTotGst() != null ? monTot.getTotGst() : BigDecimal.ZERO);
        dto.setTotalAmount(monTot.getTotAmt() != null ? monTot.getTotAmt() : BigDecimal.ZERO);
        dto.setBfBalance(monTot.getBfBal() != null ? monTot.getBfBal() : BigDecimal.ZERO);
        dto.setCurrentBalance(monTot.getCrntBal() != null ? monTot.getCrntBal() : BigDecimal.ZERO);
        dto.setReadingDate(monTot.getEnteredDtime());
        
        return dto;
    }

    /**
     * Calculate totals and averages for the summary
     */
    private void calculateTotalsAndAverages(ConsumptionSummaryDTO summary, List<ConsumptionCycleDTO> cycles) {
        if (cycles == null || cycles.isEmpty()) {
            setZeroValues(summary);
            return;
        }

        BigDecimal totalOffPeak = BigDecimal.ZERO;
        BigDecimal totalDay = BigDecimal.ZERO;
        BigDecimal totalPeak = BigDecimal.ZERO;
        BigDecimal totalKva = BigDecimal.ZERO;

        for (ConsumptionCycleDTO cycle : cycles) {
            totalOffPeak = totalOffPeak.add(cycle.getOffPeakConsumption());
            totalDay = totalDay.add(cycle.getDayConsumption());
            totalPeak = totalPeak.add(cycle.getPeakConsumption());
            totalKva = totalKva.add(cycle.getKvaConsumption());
        }

        int cycleCount = cycles.size();
        BigDecimal count = new BigDecimal(cycleCount);

        summary.setTotalOffPeak(totalOffPeak.setScale(2, RoundingMode.HALF_UP));
        summary.setTotalDay(totalDay.setScale(2, RoundingMode.HALF_UP));
        summary.setTotalPeak(totalPeak.setScale(2, RoundingMode.HALF_UP));
        summary.setTotalKva(totalKva.setScale(2, RoundingMode.HALF_UP));
        
        summary.setAverageOffPeak(totalOffPeak.divide(count, 2, RoundingMode.HALF_UP));
        summary.setAverageDay(totalDay.divide(count, 2, RoundingMode.HALF_UP));
        summary.setAveragePeak(totalPeak.divide(count, 2, RoundingMode.HALF_UP));
        summary.setAverageKva(totalKva.divide(count, 2, RoundingMode.HALF_UP));
    }

    /**
     * Set zero values for empty summary
     */
    private void setZeroValues(ConsumptionSummaryDTO summary) {
        BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        
        summary.setTotalOffPeak(zero);
        summary.setTotalDay(zero);
        summary.setTotalPeak(zero);
        summary.setTotalKva(zero);
        summary.setAverageOffPeak(zero);
        summary.setAverageDay(zero);
        summary.setAveragePeak(zero);
        summary.setAverageKva(zero);
    }

    /**
     * Create empty consumption summary for customer without data
     */
    private ConsumptionSummaryDTO createEmptyConsumptionSummary(BulkCustomer customer) {
        ConsumptionSummaryDTO summary = new ConsumptionSummaryDTO();
        
        summary.setAccountNumber(customer.getAccNbr());
        summary.setCustomerName(customer.getName());
        summary.setTariff(customer.getTariff());
        summary.setAreaCode(customer.getAreaCd());
        summary.setLatestBillCycle("N/A");
        summary.setBillCycleCount(0);
        summary.setCycles(new ArrayList<>());
        
        setZeroValues(summary);
        
        return summary;
    }

    /**
     * Validate session and access rights
     */
    private void validateSessionAndAccess(String sessionId, String userId, String areaCode, String accountNumber) {
        if (sessionId == null || userId == null) {
            throw new RuntimeException("Session ID and User ID are required");
        }

        Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId, userId);
        if (!userInfoOpt.isPresent()) {
            throw new RuntimeException("Invalid session or user not found");
        }

        SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
        
        // For area-specific validation
        if (areaCode != null) {
            boolean hasAccess = sessionUtils.hasAreaAccess(sessionId, userId, 
                    userInfo.getRegionCode(), userInfo.getProvinceCode(), areaCode);
            if (!hasAccess) {
                throw new RuntimeException("Access denied to area: " + areaCode);
            }
        }
        
        // For account-specific validation, we could add additional checks here
        // For example, check if the user has access to this specific account
    }

    /**
     * Validate cycle count
     */
    private boolean isValidCycleCount(Integer cycleCount) {
        return cycleCount == null || cycleCount == 3 || cycleCount == 6 || cycleCount == 12;
    }

    // Response creation methods
    private ConsumptionResponse createSuccessResponse(String message, ConsumptionSummaryDTO consumptionHistory) {
        ConsumptionResponse response = new ConsumptionResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setConsumptionHistory(consumptionHistory);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private ConsumptionResponse createErrorResponse(String message) {
        ConsumptionResponse response = new ConsumptionResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private MultipleAccountsResponse createMultipleAccountsSuccessResponse(String message, 
                                                                         int totalAccounts,
                                                                         int accountsWithData,
                                                                         int accountsWithoutData,
                                                                         List<ConsumptionSummaryDTO> histories) {
        MultipleAccountsResponse response = new MultipleAccountsResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setTotalAccounts(totalAccounts);
        response.setAccountsWithData(accountsWithData);
        response.setAccountsWithoutData(accountsWithoutData);
        response.setConsumptionHistories(histories);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private MultipleAccountsResponse createMultipleAccountsErrorResponse(String message) {
        MultipleAccountsResponse response = new MultipleAccountsResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }

    /**
     * Get consumption history for all customers in an area
     */
    public MultipleAccountsResponse getConsumptionHistoryForArea(String sessionId, String userId, 
                                                                String areaCode, Integer cycleCount) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode, null);

            // Validate cycle count
            if (!isValidCycleCount(cycleCount)) {
                return createMultipleAccountsErrorResponse("Invalid cycle count. Valid values are: 3, 6, 12");
            }

            // Get all customers in the area
            List<BulkCustomer> customers = bulkCustomerRepository.findByAreaCd(areaCode);
            
            if (customers.isEmpty()) {
                return createMultipleAccountsErrorResponse("No customers found in area: " + areaCode);
            }

            List<ConsumptionSummaryDTO> histories = new ArrayList<>();
            int accountsWithData = 0;
            int accountsWithoutData = 0;

            for (BulkCustomer customer : customers) {
                try {
                    ConsumptionSummaryDTO history = getConsumptionHistoryForCustomer(customer, cycleCount);
                    histories.add(history);
                    
                    if (history.getBillCycleCount() > 0) {
                        accountsWithData++;
                    } else {
                        accountsWithoutData++;
                    }
                } catch (Exception e) {
                    accountsWithoutData++;
                    // Log error but continue with other customers
                    System.err.println("Error processing customer " + customer.getAccNbr() + ": " + e.getMessage());
                }
            }

            return createMultipleAccountsSuccessResponse(
                "Consumption history retrieved for area " + areaCode,
                customers.size(),
                accountsWithData,
                accountsWithoutData,
                histories
            );

        } catch (Exception e) {
            return createMultipleAccountsErrorResponse("Failed to retrieve consumption history for area: " + e.getMessage());
        }
    }
}