package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreGridSubstation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface NcreGridSubstationRepository extends JpaRepository<NcreGridSubstation, BigDecimal> {

    @Query("SELECT t FROM NcreGridSubstation t " +
           "WHERE t.status = 2 " +
           "ORDER BY t.gssName")
    List<NcreGridSubstation> findActiveGridSubstations();

    @Query("SELECT t FROM NcreGridSubstation t " +
           "WHERE t.status = 2 AND TRIM(t.licenseCode) = TRIM(:licenseCode) " +
           "ORDER BY t.gssName")
    List<NcreGridSubstation> findActiveGridSubstationsByLicenceCode(@Param("licenseCode") String licenseCode);
}