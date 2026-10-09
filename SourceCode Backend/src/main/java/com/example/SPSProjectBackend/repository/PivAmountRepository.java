package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.PivAmount;
import com.example.SPSProjectBackend.model.PivAmountId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PivAmountRepository extends JpaRepository<PivAmount, PivAmountId> {
}