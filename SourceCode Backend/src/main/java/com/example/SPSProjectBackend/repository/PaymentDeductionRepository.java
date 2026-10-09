package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.PaymentDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentDeductionRepository extends JpaRepository<PaymentDeduction, Long> {
    List<PaymentDeduction> findByAgreement_AgreementId(Long agreementId);
}
