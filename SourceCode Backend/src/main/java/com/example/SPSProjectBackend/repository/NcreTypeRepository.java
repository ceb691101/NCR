package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreType;
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NcreTypeRepository extends JpaRepository<NcreType, String> {

    @Query("SELECT t FROM NcreType t " +
           "WHERE TRIM(t.status) = '2' " +
           "ORDER BY TRIM(t.typeName)")
    List<NcreType> findActiveNcreTypes();
}
