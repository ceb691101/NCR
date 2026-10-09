package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Spestcnd;
import com.example.SPSProjectBackend.model.SpestcndId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpestcndRepository extends JpaRepository<Spestcnd, SpestcndId> {
}
