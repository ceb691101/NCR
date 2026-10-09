package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.LoanMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanMasterRepository extends JpaRepository<LoanMaster, String> {
}
