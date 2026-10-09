 package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.TmpPaymentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface TmpPaymentService {
    
    // Get all temporary payments with pagination
    Page<TmpPaymentDTO> getAllPayments(Pageable pageable);
    
    // Get payment by composite key
    TmpPaymentDTO getPaymentById(String agentCode, String centCode, String accNbr, Short stubNo);
    
    // Get payments by area code (cent_code) with pagination
    Page<TmpPaymentDTO> getPaymentsByAreaCode(String centCode, Pageable pageable);
    
    // Get payments by account number with pagination
    Page<TmpPaymentDTO> getPaymentsByAccountNumber(String accNbr, Pageable pageable);
    
    // Get payments by agent code
    List<TmpPaymentDTO> getPaymentsByAgentCode(String agentCode);
    
    // Get payments by payment mode
    List<TmpPaymentDTO> getPaymentsByPayMode(String payMode);
    
    // Get payments by counter (bill category)
    List<TmpPaymentDTO> getPaymentsByCounter(String counter);
    
    // Create new payment
    TmpPaymentDTO createPayment(TmpPaymentDTO paymentDTO);
    
    // Update existing payment
    TmpPaymentDTO updatePayment(String agentCode, String centCode, String accNbr, Short stubNo, TmpPaymentDTO paymentDTO);
    
    // Delete payment
    void deletePayment(String agentCode, String centCode, String accNbr, Short stubNo);
    
    // Get distinct values for form dropdowns
    Map<String, List<String>> getDropdownOptions();
}