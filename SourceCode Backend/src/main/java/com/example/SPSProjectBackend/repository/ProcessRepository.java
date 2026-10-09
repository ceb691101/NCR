package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessRepository extends JpaRepository<Process, String> {
    
    // Find process by description with trimming
    @Query("SELECT p FROM Process p WHERE TRIM(p.proDesc) = TRIM(:proDesc)")
    Optional<Process> findByProDescTrimmed(@Param("proDesc") String proDesc);
    
    // Find process by code with trimming
    @Query("SELECT p FROM Process p WHERE TRIM(p.proCode) = TRIM(:proCode)")
    Optional<Process> findByProCodeTrimmed(@Param("proCode") String proCode);
}
