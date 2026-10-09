package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.YrMnth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YrMnthRepository extends JpaRepository<YrMnth, Integer> {
}