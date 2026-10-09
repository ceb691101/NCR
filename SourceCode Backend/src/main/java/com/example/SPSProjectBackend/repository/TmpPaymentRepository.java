package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.TmpPayment;
import com.example.SPSProjectBackend.model.TmpPaymentId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TmpPaymentRepository extends JpaRepository<TmpPayment, TmpPaymentId> {
    
    // Find all payments by area code (cent_code) with pagination
    Page<TmpPayment> findByCentCode(String centCode, Pageable pageable);
    
    // Find all payments by account number with pagination
    Page<TmpPayment> findByAccNbr(String accNbr, Pageable pageable);
    
    // Find payments by agent code
    List<TmpPayment> findByAgentCode(String agentCode);
    
    // Find payments by payment mode (Q=Quarterly, C=Cash, M=Monthly)
    List<TmpPayment> findByPayMode(String payMode);
    
    // Custom query to find payments by both agent and cent code
    @Query("SELECT tp FROM TmpPayment tp WHERE tp.agentCode = :agentCode AND tp.centCode = :centCode")
    List<TmpPayment> findByAgentCodeAndCentCode(@Param("agentCode") String agentCode, 
                                                 @Param("centCode") String centCode);
    
    // Find by counter (bill category)
    List<TmpPayment> findByCounter(String counter);
    
    // Get distinct agent codes
    @Query("SELECT DISTINCT tp.agentCode FROM TmpPayment tp WHERE tp.agentCode IS NOT NULL ORDER BY tp.agentCode")
    List<String> findDistinctAgentCodes();
    
    // Get distinct center codes
    @Query("SELECT DISTINCT tp.centCode FROM TmpPayment tp WHERE tp.centCode IS NOT NULL ORDER BY tp.centCode")
    List<String> findDistinctCenterCodes();
    
    // Get distinct counters
    @Query("SELECT DISTINCT tp.counter FROM TmpPayment tp WHERE tp.counter IS NOT NULL ORDER BY tp.counter")
    List<String> findDistinctCounters();
    
    // Get distinct pay modes
    @Query("SELECT DISTINCT tp.payMode FROM TmpPayment tp WHERE tp.payMode IS NOT NULL ORDER BY tp.payMode")
    List<String> findDistinctPayModes();
}