package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.MeterDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;  // Changed from Optional

@Repository
public interface MeterDetailsRepository extends JpaRepository<MeterDetails, String> {
    // Changed to List to handle multiple results
    List<MeterDetails> findByJobNumber(String jobNumber);
}