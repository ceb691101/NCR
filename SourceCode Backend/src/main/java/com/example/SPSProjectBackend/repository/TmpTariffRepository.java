package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.TmpTariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.util.Date;

@Repository
public interface TmpTariffRepository extends JpaRepository<TmpTariff, Short> {
    
    // Find by tariff number
    Optional<TmpTariff> findByTariff(Short tariff);
    
    // Find by rate status
    List<TmpTariff> findByRateStatus(Character rateStatus);
    
    // Find by record status
    List<TmpTariff> findByRecordStatus(Character recordStatus);
    
    // Find tariffs with NULL to_date (active/draft tariffs)
    List<TmpTariff> findByToDateIsNull();
    
    // Find tariffs with NON-NULL to_date (ended tariffs for archive)
    List<TmpTariff> findByToDateIsNotNull();
    
    // Find by from_date between dates
    List<TmpTariff> findByFromDateBetween(Date startDate, Date endDate);
    
    // Find by to_date between dates
    List<TmpTariff> findByToDateBetween(Date startDate, Date endDate);
    
    // Custom query to get all active tariffs ordered by tariff number
    @Query("SELECT t FROM TmpTariff t WHERE t.toDate IS NULL ORDER BY t.tariff ASC")
    List<TmpTariff> findAllActiveTariffs();
    
    // Custom query to get all ended tariffs ordered by to_date desc
    @Query("SELECT t FROM TmpTariff t WHERE t.toDate IS NOT NULL ORDER BY t.toDate DESC")
    List<TmpTariff> findAllEndedTariffs();
}