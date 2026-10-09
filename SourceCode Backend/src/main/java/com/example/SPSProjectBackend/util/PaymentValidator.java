package com.example.SPSProjectBackend.util;

import com.example.SPSProjectBackend.dto.TmpPaymentDTO;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Validator for TmpPayment input data
 * Handles server-side validation and sanitization
 */
public class PaymentValidator {
    
    // Regex patterns for field validation
    private static final String AGENT_CODE_PATTERN = "^[A-Z0-9]{1,4}$";
    private static final String CENT_CODE_PATTERN = "^[A-Z0-9]{1,3}$";
    private static final String ACC_NBR_PATTERN = "^[A-Z0-9]{1,10}$";
    private static final String COUNTER_PATTERN = "^[A-Z0-9]{1,3}$";
    private static final String LOT_PATTERN = "^[A-Z0-9\\-]{1,2}$";
    private static final String PAY_MODE_PATTERN = "^[A-Z]$";
    private static final String BANK_CODE_PATTERN = "^[A-Z0-9]{1,4}$";
    private static final String BRANCH_CODE_PATTERN = "^[A-Z0-9]{1,3}$";
    private static final String CHQ_NO_PATTERN = "^[A-Z0-9]{1,8}$";
    private static final String BANK_ACCOUNT_PATTERN = "^[A-Z0-9]{1,15}$";
    private static final String CURRENT_CYCLE_PATTERN = "^[A-Z0-9]{1,3}$";
    
