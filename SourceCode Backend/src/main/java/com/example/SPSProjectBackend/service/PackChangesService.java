// PackChangesService.java
package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.PackChangesDTO;
import com.example.SPSProjectBackend.dto.PackChangesDTO.*;
import com.example.SPSProjectBackend.model.BulkCustomer;
import com.example.SPSProjectBackend.model.TmpMonTot;
import com.example.SPSProjectBackend.repository.BulkCustomerRepository;
import com.example.SPSProjectBackend.repository.TmpMonTotRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class PackChangesService {

    @Autowired
    private BulkCustomerRepository bulkCustomerRepository;

    @Autowired
    private TmpMonTotRepository tmpMonTotRepository;

    @Autowired
    private SessionUtils sessionUtils;

    /**
     * Update pack changes for a single customer
     */
    @Transactional
    public PackChangesResponse updatePackChanges(String sessionId, String userId, 
                                               String accountNumber, String areaCode, 
                                               String billCycle, PackChangesDataDTO packChanges) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            // Get customer information
            Optional<BulkCustomer> customerOpt = bulkCustomerRepository.findByAccNbr(accountNumber);
            if (!customerOpt.isPresent()) {
                return createPackChangesErrorResponse("Customer not found with account number: " + accountNumber);
            }

            BulkCustomer customer = customerOpt.get();

            // Validate area code matches customer's area
            if (!areaCode.equals(customer.getAreaCd())) {
                return createPackChangesErrorResponse("Customer does not belong to area: " + areaCode);
            }

            // Validate at least one field is being updated
            if (!hasValidPackChanges(packChanges)) {
                return createPackChangesErrorResponse("No valid pack changes provided. At least one of reader_code, daily_pack, or walk_order must be provided.");
            }

            // Update or create TmpMonTot record with pack changes
            boolean updateSuccess = updateTmpMonTotWithPackChanges(customer, areaCode, billCycle, packChanges, userId);

            if (!updateSuccess) {
                return createPackChangesErrorResponse("Failed to update pack changes");
            }

            return createPackChangesSuccessResponse("Pack changes updated successfully", packChanges);

        } catch (Exception e) {
            return createPackChangesErrorResponse("Failed to update pack changes: " + e.getMessage());
        }
    }

    /**
     * View current and pending pack changes for a customer
     */
    @Transactional(readOnly = true)
    public PackChangesViewResponse viewPackChanges(String sessionId, String userId, 
                                                  String accountNumber, String areaCode, 
                                                  String billCycle) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            // Get customer information
            Optional<BulkCustomer> customerOpt = bulkCustomerRepository.findByAccNbr(accountNumber);
            if (!customerOpt.isPresent()) {
                return createPackChangesViewErrorResponse("Customer not found with account number: " + accountNumber);
            }

            BulkCustomer customer = customerOpt.get();

            // Validate area code matches customer's area
            if (!areaCode.equals(customer.getAreaCd())) {
                return createPackChangesViewErrorResponse("Customer does not belong to area: " + areaCode);
            }

            // Get pack changes information
            PackChangesViewDTO packChangesInfo = getPackChangesInfo(customer, areaCode, billCycle);

            return createPackChangesViewSuccessResponse("Pack changes information retrieved successfully", packChangesInfo);

        } catch (Exception e) {
            return createPackChangesViewErrorResponse("Failed to retrieve pack changes information: " + e.getMessage());
        }
    }

    /**
     * Update pack changes for multiple customers in bulk
     */
    @Transactional
    public BulkPackChangesResponse updateBulkPackChanges(String sessionId, String userId, 
                                                        String areaCode, String billCycle,
                                                        List<BulkPackChangeItemDTO> packChangesList) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            if (packChangesList == null || packChangesList.isEmpty()) {
                return createBulkPackChangesErrorResponse("No pack changes provided for bulk update");
            }

            int totalProcessed = 0;
            int successfulUpdates = 0;
            int failedUpdates = 0;
            List<String> failedAccounts = new ArrayList<>();

            for (BulkPackChangeItemDTO item : packChangesList) {
                totalProcessed++;
                
                try {
                    // Get customer information
                    Optional<BulkCustomer> customerOpt = bulkCustomerRepository.findByAccNbr(item.getAccountNumber());
                    if (!customerOpt.isPresent()) {
                        failedUpdates++;
                        failedAccounts.add(item.getAccountNumber() + " - Customer not found");
                        continue;
                    }

                    BulkCustomer customer = customerOpt.get();

                    // Validate area code matches customer's area
                    if (!areaCode.equals(customer.getAreaCd())) {
                        failedUpdates++;
                        failedAccounts.add(item.getAccountNumber() + " - Customer does not belong to area");
                        continue;
                    }

                    // Validate at least one field is being updated
                    if (!hasValidPackChanges(item.getPackChanges())) {
                        failedUpdates++;
                        failedAccounts.add(item.getAccountNumber() + " - No valid pack changes provided");
                        continue;
                    }

                    // Update TmpMonTot record with pack changes
                    boolean updateSuccess = updateTmpMonTotWithPackChanges(customer, areaCode, billCycle, 
                                                                          item.getPackChanges(), userId);

                    if (updateSuccess) {
                        successfulUpdates++;
                    } else {
                        failedUpdates++;
                        failedAccounts.add(item.getAccountNumber() + " - Failed to update pack changes");
                    }

                } catch (Exception e) {
                    failedUpdates++;
                    failedAccounts.add(item.getAccountNumber() + " - " + e.getMessage());
                }
            }

            return createBulkPackChangesSuccessResponse(totalProcessed, successfulUpdates, failedUpdates, failedAccounts);

        } catch (Exception e) {
            return createBulkPackChangesErrorResponse("Failed to process bulk pack changes: " + e.getMessage());
        }
    }

    /**
     * Get pack changes information for a customer
     */
    private PackChangesViewDTO getPackChangesInfo(BulkCustomer customer, String areaCode, String billCycle) {
        PackChangesViewDTO dto = new PackChangesViewDTO();

        // Set basic information
        dto.setAccountNumber(customer.getAccNbr());
        dto.setAreaCode(areaCode);
        dto.setBillCycle(billCycle);

        // Set current values from Customer table
        dto.setCurrentReaderCode(customer.getRedCode());
        dto.setCurrentDailyPack(customer.getDlyPack());
        dto.setCurrentWalkOrder(customer.getWlkOrd());

        // Get pending changes from TmpMonTot table
        Optional<TmpMonTot> tmpMonTotOpt = tmpMonTotRepository.findByAccNbrAndBillCycle(customer.getAccNbr(), billCycle);
        
        if (tmpMonTotOpt.isPresent()) {
            TmpMonTot tmpMonTot = tmpMonTotOpt.get();
            
            // Set new values from TmpMonTot
            dto.setNewReaderCode(tmpMonTot.getNewRederCd());
            dto.setNewDailyPack(tmpMonTot.getNewDlyPack());
            dto.setNewWalkOrder(tmpMonTot.getNewWlkOrd());
            
            // Check if there are pending changes
            boolean hasPendingChanges = hasPendingPackChanges(customer, tmpMonTot);
            dto.setHasPendingChanges(hasPendingChanges);
        } else {
            // No pending changes
            dto.setNewReaderCode(null);
            dto.setNewDailyPack(null);
            dto.setNewWalkOrder(null);
            dto.setHasPendingChanges(false);
        }

        return dto;
    }

    /**
     * Check if there are pending pack changes
     */
    private boolean hasPendingPackChanges(BulkCustomer customer, TmpMonTot tmpMonTot) {
        // Compare current values with new values
        boolean readerCodeChanged = tmpMonTot.getNewRederCd() != null && 
                                   !tmpMonTot.getNewRederCd().equals(customer.getRedCode());
        
        boolean dailyPackChanged = tmpMonTot.getNewDlyPack() != null && 
                                  !tmpMonTot.getNewDlyPack().equals(customer.getDlyPack());
        
        boolean walkOrderChanged = tmpMonTot.getNewWlkOrd() != null && 
                                  !tmpMonTot.getNewWlkOrd().equals(customer.getWlkOrd());

        return readerCodeChanged || dailyPackChanged || walkOrderChanged;
    }

    /**
     * Update TmpMonTot with pack changes
     */
    private boolean updateTmpMonTotWithPackChanges(BulkCustomer customer, String areaCode, 
                                                  String billCycle, PackChangesDataDTO packChanges, 
                                                  String userId) {
        try {
            Optional<TmpMonTot> tmpMonTotOpt = tmpMonTotRepository.findByAccNbrAndBillCycle(customer.getAccNbr(), billCycle);
            
            TmpMonTot tmpMonTot;
            if (tmpMonTotOpt.isPresent()) {
                // Update existing record
                tmpMonTot = tmpMonTotOpt.get();
            } else {
                // Create new record
                tmpMonTot = new TmpMonTot();
                tmpMonTot.setAccNbr(customer.getAccNbr());
                tmpMonTot.setBillCycle(billCycle);
                
                // Initialize with current values if creating new record
                tmpMonTot.setTotUntsKwo(null);
                tmpMonTot.setTotUntsKwd(null);
                tmpMonTot.setTotUntsKwp(null);
                tmpMonTot.setTotKva(null);
                tmpMonTot.setTotCompChg(null);
                tmpMonTot.setTotCharge(null);
                tmpMonTot.setFixedChg(null);
                tmpMonTot.setTotGst(null);
                tmpMonTot.setTotAmt(null);
                tmpMonTot.setCrntBal(null);
                tmpMonTot.setTotBillStat(null);
                tmpMonTot.setErrStat(null);
                
                // Set entered timestamp for new records
                tmpMonTot.setEnteredDtime(new Date());
                tmpMonTot.setUserId(userId);
            }

            // Update pack change fields
            if (packChanges.getNewReaderCode() != null && !packChanges.getNewReaderCode().trim().isEmpty()) {
                tmpMonTot.setNewRederCd(packChanges.getNewReaderCode().trim());
            }
            
            if (packChanges.getNewDailyPack() != null && !packChanges.getNewDailyPack().trim().isEmpty()) {
                tmpMonTot.setNewDlyPack(packChanges.getNewDailyPack().trim());
            }
            
            if (packChanges.getNewWalkOrder() != null && !packChanges.getNewWalkOrder().trim().isEmpty()) {
                tmpMonTot.setNewWlkOrd(packChanges.getNewWalkOrder().trim());
            }

            // Set edit timestamp
            tmpMonTot.setEditedDtime(new Date());
            tmpMonTot.setEditedUserId(userId);

            tmpMonTotRepository.save(tmpMonTot);
            tmpMonTotRepository.flush();

            return true;

        } catch (Exception e) {
            System.err.println("Failed to update TmpMonTot with pack changes: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate that at least one pack change field is provided
     */
    private boolean hasValidPackChanges(PackChangesDataDTO packChanges) {
        if (packChanges == null) {
            return false;
        }
        
        boolean hasReaderCode = packChanges.getNewReaderCode() != null && 
                              !packChanges.getNewReaderCode().trim().isEmpty();
        
        boolean hasDailyPack = packChanges.getNewDailyPack() != null && 
                             !packChanges.getNewDailyPack().trim().isEmpty();
        
        boolean hasWalkOrder = packChanges.getNewWalkOrder() != null && 
                             !packChanges.getNewWalkOrder().trim().isEmpty();

        return hasReaderCode || hasDailyPack || hasWalkOrder;
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
    private PackChangesResponse createPackChangesSuccessResponse(String message, PackChangesDataDTO packChanges) {
        PackChangesResponse response = new PackChangesResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setUpdatedPackChanges(packChanges);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private PackChangesResponse createPackChangesErrorResponse(String message) {
        PackChangesResponse response = new PackChangesResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private PackChangesViewResponse createPackChangesViewSuccessResponse(String message, PackChangesViewDTO packChangesInfo) {
        PackChangesViewResponse response = new PackChangesViewResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setPackChangesInfo(packChangesInfo);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private PackChangesViewResponse createPackChangesViewErrorResponse(String message) {
        PackChangesViewResponse response = new PackChangesViewResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private BulkPackChangesResponse createBulkPackChangesSuccessResponse(int totalProcessed, int successfulUpdates, 
                                                                       int failedUpdates, List<String> failedAccounts) {
        BulkPackChangesResponse response = new BulkPackChangesResponse();
        response.setSuccess(true);
        response.setMessage("Bulk pack changes processed successfully");
        response.setTotalProcessed(totalProcessed);
        response.setSuccessfulUpdates(successfulUpdates);
        response.setFailedUpdates(failedUpdates);
        response.setFailedAccounts(failedAccounts);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private BulkPackChangesResponse createBulkPackChangesErrorResponse(String message) {
        BulkPackChangesResponse response = new BulkPackChangesResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }
}