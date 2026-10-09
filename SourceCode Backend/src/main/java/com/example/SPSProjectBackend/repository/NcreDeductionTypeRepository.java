package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreDeductionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NcreDeductionTypeRepository extends JpaRepository<NcreDeductionType, String> {

    @Query("SELECT t FROM NcreDeductionType t " +
           "WHERE t.status = '2' " +
           "ORDER BY t.dedTypeNm")
    List<NcreDeductionType> findActiveDeductionTypes();
}