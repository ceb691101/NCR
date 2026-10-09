package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Tariff.TariffId> {
    
    // Find by tariff number only (may return multiple records)
    List<Tariff> findByTariff(Short tariff);
    
    // Find by tariff number and from date (exact match)
    Optional<Tariff> findByTariffAndFrmDate(Short tariff, Date frmDate);
    
    // Find all tariffs with NULL to_date (active tariffs)
    List<Tariff> findByToDateIsNull();
    
    // Find all tariffs with NON-NULL to_date (ended tariffs)
    List<Tariff> findByToDateIsNotNull();
    
    // Find by rate status
    List<Tariff> findByRateStatus(Character rateStatus);
    
    // Find by record status
    List<Tariff> findByRecordStatus(Character recordStatus);
    
    // Get latest tariff for each tariff ID (with NULL to_date)
    @Query("SELECT t FROM Tariff t WHERE t.toDate IS NULL ORDER BY t.tariff ASC, t.frmDate DESC")
    List<Tariff> findAllActiveTariffs();
    
    // Get all ended tariffs ordered by to_date
    @Query("SELECT t FROM Tariff t WHERE t.toDate IS NOT NULL ORDER BY t.toDate DESC, t.tariff ASC")
    List<Tariff> findAllEndedTariffs();

    // Find tariffs by from date
    List<Tariff> findByFrmDate(Date frmDate);

    // Get latest active tariff for each tariff number
@Query("SELECT t FROM Tariff t WHERE t.toDate IS NULL AND t.frmDate IN " +
       "(SELECT MAX(t2.frmDate) FROM Tariff t2 WHERE t2.tariff = t.tariff AND t2.toDate IS NULL GROUP BY t2.tariff)")
List<Tariff> findLatestActiveTariffs();
}