    /**
     * Validate and sanitize TmpPaymentDTO
     * @param paymentDTO the payment data to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validate(TmpPaymentDTO paymentDTO) {
        if (paymentDTO == null) {
            throw new IllegalArgumentException("Payment data cannot be null");
        }
        
        // Validate required fields
        if (paymentDTO.getAgentCode() == null || paymentDTO.getAgentCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Agent Code is required");
        }
        if (paymentDTO.getCentCode() == null || paymentDTO.getCentCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Center Code is required");
        }
        if (paymentDTO.getAccNbr() == null || paymentDTO.getAccNbr().trim().isEmpty()) {
            throw new IllegalArgumentException("Account Number is required");
        }
        if (paymentDTO.getCounter() == null || paymentDTO.getCounter().trim().isEmpty()) {
            throw new IllegalArgumentException("Counter is required");
        }
        if (paymentDTO.getStubNo() == null) {
            throw new IllegalArgumentException("Stub No is required");
        }
        if (paymentDTO.getActualPayDate() == null) {
            throw new IllegalArgumentException("Actual Pay Date is required");
        }
        if (paymentDTO.getCreditDate() == null) {
            throw new IllegalArgumentException("Credit Date is required");
        }
        if (paymentDTO.getPayMode() == null || paymentDTO.getPayMode().trim().isEmpty()) {
            throw new IllegalArgumentException("Pay Mode is required");
        }
        if (paymentDTO.getPaidAmount() == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        
        // Sanitize and validate field formats
        sanitizeAndValidate(paymentDTO);
        
        // Business logic validation
        validateBusinessRules(paymentDTO);
    }
    
    private static void sanitizeAndValidate(TmpPaymentDTO paymentDTO) {
        String agentCode = paymentDTO.getAgentCode().trim().toUpperCase();
        String centCode = paymentDTO.getCentCode().trim().toUpperCase();
        String accNbr = paymentDTO.getAccNbr().trim();
        String counter = paymentDTO.getCounter().trim().toUpperCase();
        String lot = paymentDTO.getLot() != null ? paymentDTO.getLot().trim().toUpperCase() : "";
        String payMode = paymentDTO.getPayMode().trim().toUpperCase();
        String bankCode = paymentDTO.getBankCode() != null ? paymentDTO.getBankCode().trim().toUpperCase() : "";
        String branchCode = paymentDTO.getBranchCode() != null ? paymentDTO.getBranchCode().trim().toUpperCase() : "";
        String chqNo = paymentDTO.getChequeNo() != null ? paymentDTO.getChequeNo().trim().toUpperCase() : "";
        String bankAccount = paymentDTO.getBankAccount() != null ? paymentDTO.getBankAccount().trim().toUpperCase() : "";
        String currentCycle = paymentDTO.getCurrentCycle() != null ? paymentDTO.getCurrentCycle().trim().toUpperCase() : "";
        
        // Validate patterns
        if (!agentCode.matches(AGENT_CODE_PATTERN)) {
            throw new IllegalArgumentException("Agent Code must be 1-4 alphanumeric characters");
        }
        if (!centCode.matches(CENT_CODE_PATTERN)) {
            throw new IllegalArgumentException("Center Code must be 1-3 alphanumeric characters");
        }
        if (!accNbr.matches(ACC_NBR_PATTERN)) {
            throw new IllegalArgumentException("Account Number must be 1-10 alphanumeric characters");
        }
        if (!counter.matches(COUNTER_PATTERN)) {
            throw new IllegalArgumentException("Counter must be 1-3 alphanumeric characters");
        }
        if (!lot.isEmpty() && !lot.matches(LOT_PATTERN)) {
            throw new IllegalArgumentException("Lot must be 1-2 alphanumeric characters or dash");
        }
        if (!payMode.matches(PAY_MODE_PATTERN)) {
            throw new IllegalArgumentException("Pay Mode must be a single letter (Q, C, M)");
        }
        if (!bankCode.isEmpty() && !bankCode.matches(BANK_CODE_PATTERN)) {
            throw new IllegalArgumentException("Bank Code must be 1-4 alphanumeric characters");
        }
        if (!branchCode.isEmpty() && !branchCode.matches(BRANCH_CODE_PATTERN)) {
            throw new IllegalArgumentException("Branch Code must be 1-3 alphanumeric characters");
        }
        if (!chqNo.isEmpty() && !chqNo.matches(CHQ_NO_PATTERN)) {
            throw new IllegalArgumentException("Cheque No must be 1-8 alphanumeric characters");
        }
        if (!bankAccount.isEmpty() && !bankAccount.matches(BANK_ACCOUNT_PATTERN)) {
            throw new IllegalArgumentException("Bank Account must be 1-15 alphanumeric characters");
        }
        if (!currentCycle.isEmpty() && !currentCycle.matches(CURRENT_CYCLE_PATTERN)) {
            throw new IllegalArgumentException("Current Cycle must be 1-3 alphanumeric characters");
        }
        
        // Validate numeric fields
        if (paymentDTO.getStubNo() < 0) {
            throw new IllegalArgumentException("Stub No must be a positive number");
        }
        
        // Set sanitized values back
        paymentDTO.setAgentCode(agentCode);
        paymentDTO.setCentCode(centCode);
        paymentDTO.setAccNbr(accNbr);
        paymentDTO.setCounter(counter);
        paymentDTO.setLot(lot.isEmpty() ? null : lot); // Allow null for optional LOT
        paymentDTO.setPayMode(payMode);
        paymentDTO.setBankCode(bankCode.isEmpty() ? null : bankCode);
        paymentDTO.setBranchCode(branchCode.isEmpty() ? null : branchCode);
        paymentDTO.setChequeNo(chqNo.isEmpty() ? null : chqNo);
        paymentDTO.setBankAccount(bankAccount.isEmpty() ? null : bankAccount);
        paymentDTO.setCurrentCycle(currentCycle.isEmpty() ? null : currentCycle);
    }
    
    private static void validateBusinessRules(TmpPaymentDTO paymentDTO) {
        // Validate dates
        LocalDate today = LocalDate.now();
        if (paymentDTO.getActualPayDate().isAfter(today.plusDays(1))) {
            throw new IllegalArgumentException("Actual Pay Date cannot be in the future");
        }
        if (paymentDTO.getCreditDate().isAfter(today.plusDays(1))) {
            throw new IllegalArgumentException("Credit Date cannot be in the future");
        }
        
        // Validate amount
        if (paymentDTO.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (paymentDTO.getPaidAmount().compareTo(new BigDecimal("999999999999.99")) > 0) {
            throw new IllegalArgumentException("Amount exceeds maximum allowed value");
        }
        
        // Validate valid pay modes
        String payMode = paymentDTO.getPayMode();
        if (!payMode.matches("^[QCM]$")) {
            throw new IllegalArgumentException("Pay Mode must be Q (Quarterly), C (Cash), or M (Monthly)");
        }
    }
}
