package com.example.SPSProjectBackend.service;




import com.example.SPSProjectBackend.dto.TmpPaymentDTO;
import com.example.SPSProjectBackend.model.TmpPayment;
import com.example.SPSProjectBackend.model.TmpPaymentId;
import com.example.SPSProjectBackend.repository.TmpPaymentRepository;
import com.example.SPSProjectBackend.util.PaymentValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class TmpPaymentServiceImpl implements TmpPaymentService {
    
    @Autowired
    private TmpPaymentRepository tmpPaymentRepository;

    @Autowired
    private com.example.SPSProjectBackend.repository.NcreDeveloperRepository ncreDeveloperRepository;
    
    // Helper method to convert Entity to DTO
    private TmpPaymentDTO convertToDTO(TmpPayment tmpPayment) {
        TmpPaymentDTO dto = new TmpPaymentDTO();
        dto.setAgentCode(tmpPayment.getAgentCode());
        dto.setCentCode(tmpPayment.getCentCode());
        dto.setAccNbr(tmpPayment.getAccNbr());
        if (tmpPayment.getAccNbr() != null && ncreDeveloperRepository != null) {
            ncreDeveloperRepository.findByAccNbrTrimmed(tmpPayment.getAccNbr().trim())
                    .ifPresent(dev -> dto.setFolioNo(dev.getFolioNo()));
        }
        dto.setCounter(tmpPayment.getCounter());
        dto.setLot(tmpPayment.getLot());
        dto.setStubNo(tmpPayment.getStubNo());
        dto.setActualPayDate(tmpPayment.getActualPayDate());
        dto.setCreditDate(tmpPayment.getCreditDate());
        dto.setPayMode(tmpPayment.getPayMode());
        dto.setBankCode(tmpPayment.getBankCode());
        dto.setBranchCode(tmpPayment.getBranchCode());
        dto.setChequeNo(tmpPayment.getChequeNo());
        dto.setBankAccount(tmpPayment.getBankAccount());
        dto.setPaidAmount(tmpPayment.getPaidAmount());
        dto.setUpdateDate(tmpPayment.getUpdateDate());
        dto.setCurrentCycle(tmpPayment.getCurrentCycle());
        return dto;
    }
    
    // Helper method to convert DTO to Entity
    private TmpPayment convertToEntity(TmpPaymentDTO dto) {
        TmpPayment tmpPayment = new TmpPayment();
        tmpPayment.setAgentCode(dto.getAgentCode());
        tmpPayment.setCentCode(dto.getCentCode());
        tmpPayment.setAccNbr(dto.getAccNbr());
        tmpPayment.setCounter(dto.getCounter());
        tmpPayment.setLot(dto.getLot());
        tmpPayment.setStubNo(dto.getStubNo());
        tmpPayment.setActualPayDate(dto.getActualPayDate());
        tmpPayment.setCreditDate(dto.getCreditDate());
        tmpPayment.setPayMode(dto.getPayMode());
        tmpPayment.setBankCode(dto.getBankCode());
        tmpPayment.setBranchCode(dto.getBranchCode());
        tmpPayment.setChequeNo(dto.getChequeNo());
        tmpPayment.setBankAccount(dto.getBankAccount());
        tmpPayment.setPaidAmount(dto.getPaidAmount());
        tmpPayment.setUpdateDate(LocalDate.now());
        tmpPayment.setCurrentCycle(dto.getCurrentCycle());
        return tmpPayment;
    }
    
    @Override
    public Page<TmpPaymentDTO> getAllPayments(Pageable pageable) {
        Page<TmpPayment> payments = tmpPaymentRepository.findAll(pageable);
        return payments.map(this::convertToDTO);
    }
    
    @Override
    public TmpPaymentDTO getPaymentById(String agentCode, String centCode, String accNbr, Short stubNo) {
        TmpPaymentId id = new TmpPaymentId(agentCode, centCode, accNbr, stubNo);
        return tmpPaymentRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
    
    @Override
    public Page<TmpPaymentDTO> getPaymentsByAreaCode(String centCode, Pageable pageable) {
        Page<TmpPayment> payments = tmpPaymentRepository.findByCentCode(centCode, pageable);
        return payments.map(this::convertToDTO);
    }
    
    @Override
    public Page<TmpPaymentDTO> getPaymentsByAccountNumber(String accNbr, Pageable pageable) {
        Page<TmpPayment> payments = tmpPaymentRepository.findByAccNbr(accNbr, pageable);
        return payments.map(this::convertToDTO);
    }
    
    @Override
    public List<TmpPaymentDTO> getPaymentsByAgentCode(String agentCode) {
        List<TmpPayment> payments = tmpPaymentRepository.findByAgentCode(agentCode);
        return payments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TmpPaymentDTO> getPaymentsByPayMode(String payMode) {
        List<TmpPayment> payments = tmpPaymentRepository.findByPayMode(payMode);
        return payments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TmpPaymentDTO> getPaymentsByCounter(String counter) {
        List<TmpPayment> payments = tmpPaymentRepository.findByCounter(counter);
        return payments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public TmpPaymentDTO createPayment(TmpPaymentDTO paymentDTO) {
        // Validate input data
        PaymentValidator.validate(paymentDTO);
        
        // Check for duplicates
        TmpPaymentId id = new TmpPaymentId(
            paymentDTO.getAgentCode(),
            paymentDTO.getCentCode(),
            paymentDTO.getAccNbr(),
            paymentDTO.getStubNo()
        );
        if (tmpPaymentRepository.existsById(id)) {
            throw new RuntimeException("Payment already exists with the same Agent Code, Center Code, Account Number, and Stub No");
        }
        
        TmpPayment tmpPayment = convertToEntity(paymentDTO);
        tmpPayment.setUpdateDate(LocalDate.now());
        TmpPayment savedPayment = tmpPaymentRepository.save(tmpPayment);
        return convertToDTO(savedPayment);
    }
    
    @Override
    public TmpPaymentDTO updatePayment(String agentCode, String centCode, String accNbr, Short stubNo, TmpPaymentDTO paymentDTO) {
        // Validate input data
        PaymentValidator.validate(paymentDTO);
        
        TmpPaymentId id = new TmpPaymentId(agentCode, centCode, accNbr, stubNo);
        TmpPayment existingPayment = tmpPaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        // Update non-key fields only (can't change primary key fields)
        existingPayment.setCounter(paymentDTO.getCounter());
        existingPayment.setLot(paymentDTO.getLot());
        existingPayment.setActualPayDate(paymentDTO.getActualPayDate());
        existingPayment.setCreditDate(paymentDTO.getCreditDate());
        existingPayment.setPayMode(paymentDTO.getPayMode());
        existingPayment.setBankCode(paymentDTO.getBankCode());
        existingPayment.setBranchCode(paymentDTO.getBranchCode());
        existingPayment.setChequeNo(paymentDTO.getChequeNo());
        existingPayment.setBankAccount(paymentDTO.getBankAccount());
        existingPayment.setPaidAmount(paymentDTO.getPaidAmount());
        existingPayment.setCurrentCycle(paymentDTO.getCurrentCycle());
        existingPayment.setUpdateDate(LocalDate.now());
        
        TmpPayment updatedPayment = tmpPaymentRepository.save(existingPayment);
        return convertToDTO(updatedPayment);
    }
    
    @Override
    public void deletePayment(String agentCode, String centCode, String accNbr, Short stubNo) {
        TmpPaymentId id = new TmpPaymentId(agentCode, centCode, accNbr, stubNo);
        if (!tmpPaymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found");
        }
        tmpPaymentRepository.deleteById(id);
    }
    
    @Override
    public Map<String, List<String>> getDropdownOptions() {
        Map<String, List<String>> options = new HashMap<>();
        options.put("agentCodes", tmpPaymentRepository.findDistinctAgentCodes());
        options.put("centerCodes", tmpPaymentRepository.findDistinctCenterCodes());
        options.put("counters", tmpPaymentRepository.findDistinctCounters());
        options.put("payModes", tmpPaymentRepository.findDistinctPayModes());
        return options;
    }
}