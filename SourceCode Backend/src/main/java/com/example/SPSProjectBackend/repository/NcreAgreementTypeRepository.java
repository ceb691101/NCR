package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreAgreementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NcreAgreementTypeRepository extends JpaRepository<NcreAgreementType, String> {

    @Query("SELECT t FROM NcreAgreementType t " +
           "WHERE TRIM(t.status) = '2' " +
           "ORDER BY TRIM(t.aggTypeName)")
    List<NcreAgreementType> findActiveAgreementTypes();
}