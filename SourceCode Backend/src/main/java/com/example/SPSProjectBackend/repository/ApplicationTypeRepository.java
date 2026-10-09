package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.ApplicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationTypeRepository extends JpaRepository<ApplicationType, Long> {
    // You can add custom query methods here if needed
}
