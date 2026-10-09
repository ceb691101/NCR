package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreTariffDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NcreTariffDescriptionRepository extends JpaRepository<NcreTariffDescription, String> {

    @Query("SELECT DISTINCT TRIM(t.tariffDesc) " +
           "FROM NcreTariffDescription t " +
           "WHERE TRIM(t.status) = '2' " +
           "AND t.tariffDesc IS NOT NULL " +
           "AND TRIM(t.tariffDesc) <> '' " +
           "ORDER BY TRIM(t.tariffDesc)")
    List<String> findActiveTariffDescriptions();
}
