package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.ProgressMoniter;
import com.example.SPSProjectBackend.model.ProgressMoniterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgressMoniterRepository extends JpaRepository<ProgressMoniter, ProgressMoniterId> {
}