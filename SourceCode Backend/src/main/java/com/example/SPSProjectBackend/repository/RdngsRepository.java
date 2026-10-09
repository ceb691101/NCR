package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Rdngs;
import com.example.SPSProjectBackend.model.RdngsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RdngsRepository extends JpaRepository<Rdngs, RdngsId> {
    // Add custom query methods here if needed
}

