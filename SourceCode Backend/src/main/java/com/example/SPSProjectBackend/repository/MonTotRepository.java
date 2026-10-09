package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.MonTot;
import com.example.SPSProjectBackend.model.MonTotId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonTotRepository extends JpaRepository<MonTot, MonTotId> { // Change String to MonTotId
    
    // Find by account number and bill cycle
    @Query("SELECT m FROM MonTot m WHERE m.accNbr = :accNbr AND m.billCycle = :billCycle")
    Optional<MonTot> findByAccNbrAndBillCycle(@Param("accNbr") String accNbr, @Param("billCycle") String billCycle);
    
    // Find by area code and bill cycle
    @Query("SELECT m FROM MonTot m WHERE m.billCycle = :billCycle AND m.accNbr IN " +
           "(SELECT c.accNbr FROM BulkCustomer c WHERE c.areaCd = :areaCode)")
    List<MonTot> findByAreaCodeAndBillCycle(@Param("areaCode") String areaCode, @Param("billCycle") String billCycle);

    // Find all by account number ordered by bill cycle descending (latest first)
    @Query("SELECT m FROM MonTot m WHERE m.accNbr = :accNbr ORDER BY m.billCycle DESC")
    List<MonTot> findByAccNbr(@Param("accNbr") String accNbr);
    
    // Find top N bill cycles for an account
    @Query("SELECT m FROM MonTot m WHERE m.accNbr = :accNbr ORDER BY m.billCycle DESC")
    List<MonTot> findTopNByAccNbr(@Param("accNbr") String accNbr, org.springframework.data.domain.Pageable pageable);
    
    // Get maximum bill cycle for an account
    @Query("SELECT MAX(m.billCycle) FROM MonTot m WHERE m.accNbr = :accNbr")
    Optional<String> findMaxBillCycleByAccNbr(@Param("accNbr") String accNbr);
    
    // Find specific bill cycles for an account
    @Query("SELECT m FROM MonTot m WHERE m.accNbr = :accNbr AND m.billCycle IN :billCycles ORDER BY m.billCycle DESC")
    List<MonTot> findByAccNbrAndBillCycles(@Param("accNbr") String accNbr, @Param("billCycles") List<String> billCycles);
}