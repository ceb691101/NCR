package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.TmpMonTot;
import com.example.SPSProjectBackend.model.TmpMonTotId; // Add this import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TmpMonTotRepository extends JpaRepository<TmpMonTot, TmpMonTotId> { // Change String to TmpMonTotId
    
    // Find by account number and bill cycle
    @Query("SELECT t FROM TmpMonTot t WHERE t.accNbr = :accNbr AND t.billCycle = :billCycle")
    Optional<TmpMonTot> findByAccNbrAndBillCycle(@Param("accNbr") String accNbr, @Param("billCycle") String billCycle);
    
    // Find all by account number
    @Query("SELECT t FROM TmpMonTot t WHERE t.accNbr = :accNbr ORDER BY t.billCycle DESC")
    List<TmpMonTot> findByAccNbr(@Param("accNbr") String accNbr);
    
    // Find by area code and bill cycle
    @Query("SELECT t FROM TmpMonTot t WHERE t.billCycle = :billCycle AND t.accNbr IN " +
           "(SELECT c.accNbr FROM BulkCustomer c WHERE c.areaCd = :areaCode)")
    List<TmpMonTot> findByAreaCodeAndBillCycle(@Param("areaCode") String areaCode, @Param("billCycle") String billCycle);
}