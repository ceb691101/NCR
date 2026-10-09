package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.AmndType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmndTypeRepository extends JpaRepository<AmndType, String> {
}