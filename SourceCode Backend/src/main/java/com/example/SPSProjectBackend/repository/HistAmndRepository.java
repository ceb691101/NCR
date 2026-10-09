package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.HistAmnd;
import com.example.SPSProjectBackend.model.HistAmndPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistAmndRepository extends JpaRepository<HistAmnd, HistAmndPk> {
}