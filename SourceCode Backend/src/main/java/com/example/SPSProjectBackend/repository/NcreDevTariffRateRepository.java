package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreDevTariffRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NcreDevTariffRateRepository extends JpaRepository<NcreDevTariffRate, Short> {

    Optional<NcreDevTariffRate> findByFolioNo(Short folioNo);
}
